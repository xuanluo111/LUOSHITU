package com.fongmi.android.tv.player.exo;

import androidx.media3.common.PlaybackException;
import androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException;

import com.github.catvod.Proxy;

import java.nio.charset.StandardCharsets;

public class ExoErrorMessageProvider {

    // 洛视频：错误提示必须说人话。此前这里全是英文（Network Connection Failed 等），
    // 适老化用户根本看不懂，只能干瞪眼。统一改成中文短句；实在没映射到的错误码
    // 才回退到 media3 给的原名，避免编造误导性的描述。
    public String get(PlaybackException e) {
        return switch (e.errorCode) {
            case PlaybackException.ERROR_CODE_TIMEOUT -> "连接超时";
            case PlaybackException.ERROR_CODE_UNSPECIFIED -> "播放出错";
            case PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK -> "播放环境异常";
            case PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "读取失败";
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> httpError(e);
            case PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> "返回内容不是视频";
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "网络连接失败";
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "网络连接超时";
            case PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE -> "超出视频范围";
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> "播放列表格式错误";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> "视频文件格式错误";
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> "不支持的播放列表";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "不支持该视频格式";
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "解码器启动失败";
            case PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> "解码器查询失败";
            case PlaybackException.ERROR_CODE_DECODING_FAILED -> "解码失败";
            case PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> "不支持该视频编码";
            case PlaybackException.ERROR_CODE_DECODING_RESOURCES_RECLAIMED -> "解码资源被系统回收";
            case PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> "视频超出设备解码能力";
            case PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> "声音初始化失败";
            case PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED -> "声音输出失败";
            case PlaybackException.ERROR_CODE_DRM_UNSPECIFIED -> "版权保护校验失败";
            case PlaybackException.ERROR_CODE_DRM_SYSTEM_ERROR -> "版权保护校验失败";
            case PlaybackException.ERROR_CODE_DRM_CONTENT_ERROR -> "版权保护校验失败";
            case PlaybackException.ERROR_CODE_DRM_DEVICE_REVOKED -> "版权保护校验失败";
            case PlaybackException.ERROR_CODE_DRM_LICENSE_EXPIRED -> "版权授权已过期";
            case PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED -> "版权保护校验失败";
            case PlaybackException.ERROR_CODE_DRM_DISALLOWED_OPERATION -> "版权限制不允许播放";
            case PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> "版权授权获取失败";
            default -> e.getErrorCodeName();
        };
    }

    private String httpError(PlaybackException e) {
        for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
            if (!(cause instanceof InvalidResponseCodeException error)) continue;
            String message = "服务器返回 " + error.responseCode;
            String detail = proxyMessage(error);
            return detail.isEmpty() ? message : message + "：" + detail;
        }
        return "服务器返回异常状态";
    }

    private String proxyMessage(InvalidResponseCodeException error) {
        if (!error.dataSpec.uri.toString().startsWith(Proxy.getUrl(true) + "?")) return "";
        boolean marked = error.headerFields.entrySet().stream().anyMatch(entry -> "X-TV-Proxy-Error".equalsIgnoreCase(entry.getKey()) && entry.getValue().contains("1"));
        boolean plain = error.headerFields.entrySet().stream().anyMatch(entry -> "Content-Type".equalsIgnoreCase(entry.getKey()) && entry.getValue().stream().anyMatch(value -> "text/plain".equalsIgnoreCase(value.split(";", 2)[0].trim())));
        if (!marked || !plain) return "";
        String message = new String(error.responseBody, 0, Math.min(error.responseBody.length, 1024), StandardCharsets.UTF_8).replaceAll("https?://\\S+", "[URL]").replaceAll("\\s+", " ").trim();
        return message.substring(0, Math.min(message.length(), 240));
    }
}
