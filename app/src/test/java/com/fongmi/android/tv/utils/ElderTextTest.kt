package com.fongmi.android.tv.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ElderTextTest {

    @Test fun `strips leading emoji prefix`() {
        assertEquals("资源站", ElderText.clean("🚀 资源站"))
        assertEquals("资源站", ElderText.clean("🔥💎 资源站"))
    }

    @Test fun `keeps cjk and letters intact`() {
        assertEquals("菜菜资源", ElderText.clean("菜菜资源"))
        assertEquals("Netflix", ElderText.clean("Netflix"))
    }

    @Test fun `keeps inner emoji only strips prefix`() {
        assertEquals("资源站🔥", ElderText.clean("资源站🔥"))
    }

    @Test fun `handles null blank and symbol-only names`() {
        assertEquals("", ElderText.clean(null))
        assertEquals("", ElderText.clean("   "))
        assertEquals("", ElderText.clean("🚀💎"))
    }

    @Test fun `keeps punctuation middot as content`() {
        // · 是标点（Po）不是 emoji 装饰，保留原名（3.2 只剥 emoji/符号前缀）
        assertEquals("·资源站", ElderText.clean("·资源站"))
    }
}
