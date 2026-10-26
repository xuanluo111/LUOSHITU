package com.luoshitu.video.elder

import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import com.fongmi.android.tv.R

/**
 * 适老化设计令牌的代码侧入口。数值以 res/values/elder_tokens.xml 为唯一来源，
 * 代码不得散写魔法数；无法用资源表达的比率（如换台按钮 = 屏高 20%）也集中在此。
 */
object ElderDesignTokens {

    const val LIVE_SWITCH_BUTTON_HEIGHT_RATIO = 0.20f

    const val FONT_SCALE_MAX = 1.3f

    @DimenRes
    val fontBody: Int = R.dimen.elder_font_body

    @DimenRes
    val fontListTitle: Int = R.dimen.elder_font_list_title

    @DimenRes
    val fontPageTitle: Int = R.dimen.elder_font_page_title

    @DimenRes
    val fontChannel: Int = R.dimen.elder_font_channel

    @DimenRes
    val fontHero: Int = R.dimen.elder_font_hero

    @DimenRes
    val touch: Int = R.dimen.elder_touch

    @DimenRes
    val touchMain: Int = R.dimen.elder_touch_main

    @DimenRes
    val gap: Int = R.dimen.elder_gap

    @ColorRes
    val textPrimary: Int = R.color.elder_text_primary

    @ColorRes
    val textSecondary: Int = R.color.elder_text_secondary

    @ColorRes
    val primary: Int = R.color.elder_primary

    @ColorRes
    val danger: Int = R.color.elder_danger

    @ColorRes
    val success: Int = R.color.elder_success

    @ColorRes
    val bannerBg: Int = R.color.elder_banner_bg
}
