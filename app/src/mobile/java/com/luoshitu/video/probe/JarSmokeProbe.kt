package com.luoshitu.video.probe

import android.os.SystemClock
import com.fongmi.android.tv.api.loader.BaseLoader
import com.github.catvod.crawler.SpiderNull

/**
 * S1 冒烟闸门工具：加壳 jar 在 mobile flavor 下完成 Init.init → ≥1 个 csp_ 源真调用返回内容。
 * 在真机（arm64）上由家属模式（S2 接入 UI）或 adb 触发；jar 地址由家属填入，App 不内置任何配置（合规红线）。
 * x86_64 模拟器上加壳源全灭属预期（3.11-C），该环境不得用于冒烟判定。
 */
object JarSmokeProbe {

    data class Report(val ok: Boolean, val siteKey: String, val elapsedMs: Long, val detail: String, val homeJsonPreview: String? = null)

    fun run(jarUrl: String, siteKey: String, api: String): Report {
        val start = SystemClock.elapsedRealtime()
        return try {
            val spider = BaseLoader.get().getSpider(siteKey, api, "{}", jarUrl)
            if (spider is SpiderNull) {
                Report(false, siteKey, elapsed(start), "Init.init 已执行，但实例化静默降级为空实现（native 解密未就绪或缺类）")
            } else {
                val home = spider.homeContent(false)
                val ok = !home.isNullOrEmpty()
                Report(ok, siteKey, elapsed(start), if (ok) "真调用返回内容" else "homeContent 返回空",
                    home?.take(400))
            }
        } catch (t: Throwable) {
            Report(false, siteKey, elapsed(start), "异常：${t.javaClass.name}: ${t.message}")
        }
    }

    private fun elapsed(start: Long): Long = SystemClock.elapsedRealtime() - start
}
