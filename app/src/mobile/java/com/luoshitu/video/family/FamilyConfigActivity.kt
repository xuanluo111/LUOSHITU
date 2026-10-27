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
import android.util.Log
import com.github.catvod.net.OkHttp

/**
 * 家属模式配置页（F1 配置注入 MUST）：粘贴地址 / Download 目录扫描导入 / 多配置管理与导入 / 体检与诊断 / 联系方式。
 * 入口仅家属可见（结构性，不靠提示拦截）。老人端不存在任何配置入口。
 */
class FamilyConfigActivity : Activity() {

    private lateinit var root: android.widget.ScrollView
    private lateinit var content: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var pasteUrl: EditText
    private lateinit var configRows: ArrayList<LinearLayout>
    private lateinit var dirList: LinearLayout
    private lateinit var diagText: TextView
    private lateinit var contactSection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
        }
        root = android.widget.ScrollView(applicationContext).apply { addView(content) }
        dirList = LinearLayout(applicationContext)
        diagText = TextView(applicationContext).apply { setTextSize(16f); setTextColor(0xFF3C3C3C.toInt()) }
        contactSection = contactSection()
        statusText = TextView(applicationContext).apply { setTextSize(16f); setTextColor(0xFF1A73E8.toInt()); setPadding(0, 8, 0, 8) }
        rootAdd(statusText)
        rootAdd(buildSection("粘贴配置地址"))
        pasteUrl = EditText(applicationContext).apply {
            setHint("粘贴节目单地址（例如 http://...）")
            setSingleLine(true)
        }
        rootAdd(pasteUrl)
        val importBtn = TextView(applicationContext).apply {
            setText("导 入")
            setTextSize(20f)
            setTextColor(0xFF1A73E8.toInt())
            setPadding(0, 24, 0, 8)
        }
        rootAdd(importBtn)
        importBtn.setOnClickListener { importByPaste() }
        refreshConfigRows()
        rootAdd(buildSection("从 Download/ 目录发现节目单", dirList))
        dirList.setOnClickListener { importByDir() }
        rootAdd(buildSection("已保存的节目单（老人端即时可见，支持多个）"))
        rootAdd(buildSection("体检与诊断（仅家属可见，不弹给老人）", diagText))
        diagText.setOnClickListener { refreshDiagnosis() }
        rootAdd(buildSection("联系家人（「遇到问题？点这里」时显示）", contactSection))
        rootAdd(buildSection("家属口令（保护本页：建议设为手机号后 4 位）", passgateSection()))
        setContentView(root)
    }

    private fun buildSection(title: String, dirList: LinearLayout): LinearLayout {
        val card = cardBuilder(title)
        dirList.addView(card)
        return card
    }

    private fun buildSection(title: String, vararg children: android.view.View): LinearLayout {
        val card = cardBuilder(title)
        children.forEach {
            (it.parent as? android.view.ViewGroup)?.removeView(it)
            card.addView(it)
        }
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 32 }
        }
    }

    private fun rootAdd(card: android.view.View) {
        (card.parent as? android.view.ViewGroup)?.removeView(card)
        content.addView(card)
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
        importView.setOnClickListener { showLoading("正在导入该节目单…"); doImport(url) }
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
        Thread {
            try {
                val dao = LsDatabase.get(this)
                val rows = dao.getSiteHealthDao().findAll()
                val ok = rows.count { it.ok }
                runOnUiThread { diagText.text = buildDiagnosisText(rows, ok) }
            } catch (t: Throwable) {
                Log.d("FamilyImport", "diagnosis fail $t")
            }
        }.start()
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
        Thread { runCatching { SiteHealthRunner.run(this@FamilyConfigActivity) } }.start()
    }

    private fun importByPaste() {
        val url = pasteUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "请先粘贴配置地址", Toast.LENGTH_LONG).show()
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "请粘贴 http 或 https 开头的配置地址", Toast.LENGTH_LONG).show()
            return
        }
        showLoading("正在准备节目，请稍候…（下载与校验约需 1-2 分钟）")
        Thread {
            doImport(url)
        }.start()
    }

    /** 后台线程执行：网络/校验/入库；UI 结果经主线程回调 */
    private fun doImport(url: String) {
        fun ui(block: () -> Unit) { runOnUiThread(block) }
        Log.d("FamilyImport", "doImport start $url")
        try {
            // 海外 CDN 握手慢且时好时坏：25s 超时 + 最多 3 次尝试
            var downloaded: String? = null
            for (attempt in 1..3) {
                if (downloaded != null) break
                try {
                    OkHttp.newCall(OkHttp.client(25000), url).execute().use { res ->
                        val body = res.body.string()
                        Log.d("FamilyImport", "download done attempt=$attempt code=${res.code} len=${body.length}")
                        if (res.isSuccessful && body.isNotBlank()) downloaded = body
                    }
                } catch (t: Throwable) {
                    Log.d("FamilyImport", "download fail attempt=$attempt $t")
                }
            }
            val finalText: String = downloaded ?: ""
            if (finalText.isBlank()) {
                ui { showLoading(""); Toast.makeText(this, "这个地址返回的是空内容，家人确认一下地址对不对", Toast.LENGTH_LONG).show() }
                return
            }
            val verdict = ContentGuard.check(ContentGuard.ResourceType.CONFIG, finalText.toByteArray(Charsets.UTF_8))
            val reject = verdict as? ContentGuard.Verdict.Reject
            if (reject != null) {
                ui { showLoading(""); Toast.makeText(this, reject.error.family, Toast.LENGTH_LONG).show() }
                return
            }
            Log.d("FamilyImport", "guard ok, saving config")
            // 慢网适配：下载成功的配置写入进程级缓存，基座重复加载同 URL 直接命中（DecoderCache）
            com.github.catvod.utils.DecoderCache.put(url, finalText)
            val config = Config.create(0, url)
            config.json = finalText
            config.name = siteName(url)
            config.save()
            Log.d("FamilyImport", "config saved, calling VodConfig.load")
            ui {
                VodConfig.load(config, object : com.fongmi.android.tv.impl.Callback() {
                    override fun success() {
                        showLoading("")
                        Toast.makeText(this@FamilyConfigActivity, "节目单已就绪，回「找片」看电视", Toast.LENGTH_LONG).show()
                        Log.d("FamilyImport", "VodConfig success")
                        loadHealthy()
                        refreshDiagnosis()
                    }

                    override fun error(msg: String?) {
                        Log.d("FamilyImport", "VodConfig error: $msg")
                        showLoading("")
                        Toast.makeText(this@FamilyConfigActivity, "节目单还没准备好：${msg ?: "内容方暂时没有回应"}", Toast.LENGTH_LONG).show()
                        refreshDiagnosis()
                    }
                })
            }
        } catch (t: Throwable) {
            ui { showLoading(""); Toast.makeText(this, ElderError.from(t).family, Toast.LENGTH_LONG).show() }
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
            if (!json.isEmpty()) doImport(json)
            if (!jar.isEmpty()) BaseLoader.get().dex(java.io.File(jar).absolutePath)
            showLoading("")
            refreshDiagnosis()
        }
    }

    private fun passgateSection(): android.view.View {
        val section = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
        }
        val input = EditText(applicationContext).apply { setHint("设置口令（如手机号后 4 位）") }
        section.addView(input)
        val btn = TextView(applicationContext).apply {
            setText("设置口令")
            setTextSize(18f)
            setTextColor(0xFF1A73E8.toInt())
            setPadding(0, 16, 0, 0)
        }
        btn.setOnClickListener {
            val value = input.text.toString().trim()
            if (value.isEmpty()) {
                Toast.makeText(this, "先输入口令内容", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            FamilyPassgate.enter(value)
            Toast.makeText(this, "口令已保存，下次从「我的」长按标题进入需要口令", Toast.LENGTH_LONG).show()
        }
        section.addView(btn)
        return section
    }

    private fun saveContact() {
        Prefers.put("luoshitu_contact_name", pasteUrl.text.toString().trim())
        Prefers.put("luoshitu_contact_phone", pasteUrl.text.toString().trim())
        Toast.makeText(this, "联系方式已保存", android.view.View.VISIBLE).show()
    }

    private fun showLoading(text: String) {
        statusText.text = text
    }

    private fun siteName(url: String): String = url.substringBeforeLast('/')

    private fun TextLogger(error: ElderError) {
        Toast.makeText(this, "家庭处理：" + error.family, android.view.View.VISIBLE).show()
    }
}
