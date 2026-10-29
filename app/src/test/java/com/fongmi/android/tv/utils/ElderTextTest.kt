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

    @Test fun `drops placeholder zero rating remark`() {
        // 站点把「评分：0」写进 remark，每张海报都挂一句，老人会误以为片子很差
        assertEquals("", ElderText.remark("评分：0"))
        assertEquals("", ElderText.remark("评分: 0"))
        assertEquals("", ElderText.remark("評分：0.0"))
        assertEquals("", ElderText.remark("得分: 0分"))
    }

    @Test fun `keeps real rating remark`() {
        assertEquals("评分：8.9", ElderText.remark("评分：8.9"))
    }

    @Test fun `keeps non rating remark`() {
        assertEquals("更新至20261023期", ElderText.remark("更新至20261023期"))
        assertEquals("HD国语", ElderText.remark("HD国语"))
        assertEquals("", ElderText.remark(null))
        assertEquals("", ElderText.remark("   "))
    }
}
