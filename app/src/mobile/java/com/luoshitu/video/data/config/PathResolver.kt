package com.luoshitu.video.data.config

/**
 * 3.5 相对路径解析（单一实现，3.5 要求全项目禁止各处自行拼接）。
 * base = 配置地址所在目录；支持 ./ ../ 与裸相对路径；绝对地址原样返回。
 * 纯 JVM 字符串实现（不做 URL 重编码），单测覆盖（S1 DoD）。
 *
 * 基座内部 Decoder.fix() 的既有拼接保持原样（提示词 2.2 不做无关重构）；
 * 我方新增代码（家属导入、体检、下载）一律经此解析。
 */
object PathResolver {

    private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")

    fun resolve(base: String, path: String?): String {
        if (path.isNullOrBlank()) return base
        val target = path.trim()
        if (SCHEME.containsMatchIn(target)) return target

        val schemeEnd = base.indexOf("://") + 3
        val firstSlash = base.indexOf('/', schemeEnd)
        val root = if (firstSlash < 0) base else base.substring(0, firstSlash)

        var ref = target
        if (ref.startsWith("/")) return "$root$ref"

        var depth = 0
        while (true) {
            when {
                ref.startsWith("../") -> { depth++; ref = ref.removePrefix("../") }
                ref.startsWith("./") -> ref = ref.removePrefix("./")
                else -> break
            }
        }
        if (ref.isEmpty()) return base

        val dir = if (firstSlash < 0) "$root/" else base.substringBeforeLast('/') + "/"
        val segments = dir.removePrefix("$root/").split('/').filter { it.isNotEmpty() }.toMutableList()
        repeat(depth) { if (segments.isNotEmpty()) segments.removeAt(segments.size - 1) }

        return buildString {
            append(root)
            segments.forEach { append('/').append(it) }
            if (ref.endsWith("/")) {
                // 相对路径指向目录时，把配置文件落在该目录下（保持与 base 一致）
                append('/').append(ref.removeSuffix("/"))
                append("/config.json")
            } else {
                append('/').append(ref)
            }
        }
    }
}
