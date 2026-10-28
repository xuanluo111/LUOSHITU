package com.fongmi.android.tv.server.process;

import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

import com.fongmi.android.tv.server.Server;
import com.github.catvod.net.OkHttp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 洛视频新增（播放器基础设施）：内置通用 m3u8/ts 代理。
 * do=m3u8 拉取播放列表并把分片/嵌套列表/KEY/MAP 的 URI 重写为本代理地址；do=ts 流式透传上游分片。
 * 替代加壳 jar 内部的同名代理（其内部异常会导致 500 空响应，真机实测）；不依赖 jar 上下文，对全部源生效。
 * 仅做地址重写与字节透传，不做任何内容加工（合规：与直播 m3u 解析同类的基础设施）。
 */
public class MediaProxy {

    private static final Pattern URI_ATTR = Pattern.compile("URI=\"([^\"]+)\"");
    private static final String CONTENT_RANGE = "Content-Range";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String PLAYLIST = "m3u8";
    private static final String SEGMENT = "ts";
    private static final int MAX_PLAYLIST_BYTES = 2 * 1024 * 1024;

    public static boolean matches(Map<String, String> params) {
        String doParam = params.get("do");
        return "m3u8".equals(doParam) || "ts".equals(doParam);
    }

    public static Object[] handle(Map<String, String> params) throws Exception {
        String url = params.get("url");
        Log.d("MediaProxy", "handle do=" + params.get("do") + " url=" + url);
        if (TextUtils.isEmpty(url)) return error("missing url");
        Map<String, String> headers = decodeHeaders(params.get("headers"));
        if (PLAYLIST.equals(params.get("do"))) {
            try {
                // 列表必须整份拉取才能重写地址：不透传 Range，也不能把 Range 烧进子地址
                String text = fetchText(url, headers);
                Log.d("MediaProxy", "upstream ok len=" + text.length());
                return new Object[]{200, "application/vnd.apple.mpegurl", new ByteArrayInputStream(rewrite(url, text, headers).getBytes(StandardCharsets.UTF_8))};
            } catch (Throwable t) {
                Log.d("MediaProxy", "m3u8 fail", t);
                return error("m3u8 " + t);
            }
        }
        return pass(url, withRange(headers, params.get("range")));
    }

    private static Object[] error(String message) {
        return new Object[]{500, "text/plain", new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8))};
    }

    private static String fetchText(String url, Map<String, String> headers) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        headers.forEach(builder::header);
        try (Response res = OkHttp.client().newCall(builder.build()).execute()) {
            if (!res.isSuccessful()) throw new Exception("upstream " + res.code());
            // 洛视频：列表无上限读入内存会被超大/恶意源拖垮进程，给一个远超正常列表的上限兜底。
            try (InputStream in = res.body().byteStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int total = 0, read;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_PLAYLIST_BYTES) throw new Exception("playlist exceeds " + MAX_PLAYLIST_BYTES + " bytes");
                    out.write(buffer, 0, read);
                }
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * 洛视频：播放器的 CacheDataSource/预加载会带 Range 请求分片，不透传会让代理退化成每次整段重下，首屏更慢。
     * Proxy 已把客户端请求头(小写)并入 params。
     */
    private static Map<String, String> withRange(Map<String, String> headers, String range) {
        if (TextUtils.isEmpty(range)) return headers;
        Map<String, String> prepared = new LinkedHashMap<>(headers);
        prepared.put("Range", range);
        return prepared;
    }

    private static Object[] pass(String url, Map<String, String> headers) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        headers.forEach(builder::header);
        Response res = OkHttp.client().newCall(builder.build()).execute();
        ResponseBody body = res.body();
        if (body == null) {
            res.close();
            return error("empty upstream");
        }
        String mime = body.contentType() == null ? "video/mp2t" : body.contentType().toString();
        Log.d("MediaProxy", "ts upstream code=" + res.code() + " len=" + body.contentLength() + " mime=" + mime + " range=" + headers.get("Range"));
        Map<String, String> out = new LinkedHashMap<>();
        putHeader(res, out, CONTENT_RANGE);
        putHeader(res, out, ACCEPT_RANGES);
        return new Object[]{res.code(), mime, new UpstreamStream(body.byteStream(), res), out};
    }

    private static void putHeader(Response res, Map<String, String> out, String name) {
        String value = res.header(name);
        if (!TextUtils.isEmpty(value)) out.put(name, value);
    }

    private static String rewrite(String base, String text, Map<String, String> headers) {
        // 洛视频：中文/劣质源的分片行常含未转义字符，URI.create/resolve 会抛 IllegalArgumentException。
        // 单行失败只牺牲该行（原样输出），不能一行坏数据打死整份列表（此前整列表 500 → 频道直接失败）。
        URI ctx;
        try {
            ctx = URI.create(base);
        } catch (Throwable t) {
            Log.w("MediaProxy", "m3u8 base not a URI, pass through: " + base);
            return text;
        }
        String headersParam = encodeHeaders(headers);
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) {
                sb.append('\n');
            } else if (t.startsWith("#")) {
                try {
                    sb.append(rewriteTag(t, ctx, headersParam)).append('\n');
                } catch (Throwable e) {
                    sb.append(line).append('\n');
                }
            } else {
                try {
                    String target = ctx.resolve(t).toString();
                    sb.append(proxy(kind(target), target, headersParam)).append('\n');
                } catch (Throwable e) {
                    sb.append(line).append('\n');
                }
            }
        }
        return sb.toString();
    }

    private static String rewriteTag(String line, URI ctx, String headersParam) {
        if (!line.contains("URI=\"")) return line;
        Matcher matcher = URI_ATTR.matcher(line);
        StringBuffer sb = new StringBuffer(); // Android 的 Matcher.appendReplacement 仅有 StringBuffer 重载
        while (matcher.find()) {
            String target;
            try {
                target = ctx.resolve(matcher.group(1)).toString();
            } catch (Throwable e) {
                target = matcher.group(1); // 解析失败保留原 URI，由播放器直连
            }
            matcher.appendReplacement(sb, "URI=\"" + Matcher.quoteReplacement(proxy(kind(target), target, headersParam)) + "\"");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 洛视频：主列表里的子列表是裸行(非 # 开头)，此前一律当 ts 透传，子列表的 Content-Type 与 do 语义都不对；
     * 按后缀判定，列表仍走 do=m3u8（也才对得上 PlaySpec 的 HLS 识别）。
     */
    private static String kind(String url) {
        int index = url.indexOf('?');
        String path = index > 0 ? url.substring(0, index) : url;
        return path.toLowerCase(Locale.US).endsWith(".m3u8") ? PLAYLIST : SEGMENT;
    }

    private static String proxy(String type, String url, String headersParam) {
        String address = Server.get().getAddress("/proxy?do=" + type + "&url=" + Uri.encode(url));
        return TextUtils.isEmpty(headersParam) ? address : address + "&headers=" + Uri.encode(headersParam);
    }

    private static Map<String, String> decodeHeaders(String encoded) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (TextUtils.isEmpty(encoded)) return headers;
        try {
            String text = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            for (String line : text.split("\n")) {
                int index = line.indexOf(':');
                if (index > 0) headers.put(line.substring(0, index).trim(), Uri.decode(line.substring(index + 1).trim()));
            }
        } catch (Throwable ignored) {
        }
        return headers;
    }

    private static String encodeHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) sb.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
        return Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** 洛视频：上游连接必须随流结束释放，否则每段分片漏一条连接（1293 段的长片会拖垮连接池并拖慢首屏）。 */
    private static final class UpstreamStream extends InputStream {

        private final InputStream stream;
        private final Response response;
        private boolean closed;
        private long count;

        private UpstreamStream(InputStream stream, Response response) {
            this.stream = stream;
            this.response = response;
        }

        @Override
        public int read() throws IOException {
            int read = stream.read();
            if (read > 0) count++;
            else if (read < 0) close();
            return read;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int read = stream.read(buffer, offset, length);
            if (read > 0) count += read;
            else if (read < 0) close();
            return read;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            Log.d("MediaProxy", "ts stream closed bytes=" + count);
            try {
                stream.close();
            } finally {
                response.close();
            }
        }
    }
}
