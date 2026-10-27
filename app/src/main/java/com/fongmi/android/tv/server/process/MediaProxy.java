package com.fongmi.android.tv.server.process;

import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

import com.fongmi.android.tv.server.Server;
import com.github.catvod.net.OkHttp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

/**
 * 洛视频新增（播放器基础设施）：内置通用 m3u8/ts 代理。
 * do=m3u8 拉取播放列表并把分片/嵌套列表/KEY/MAP 的 URI 重写为本代理地址；do=ts 流式透传上游分片。
 * 替代加壳 jar 内部的同名代理（其内部异常会导致 500 空响应，真机实测）；不依赖 jar 上下文，对全部源生效。
 * 仅做地址重写与字节透传，不做任何内容加工（合规：与直播 m3u 解析同类的基础设施）。
 */
public class MediaProxy {

    private static final Pattern URI_ATTR = Pattern.compile("URI=\"([^\"]+)\"");

    public static boolean matches(Map<String, String> params) {
        String doParam = params.get("do");
        return "m3u8".equals(doParam) || "ts".equals(doParam);
    }

    public static Object[] handle(Map<String, String> params) throws Exception {
        String url = params.get("url");
        Log.d("MediaProxy", "handle do=" + params.get("do") + " url=" + url);
        if (TextUtils.isEmpty(url)) return error("missing url");
        Map<String, String> headers = decodeHeaders(params.get("headers"));
        if ("m3u8".equals(params.get("do"))) {
            try {
                String text = fetchText(url, headers);
                Log.d("MediaProxy", "upstream ok len=" + text.length());
                return new Object[]{200, "application/vnd.apple.mpegurl", new ByteArrayInputStream(rewrite(url, text, headers).getBytes(StandardCharsets.UTF_8))};
            } catch (Throwable t) {
                Log.d("MediaProxy", "m3u8 fail", t);
                return error("m3u8 " + t);
            }
        }
        return pass(url, headers);
    }

    private static Object[] error(String message) {
        return new Object[]{500, "text/plain", new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8))};
    }

    private static String fetchText(String url, Map<String, String> headers) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        headers.forEach(builder::header);
        try (Response res = OkHttp.client().newCall(builder.build()).execute()) {
            if (!res.isSuccessful()) throw new Exception("upstream " + res.code());
            return res.body().string();
        }
    }

    private static Object[] pass(String url, Map<String, String> headers) throws Exception {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        headers.forEach(builder::header);
        Response res = OkHttp.client().newCall(builder.build()).execute();
        InputStream stream = res.body().byteStream();
        String mime = res.body().contentType() == null ? "video/mp2t" : res.body().contentType().toString();
        return new Object[]{res.code(), mime, stream};
    }

    private static String rewrite(String base, String text, Map<String, String> headers) {
        URI ctx = URI.create(base);
        String headersParam = encodeHeaders(headers);
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) {
                sb.append('\n');
            } else if (t.startsWith("#")) {
                sb.append(rewriteTag(t, ctx, headersParam)).append('\n');
            } else {
                sb.append(proxy("ts", ctx.resolve(t).toString(), headersParam)).append('\n');
            }
        }
        return sb.toString();
    }

    private static String rewriteTag(String line, URI ctx, String headersParam) {
        if (!line.contains("URI=\"")) return line;
        Matcher matcher = URI_ATTR.matcher(line);
        StringBuffer sb = new StringBuffer(); // Android 的 Matcher.appendReplacement 仅有 StringBuffer 重载
        while (matcher.find()) {
            String target = ctx.resolve(matcher.group(1)).toString();
            String nested = target.contains(".m3u8") ? "m3u8" : "ts";
            matcher.appendReplacement(sb, "URI=\"" + Matcher.quoteReplacement(proxy(nested, target, headersParam)) + "\"");
        }
        matcher.appendTail(sb);
        return sb.toString();
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
}
