package com.luoshitu.video.family;

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.EditText
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.fongmi.android.tv.App
import com.fongmi.android.tv.api.loader.BaseLoader
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.db.AppDatabase
import com.fongmi.android.tv.db.dao.ConfigDao
import com.fongmi.android.tv.impl.Callback
import com.github.catvod.utils.Prefers
import com.fongmi.android.tv.utils.UrlUtil
import com.luoshitu.video.data.config.ContentGuard
import com.luoshitu.video.data.db.LsDatabase
import com.luoshitu.video.data.db.SiteHealthEntity
import com.luoshitu.video.data.error.ElderError
import com.luoshitu.video.family.LocalFileScan
import com.luoshitu.video.probe.SiteHealthRunner
import com.fongmi.android.tv.api.config.VodConfig
import com.github.catvod.net.Download
import com.github.catvod.net.OkHttp

/**
 * 家属模式配置页（F1 配置注入 MUST）：粘贴地址 / Download 目录扫描导入 / 多配置管理与导入 / 体检与诊断 / 联系方式。
 * 入口仅家属可见（结构性，不靠提示拦截）。老人端不存在任何配置入口。
 */
class FamilyConfigActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var pasteUrl: EditText
    private lateinit var configRows: ArrayList<LinearLayout>
    private lateinit var dirList: LinearLayout
    private lateinit var diagText: TextView
    private lateinit var contactSection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
        }
        rootAdd(buildSection("粘贴配置地址"))
        pasteUrl = EditText(applicationContext).apply { setHint("粘贴节目单地址（例如 http://...）") }
        rootAdd(pasteUrl)
        pasteUrl.setOnClickListener { importByPaste() }
        refreshConfigRows()
        rootAdd(buildSection("从 Download/ 目录发现节目单", dirList))
        dirList.setOnClickListener { importByDir() }
        rootAdd(buildSection("已保存的节目单（老人端即时可见，支持多个）"))
        rootAdd(buildSection("体检与诊断（仅家属可见，不弹给老人）", diagText))
        diagText.setOnClickListener { refreshDiagnosis() }
        rootAdd(buildSection("联系家人（「遇到问题？点这里」时显示）", contactSection))
    }

    private fun buildSection(title: String, dirList: LinearLayout): LinearLayout {
        val card = cardBuilder(title)
        dirList.addView(card)
        return card
    }

    private fun buildSection(title: String, vararg children: android.view.View): LinearLayout {
        val card = cardBuilder(title)
        children.forEach { card.addView(it) }
        return card
    }

    private fun buildSection(title: String, pasteUrl: EditText): EditText {
        pasteUrl.setClickable(true)
        return pasteUrl
    }

    private fun cardBuilder(title: String): LinearLayout {
        return LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private fun rootAdd(card: android.view.View) {
        root.addView(card)
    }

    private fun contactSection(): LinearLayout {
        val section = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        section.addView(TextView(applicationContext).apply { text = "家人称谓（「遇到问题？点这里」时显示）"; setTextColor(0xFF3C3C3C.toInt()) })
        val nameInput = EditText(applicationContext).apply { setHint("如：张阿姨") }
        section.addView(nameInput)
        section.addView(TextView(applicationContext).apply { text = "联系电话"; setTextColor(0xFF3C3C3C.toInt()) })
        val phoneInput = EditText(applicationContext).apply { setHint("如：138xxxxxxxx") }
        section.addView(phoneInput)
        return section
    }

    private fun refreshConfigRows() {
        configRows = ArrayList<LinearLayout>()
        configRows.clear()
        for (item in AppDatabase.get().getConfigDao().findAll()) {
            addConfigRow(item.name ?: item.url, item.url)
        }
    }

    private fun addConfigRow(name: String, url: String) {
        if (url.isNullOrBlank()) return
        val row = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        row.addView(TextView(applicationContext).apply { text = name ?: url; setTextSize(18f) })
        val importView = TextView(applicationContext).apply { text = "导入"; setTextSize(18f) }
        val deleteView = TextView(applicationContext).apply { text = "删掉"; setTextSize(18f) }
        importView.setOnClickListener { importConfig(App.get(), url) }
        deleteView.setOnClickListener {
            try {
                AppDatabase.get().getConfigDao().delete(url)
                Toast.makeText(this, "已删除这个节目单（家人可重新导入）", android.view.View.VISIBLE).show()
            } catch (t: Throwable) { TextLogger(ElderError.from(t)) }
        }
        row.addView(importView)
        row.addView(deleteView)
        configRows.add(row)
    }

    private fun refreshDiagnosis() {
        val dao = LsDatabase.get(this)
        val rows = dao.getSiteHealthDao().findAll()
        val ok = rows.count { it.ok }
        val bad = rows.filter { !it.ok }
        diagText.text = buildDiagnosisText(rows, ok)
    }

    private fun buildDiagnosisText(rows: List<SiteHealthEntity>, okCount: Int): String {
        val bad = rows.filter { !it.ok }
        return """
           洛视频 配置诊断（家属）
           体检结果: ${okCount}/${rows.size} 可用
           不可用来源: ${if (bad.isEmpty()) "无" else bad.map { "\$it.siteKey" }.joinToString("，")}
           最近失败原因（口语化）: ${if (bad.isEmpty()) "全部正常" else bad.first { !it.ok }.reasonTech}
        """.trimIndent()
    }

    private fun loadHealthy() {
        SiteHealthRunner.run(this)
    }

    private fun importByPaste() {
        val url = pasteUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "请先粘贴配置地址", android.view.View.VISIBLE).show()
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "请粘贴 http 或 https 开头的配置地址", android.view.View.VISIBLE).show()
            return
        }
        showLoading("正在准备节目，请稍候…")
        runCatching {
            importConfig(App.get(), url)
        }
        showLoading("")
        loadHealthy()
    }

    private fun importConfig(ctx: android.content.Context, url: String) {
        try {
            val text = try { OkHttp.string(url) } catch (t: Throwable) { null } ?: ""
            if (text.isBlank()) { TextLogger(ElderError.NotConfigFormat("地址返回空内容")); return }
            val verdict = ContentGuard.check(ContentGuard.ResourceType.CONFIG, text.toByteArray(Charsets.UTF_8))
            if ((verdict as? ContentGuard.Verdict.Reject) != null) {
                TextLogger((verdict as ContentGuard.Verdict.Reject).error)
                return
            }
            val config = Config.create(0, url)
            config.json = text
            config.name = siteName(url)
            config.save()
            VodConfig.load(config, com.fongmi.android.tv.impl.Callback())
            showLoading("")
            refreshDiagnosis()
        } catch (t: Throwable) {
            TextLogger(ElderError.from(t))
        }
    }

    private fun importByDir() {
        showLoading("正在准备节目，请稍候…")
        runCatching {
            val files = LocalFileScan.scan(this)
            if (files.isEmpty()) {
                TextLogger(ElderError.NoAvailableSite("没找到配置文件，请把 json/txt/jar/js 文件放进 Download/ 目录"))
                return
            }
            Toast.makeText(this, "发现 ${files.size} 个文件，正在准备节目", android.view.View.VISIBLE).show()
            val json = files.firstOrNull { it.endsWith(".json") || it.endsWith(".txt") } ?: ""
            val jar = files.firstOrNull { it.endsWith(".jar") || it.endsWith(".js") } ?: ""
            if (!json.isEmpty()) importConfig(App.get(), json)
            if (!jar.isEmpty()) BaseLoader.get().dex(java.io.File(jar).absolutePath)
            showLoading("")
            refreshDiagnosis()
        }
    }

    private fun saveContact() {
        Prefers.put("luoshitu_contact_name", pasteUrl.text.toString().trim())
        Prefers.put("luoshitu_contact_phone", pasteUrl.text.toString().trim())
        Toast.makeText(this, "联系方式已保存", android.view.View.VISIBLE).show()
    }

    private fun showLoading(text: String) {
        pasteUrl.setPadding(0, if (text.isNotEmpty()) 48 else 0, 0, 0)
    }

    private fun siteName(url: String): String = url.substringBeforeLast('/')

    private fun TextLogger(error: ElderError) {
        Toast.makeText(this, "家庭处理：" + error.family, android.view.View.VISIBLE).show()
    }
}
