package com.luoshitu.video.data.config

import com.luoshitu.video.data.error.ElderError
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 3.7 反静默失败校验层。本生态的 CDN 对任意不存在的路径返回 HTTP 200 + 配置 JSON 原文，
 * 只看状态码会把 JSON 当 jar/JS 静默加载，表现为"点了没反应"。所有下载与导入内容必须先过此层。
 * 纯 JVM 实现，不依赖 Android 类，便于单测（S1 DoD）。
 */
object ContentGuard {

    enum class Expected { CONFIG, JAR, JS }

    /** 诊断页记录条目：期望类型 / 实际 content-type / 实际字节数（3.7） */
    data class Record(val expected: Expected, val contentType: String?, val bytes: Long, val md5: String)

    sealed interface Verdict {
        data class Ok(val record: Record) : Verdict
        data class Reject(val error: ElderError, val record: Record) : Verdict
    }

    private val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    fun check(
        expected: Expected,
        bytes: ByteArray,
        contentType: String? = null,
        contentLength: Long = -1,
        configMd5: String? = null,
    ): Verdict {
        val record = Record(expected, contentType, bytes.size.toLong(), md5(bytes))

        // catch-all 回落：响应体与配置本体逐字节相同 ⇒ 资源不存在（3.7 / A6）
        if (configMd5 != null && record.md5.equals(configMd5, ignoreCase = true)) {
            return Verdict.Reject(ElderError.ResourceMissing("响应体与配置内容完全相同"), record)
        }

        return when (expected) {
            Expected.JAR -> checkJar(bytes, contentLength, record)
            Expected.JS -> checkJs(bytes, record)
            Expected.CONFIG -> checkConfig(bytes, record)
        }
    }

    private fun checkJar(bytes: ByteArray, contentLength: Long, record: Record): Verdict {
        // 魔数校验（A45 半截 jar 的第一道闸）
        if (bytes.size < ZIP_MAGIC.size || !bytes.copyOfRange(0, 4).contentEquals(ZIP_MAGIC)) {
            return Verdict.Reject(ElderError.JarInvalid("文件开头不是 ZIP 魔数（通常是服务器返回了网页或 JSON）"), record)
        }
        // 体积与声明一致（忽略 Range 的服务器全量重发时由此拦下截断文件）
        if (contentLength > 0 && bytes.size.toLong() != contentLength) {
            return Verdict.Reject(ElderError.JarInvalid("下载不完整：得到 ${bytes.size} 字节，声明 $contentLength 字节"), record)
        }
        // 条目逐个可读（3.11-E：半截 jar 的首个 dex 可能"看起来完整"而 .guard 缺失）
        if (!zipEntriesReadable(bytes)) {
            return Verdict.Reject(ElderError.JarInvalid("ZIP 条目损坏或文件被截断"), record)
        }
        return Verdict.Ok(record)
    }

    private fun zipEntriesReadable(bytes: ByteArray): Boolean {
        var entries = 0
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                while (true) {
                    val entry: ZipEntry = zip.nextEntry ?: break
                    entries++
                    while (zip.read(ByteArray(8192)) != -1) { /* 消费流，CRC/截断错误在此抛出 */ }
                    zip.closeEntry()
                }
            }
        } catch (e: Exception) {
            return false
        }
        return entries > 0
    }

    private fun checkJs(bytes: ByteArray, record: Record): Verdict {
        // JS 源文件不存在时，catch-all 典型返回配置 JSON 原文 ⇒ 首字节为 { 或 [；网页则首字节为 <
        val first = bytes.firstNonWhitespaceOrNull()
        return if (first == null || first == '{' || first == '[' || first == '<') {
            Verdict.Reject(ElderError.ResourceMissing("JS 文件不存在（服务器返回了 JSON 或网页）"), record)
        } else {
            Verdict.Ok(record)
        }
    }

    private fun checkConfig(bytes: ByteArray, record: Record): Verdict {
        val text = bytes.toString(Charsets.UTF_8).trim()
        val looksJson = text.startsWith("{") || text.startsWith("[")
        val looksTxt = text.contains("【")
        return if (looksJson || looksTxt) Verdict.Ok(record)
        else Verdict.Reject(ElderError.NotConfigFormat("内容既不是 JSON 也不是 TXT 多仓格式"), record)
    }

    private fun ByteArray.firstNonWhitespaceOrNull(): Char? {
        for (b in this) {
            if (b != 0x20.toByte() && b != 0x09.toByte() && b != 0x0A.toByte() && b != 0x0D.toByte()) return b.toInt().toChar()
        }
        return null
    }

    fun md5(bytes: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }
}
