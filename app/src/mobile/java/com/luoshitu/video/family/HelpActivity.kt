package com.luoshitu.video.family;

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.fongmi.android.tv.ui.activity.HomeActivity
import com.github.catvod.utils.Prefers
import androidx.core.content.ContextCompat
import com.luoshitu.video.elder.ElderDesignTokens

/**
 * 求助页（F6）：老人界面的常驻「遇到问题？点这里」出口。
 * 展示家属预填的联系方式（一键拨号）+ 三个真实自助出口：关掉重开 / 把声音调大 / 换个节目看。
 */
class HelpActivity : Activity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
        }
        root.addView(title())
        root.addView(contactSection())
        root.addView(buildBigButton("一键拨号", Action.DIAL))
        root.addView(buildBigButton("关掉重新打开试试", Action.REOPEN))
        root.addView(buildBigButton("把声音调大", Action.VOLUME))
        root.addView(buildBigButton("换个节目看", Action.SWITCH))
    }

    private fun title(): View = TextView(applicationContext).apply {
        setText("遇到问题，点这里找家人")
        setTextSize(ElderDesignTokens.fontPageTitle.toFloat())
        setTextColor(ContextCompat.getColor(applicationContext, ElderDesignTokens.textPrimary))
    }

    private fun contactSection(): LinearLayout {
        val section = LinearLayout(applicationContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
        }
        section.addView(showLabel("家人联系方式"))
        section.addView(showContactValue(Prefers.getString("luoshitu_contact_name"), ElderDesignTokens.fontChannel))
        section.addView(showContactValue(Prefers.getString("luoshitu_contact_phone"), ElderDesignTokens.fontChannel))
        return section
    }

    private fun showLabel(text: String): View = TextView(applicationContext).apply {
        setText(text)
        setTextColor(ContextCompat.getColor(applicationContext, ElderDesignTokens.textSecondary))
    }

    private fun showContactValue(text: String?, big: Int): View = TextView(applicationContext).apply {
        setText(text ?: "")
        setTextSize(big.toFloat())
        setTextColor(ContextCompat.getColor(applicationContext, ElderDesignTokens.textPrimary))
    }

    private fun buildBigButton(text: String, action: Action): View {
        val button = Button(applicationContext).apply {
            setText(text)
            setTextSize(ElderDesignTokens.fontListTitle.toFloat())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        button.setOnClickListener { handle(action) }
        root.addView(button)
        return button
    }

    private fun handle(action: Action) {
        when (action) {
            Action.DIAL -> dial()
            Action.REOPEN -> finishAfterHome()
            Action.VOLUME -> adjustVolume()
            Action.SWITCH -> startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun dial() {
        val phone = Prefers.getString("luoshitu_contact_phone") ?: ""
        if (phone.isEmpty()) {
            Toast.makeText(this, "请先在家属模式里保存联系方式（称谓与电话）", android.view.View.VISIBLE).show()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)))
        } catch (e: Exception) {
            Toast.makeText(this, "号码无法拨出，请改用家属预留的联系方式", android.view.View.VISIBLE).show()
        }
    }

    private fun adjustVolume() {
        val audio = applicationContext.getSystemService(AudioManager::class.java)
        audio.adjustVolume(60, 60)
        Toast.makeText(this, "声音已经调大，可以开始看了", android.view.View.VISIBLE).show()
    }

    private fun finishAfterHome() {
        finish()
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private enum class Action { DIAL, REOPEN, VOLUME, SWITCH }
}

