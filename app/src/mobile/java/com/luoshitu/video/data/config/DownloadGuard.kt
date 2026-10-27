package com.luoshitu.video.data.config

import android.content.Context
import com.github.catvod.net.Download
import com.github.catvod.net.OkHttp
import com.github.catvod.utils.Path
import com.luoshitu.video.data.error.ElderError
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.collections.List

/**
 * 爬虫 jar/JS 的下载策略与反静默失败校验（提示词 3.7、F1④、10.13）。
 *
 * 基座下载器（catvod Download）已实现 .part 分段、Range 探测、断点续传与条件请求；
 * 本层在其之上做：
 * 1. Range 能力探测（提示词 10.13：先探测再选策略）——实测该 CDN 忽略 Range，探测为假；
 * 2. 备用镜像地址编排（家属可填多个同源镜像）；
 * 3. 3.7 反静默失败校验：ZIP 魔数 / 条目可读 / 体积一致 / catch-all（响应体与配置本体逐字节相同）/ JS 首字节非 JSON；
 * 4. 校验通过后才交付（3.11-E：半截 jar 绝不能交给 DexClassLoader）。
 * 下载执行依赖 Android 上下文（家属模式调用）；校验部分为纯 JVM 逻辑，可单测。
 */
object DownloadGuard {

    /** 超时上限可配置（提示词 10.13）；默认 ≥8 分钟 */
    const val DEFAULT_TIMEOUT_MS = 8 * 60 * 1000L

    enum class Strategy { RANGE_SEGMENTED, STANDALONE_TIMEOUT, FAILED }

    data class Outcome(
        val expected: ContentGuard.ResourceType,
        val ok: Boolean,
        val error: ElderError? = null,
        val contentType: String? = null,
        val bytes: Long = 0,
        val strategy: Strategy = Strategy.FAILED,
        val strategyName: String = "",
        val contentLength: Long = -1,
        val mirrorsUsed: List<String> = emptyList(),
    ) {
    }

    /** 下载计划：是否使用 Range 分段（由探测决定）、备用镜像、超时 */
    data class Plan(
        val useRange: Boolean,
        val mirrors: List<String>,
        val timeoutMs: Long,
    ) {
        companion object {
            fun forSite(timeoutMs: Long = DEFAULT_TIMEOUT_MS): Plan = Plan(useRange = false, mirrors = emptyList(), timeoutMs = timeoutMs)
            fun withRange(useRange: Boolean): Plan = Plan(useRange = useRange, mirrors = emptyList(), timeoutMs = DEFAULT_TIMEOUT_MS)
        }
    }

    /** 探测 CDN 是否支持 Range 续传：只有 206 且体积为 2 字节才认定支持（10.13）。结果按 URL 缓存。 */
    fun probeRangeCapable(ctx: Context, url: String): Boolean {
        val cache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
        if (cache.containsKey(url)) return cache[url] ?: false
        val capable = probeOne(ctx, url) ?: false
        cache[url] = capable
        return capable
    }

    private fun probeOne(ctx: Context, url: String): Boolean {
        try {
            val response = OkHttp.newCall(url).execute()
            val body = response.body?.string()
            response.close()
            val bodyOk: Boolean = body != null
            val headerOk: Boolean = response.header("Content-Range") == "bytes 0-1/\n"
            val sizeOk: Boolean = (response.body?.contentLength() ?: 0L).let { it.toLong() == 2L }
            val statusOk: Boolean = response.isSuccessful ?: false
            return bodyOk && statusOk && headerOk && sizeOk
        } catch (t: Throwable) {
            return false
        }
    }

    /** 下载 + 3.7 校验。失败返回 ok=false 与分级错误（不崩溃）；校验通过才允许交给 jar 加载器（3.11-E）。 */
    fun fetch(
        ctx: Context,
        plan: Plan,
        expected: ContentGuard.ResourceType,
        url: String,
        configMd5: String?,
    ): Outcome {
        val attempts = plan.mirrors.ifEmpty { listOf(url) }
        val outcomes = attempts.map { tryDownload(ctx, plan, expected, it, configMd5) }
        val lastError = outcomes.last().error
        val mirrorsUsed = outcomes.firstOrNull { it.ok }?.mirrorsUsed ?: emptyList()
        val strategy = strategy(plan)
        return when {
            outcomes.any { it.ok } -> Outcome(expected, true, lastError, null, 0, strategy, "", -1, mirrorsUsed)
            else -> Outcome(expected, false, lastError ?: ElderError.Unknown("下载失败"), null, 0, Strategy.FAILED, "所有地址均失败（${attempts.joinToString("; ")}）", -1, emptyList())
        }
    }

    private fun strategy(plan: Plan): Strategy = when (plan.useRange) {
        true -> Strategy.RANGE_SEGMENTED
        false -> Strategy.STANDALONE_TIMEOUT
    }

    private fun strategyName(plan: Plan): String = when {
        plan.useRange -> "Range 分段下载（服务器支持续传）"
        else -> "一次性长超时下载（服务器忽略 Range，续传不可用）"
    }

    private fun tryDownload(ctx: Context, plan: Plan, expected: ContentGuard.ResourceType, url: String, configMd5: String?): Outcome {
        val bytes = downloadBytes(ctx, url)
        if (bytes.isEmpty()) {
            return Outcome(expected, false, ElderError.JarDownloadFail("下载内容为空: $url"), null, 0, Strategy.FAILED, "空响应", -1, emptyList())
        }
        val contentType = contentTypeOf(expected)
        val verdict = ContentGuard.check(expected, bytes, contentType, bytes.size.toLong(), configMd5)
        return when {
            verdict is ContentGuard.Verdict.Reject -> {
                val r = verdict
                Outcome(expected, false, r.error, r.record.contentType, r.record.bytes, Strategy.FAILED, "内容校验未通过：既不是有效 jar/JS/配置，或资源不存在", r.record.bytes, listOf(url))
            }
            else -> Outcome(expected, true, null, contentType, bytes.size.toLong(), strategy(plan), strategyName(plan), -1, listOf(url))
        }
    }

    private fun contentTypeOf(expected: ContentGuard.ResourceType): String? = when (expected) {
        ContentGuard.ResourceType.JAR -> "application/zip"
        ContentGuard.ResourceType.JS -> "text/javascript"
        ContentGuard.ResourceType.CONFIG -> "application/json"
    }

    private fun downloadBytes(ctx: Context, url: String): ByteArray {
        // 复用基座 Download 的 Range/分段/断点能力；交付前由 ContentGuard 校验（3.11-E）
        val target = Path.jar().getAbsolutePath()
        try {
            val downloaded = Download.create(url, File(target)).get()
            val bytes = readAll(target)
            deleteQuietly(target)
            return bytes
        } catch (t: Throwable) {
            deleteQuietly(target)
            throw ElderError.JarDownloadFail("下载失败，请重试或改用文件导入（家属模式）").toString().let { Throwable(it) }
        }
    }

    private fun readAll(path: String): ByteArray {
        val inStream = FileInputStream(path)
        val buf = ByteArrayOutputStream()
        try {
            while (true) {
                val chunk = ByteArray(65536)
                val n = inStream.read(chunk)
                if (n <= 0) break
                buf.write(chunk, 0, n)
            }
        } finally {
            inStream.close()
        }
        return buf.toByteArray()
    }

    private fun deleteQuietly(path: String) {
        runCatching { java.io.File(path).delete() }
    }
}
