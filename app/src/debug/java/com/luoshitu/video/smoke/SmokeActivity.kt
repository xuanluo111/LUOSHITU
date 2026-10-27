package com.luoshitu.video.smoke

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.fongmi.android.tv.App
import com.luoshitu.video.probe.JarSmokeProbe

/**
 * S1 冒烟闸门的真机执行器（仅 debug 构建注册）。
 * 手动输入家属配置所指向的 jar 地址与任一 csp_ 站点 key，执行 Init.init → 真调用 → 报告。
 * App 不预置任何地址（合规红线）；arm64 真机专用，x86_64 模拟器结果无效（3.11-C）。
 */
class SmokeActivity : Activity() {

    private lateinit var jarInput: EditText
    private lateinit var keyInput: EditText
    private lateinit var apiInput: EditText
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        jarInput = addField(root, "爬虫 jar 地址（家属配置所指向）")
        keyInput = addField(root, "站点 key（如 csp_Bili）")
        apiInput = addField(root, "api（如 csp_Bili）")
        val button = Button(this).apply { text = "运行冒烟" }
        root.addView(button)
        output = TextView(this).apply { setTextIsSelectable(true) }
        root.addView(output)
        setContentView(root)

        // 冒烟脚本自动化：extras 预填 + auto=1 自动执行（地址仍来自脚本参数，App 不预置任何配置）
        intent.getStringExtra("jar")?.let { jarInput.setText(it) }
        intent.getStringExtra("key")?.let { keyInput.setText(it) }
        intent.getStringExtra("api")?.let { apiInput.setText(it) }
        val auto = intent.getBooleanExtra("auto", false)

        val runner = {
            output.text = "运行中…（下载 jar 可能需要几分钟）"
            button.isEnabled = false
            val jar = jarInput.text.toString().trim()
            val key = keyInput.text.toString().trim().ifEmpty { "smoke" }
            val api = apiInput.text.toString().trim()
            Thread {
                val report = JarSmokeProbe.run(jar, key, api)
                Log.i(TAG, "SMOKE_RESULT ok=${report.ok} siteKey=${report.siteKey} elapsedMs=${report.elapsedMs} detail=${report.detail}")
                runOnUiThread {
                    button.isEnabled = true
                    output.text = listOf(
                        "结果: ${if (report.ok) "通过 ✓" else "失败 ✗"}",
                        "siteKey: ${report.siteKey}",
                        "耗时: ${report.elapsedMs} ms",
                        "说明: ${report.detail}",
                        report.homeJsonPreview?.let { "返回预览: $it" },
                    ).filterNotNull().joinToString("\n")
                }
            }.start()
        }
        button.setOnClickListener { runner() }
        if (auto) App.post({ runner() }, 800)
    }

    private fun addField(root: LinearLayout, hint: String): EditText {
        val label = TextView(this).apply { text = hint }
        val field = EditText(this).apply { setSingleLine(true) }
        root.addView(label)
        root.addView(field)
        (field.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin = 8
        return field
    }

    companion object {
        private const val TAG = "JarSmoke"
    }
}
