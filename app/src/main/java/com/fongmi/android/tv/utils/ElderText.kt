package com.fongmi.android.tv.utils

/**
 * 老人界面文本清洗（3.2）：剥离源配置/影片名前缀的 emoji 与装饰符号。
 * 只清洗展示层，不改动存储与匹配逻辑（搜索/收藏仍用原始名）。
 * 纯 JVM，可单测。
 */
object ElderText {

    /** 剥掉名称开头连续的装饰字符（emoji/符号/空白），返回从第一个"实字"开始的名称 */
    @JvmStatic
    fun clean(name: String?): String {
        if (name.isNullOrEmpty()) return ""
        var start = 0
        while (start < name.length && isDecorative(name[start])) start++
        return name.substring(start).trim()
    }

    private fun isDecorative(c: Char): Boolean {
        if (c.isWhitespace()) return true
        val t = Character.getType(c).toInt()
        // emoji 代理对（高低半区类型相同）/符号区/变体选择符/数学符号/组合记号
        return t == Character.SURROGATE.toInt() || t == Character.OTHER_SYMBOL.toInt() ||
            t == Character.MODIFIER_SYMBOL.toInt() || t == Character.MATH_SYMBOL.toInt() ||
            t == Character.NON_SPACING_MARK.toInt()
    }
}
