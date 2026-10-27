package com.github.catvod.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 洛视频新增（慢网适配）：配置文本的进程级缓存。
 * 家属导入时已成功下载的配置，供 Decoder.load 同 URL 命中，避免基座重试加载时再次穿越不稳定外网。
 * 仅进程内生效，不落盘；App 不预置任何内容（合规红线）。
 */
public class DecoderCache {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static void put(String url, String text) {
        if (url != null && text != null && !text.isEmpty()) CACHE.put(url, text);
    }

    public static String get(String url) {
        return url == null ? null : CACHE.get(url);
    }
}
