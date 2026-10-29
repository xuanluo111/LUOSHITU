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

    /** 「评分：0」「得分: 0.0分」这类格式 */
    private val rating = Regex("""^\s*(评分|評分|得分|分数|分數)\s*[:：]\s*([0-9]+(?:\.[0-9]+)?)\s*分?\s*$""")

    /**
     * 海报/详情展示用的备注清洗：部分站点的 remark 字段塞的是「评分：0」这类没打分的占位内容，
     * 每张海报都挂一句，老人会真以为这片子评分很差。评为 0 分的评分标签等同于没有备注。
     * 只影响展示，列表排序与收藏仍用原始字符串。
     */
    @JvmStatic
    fun remark(remark: String?): String {
        val text = remark?.trim().orEmpty()
        if (text.isEmpty()) return ""
        val match = rating.find(text) ?: return text
        val score = match.groupValues[2].toDoubleOrNull() ?: return text
        return if (score <= 0.0) "" else text
    }
}
