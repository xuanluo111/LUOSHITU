package com.luoshitu.video.probe

import android.content.Context
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.api.loader.BaseLoader
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.db.dao.ConfigDao
import com.luoshitu.video.data.db.LsDatabase
import com.luoshitu.video.data.db.SiteHealthEntity
import com.luoshitu.video.data.error.ElderError
import com.luoshitu.video.probe.SiteHealthProbe

/**
 * 配置加载后的逐源体检编排（F1⑤，提示词 3.11-D）。
 * 判据按 type 定义：type:3 只能用真实调用 Proxy.proxy(act=home/category) 判定，严禁用 loadClass 是否成功
 * （加壳 jar 的类要等 native 解密后才出现）；探测必须发生在 Init.init(Context) 之后（JarLoader 内部天然满足）。
 * 体检结果写入独立 Room 库 SiteHealth（与基座库隔离，见 S1），驱动未就绪态与诊断页（10.4）。
 */
object SiteHealthRunner {

    data class RunReport(var okSites: Int = 0, var totalSites: Int = 0, var unavailable: MutableList<String> = mutableListOf())

    /** 在配置加载完成后执行全部体检 */
    fun run(ctx: Context, liveKeys: List<String> = emptyList()): RunReport {
        val report = RunReport()
        val dao = LsDatabase.get(ctx)
        val sites = try { VodConfig.get().getSites().map { it.key } } catch (t: Throwable) { emptyList() }
        val lives = try { LiveConfig.get().getLives().map { it.getName() } } catch (t: Throwable) { emptyList() }
        for (key in sites + liveKeys) {
            val entity = probe(key)
            dao.getSiteHealthDao().upsert(listOf(entity))
            if (entity.ok) report.okSites++ else report.unavailable.add(key)
        }
        report.totalSites = sites.size + liveKeys.size
        return report
    }

    private fun probe(key: String): SiteHealthEntity = try {
        val site = Site().apply { setKey(key); setName(key); setApi(liveUrl(key)); setJar("") }
        site.setType(3)
        SiteHealthProbe.probe(site)
    } catch (t: Throwable) {
        val err = ElderError.Unknown("体检异常: ${t.javaClass.simpleName}: ${t.message}")
        SiteHealthEntity(key).apply { ok = false; reasonKind = err.kind.name; reasonTech = err.tech }
    }

    private fun liveUrl(key: String): String = try {
        LiveConfig.get().getLive(key).url
    } catch (t: Throwable) { "" }
}
