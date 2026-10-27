package com.luoshitu.video.probe

import android.os.SystemClock
import com.fongmi.android.tv.api.loader.BaseLoader
import com.fongmi.android.tv.bean.Site
import com.github.catvod.crawler.SpiderNull
import com.github.catvod.net.OkHttp
import com.luoshitu.video.data.error.ElderError
import com.luoshitu.video.data.db.SiteHealthEntity

/**
 * F1⑤ 逐源可用性体检。判据按 type 分定义（3.11-D：type:3 只能用真实调用判定，
 * 严禁用 loadClass 是否成功判定——加壳 jar 的类要等 native 解密后才出现；
 * 本探针经 BaseLoader.getSpider 触发 jar 下载与 Init.init，天然满足"探测在 Init.init 之后"）。
 * 并发调度与超时隔离由调用方（S2 家属模式/诊断流程）负责；本类只做单源同步探测。
 */
object SiteHealthProbe {

    fun probe(site: Site): SiteHealthEntity {
        val start = SystemClock.elapsedRealtime()
        val result = runCatching { check(site) }
        val entity = SiteHealthEntity(site.key)
        entity.name = site.name
        entity.type = site.type
        entity.checkedAt = System.currentTimeMillis()
        entity.durationMs = SystemClock.elapsedRealtime() - start
        result.fold(
            onSuccess = { entity.ok = it == null; entity.reasonKind = it?.kind?.name; entity.reasonTech = it?.tech },
            onFailure = { t ->
                val error = ElderError.from(t)
                entity.ok = false
                entity.reasonKind = error.kind.name
                entity.reasonTech = error.tech
            },
        )
        return entity
    }

    /** 返回 null 表示可用；返回 ElderError 表示该源不可用及其分级原因 */
    private fun check(site: Site): ElderError? = when (site.type) {
        3 -> probeSpider(site)
        0, 1 -> probeCms(site)
        else -> ElderError.ModuleMissing("站点 type=${site.type} 本版本不支持")
    }

    private fun probeSpider(site: Site): ElderError? {
        if (site.jar.isEmpty() && site.api.endsWith(".js")) return probeJs(site)
        val spider = BaseLoader.get().getSpider(site.key, site.api, site.ext, site.jar)
        if (spider is SpiderNull) {
            return ElderError.JarClassMissing("站点 ${site.key} 静默降级为空实现（类缺失或解密未就绪）", listOf(site.key))
        }
        val home = runCatching { spider.homeContent(false) }
        return when {
            home.isFailure -> ElderError.from(home.exceptionOrNull()!!)
            home.getOrNull().isNullOrEmpty() -> ElderError.NoAvailableSite("站点 ${site.key} homeContent 返回空")
            else -> null
        }
    }

    private fun probeJs(site: Site): ElderError? {
        // JS 源先做 3.7 内容校验，防止把 catch-all 返回的 JSON 喂给引擎
        return try {
            val bytes = OkHttp.string(site.api).toByteArray(Charsets.UTF_8)
            val verdict = com.luoshitu.video.data.config.ContentGuard.check(
                com.luoshitu.video.data.config.ContentGuard.ResourceType.JS, bytes,
            )
            (verdict as? com.luoshitu.video.data.config.ContentGuard.Verdict.Reject)?.error
                ?: ElderError.ModuleMissing("JS 源需要在装载后真调用验证（QuickJS 内嵌完成于 S3）")
        } catch (t: Throwable) {
            ElderError.from(t)
        }
    }

    private fun probeCms(site: Site): ElderError? {
        val url = if (site.api.endsWith("/")) "${site.api}?ac=videolist" else "${site.api}/?ac=videolist"
        return try {
            val body = OkHttp.string(url)
            if (body.isBlank()) ElderError.NoAvailableSite("CMS 源 ${site.key} 返回空")
            else null
        } catch (t: Throwable) {
            ElderError.from(t)
        }
    }
}
