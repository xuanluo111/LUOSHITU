// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 弹幕样式配置；本产品不启用弹幕（F8），仅承载编译期签名与默认值。
package androidx.media3.ui.danmaku;

public class DanmakuConfig {

    public static final int STYLE_NONE = 0;
    public static final int STYLE_SHADOW = 1;
    public static final int STYLE_STROKE = 2;
    public static final int STYLE_PROJECTION = 3;
    public static final int COLOR_MODE_DEFAULT = 0;
    public static final int COLOR_MODE_COLORFUL = 1;
    public static final int COLOR_MODE_GRADIENT = 2;
    public static final DanmakuConfig DEFAULT = new DanmakuConfig();

    public long timeOffsetMs;
    public float textScale;
    public float transparency;
    public boolean textBold;
    public int styleMode;
    public Object typeface;
    public float shadowTransparency;
    public float strokeWidthMultiplier;
    public float projectionOffsetXMultiplier;
    public float projectionOffsetYMultiplier;
    public float projectionTransparency;
    public int colorMode;
    public long durationMs;
    public long fixedDurationMs;
    public int maxOnScreen;
    public float scrollAreaRatio;
    public float scrollGapRatio;
    public float lineSpacing;
    public int maxScrollLines;
    public int maxTopLines;
    public int maxBottomLines;
    public boolean showScroll;
    public boolean showTop;
    public boolean showBottom;
    public boolean showReverse;
    public boolean showPositioned;
    public boolean showSubtitle;
    public boolean showSpecial;

    public long getTimeOffsetMs() { return timeOffsetMs; }
    public float getTextScale() { return textScale; }
    public float getTransparency() { return transparency; }
    public boolean getTextBold() { return textBold; }
    public int getStyleMode() { return styleMode; }
    public float getShadowTransparency() { return shadowTransparency; }
    public float getStrokeWidthMultiplier() { return strokeWidthMultiplier; }
    public float getProjectionOffsetXMultiplier() { return projectionOffsetXMultiplier; }
    public float getProjectionOffsetYMultiplier() { return projectionOffsetYMultiplier; }
    public float getProjectionTransparency() { return projectionTransparency; }
    public int getColorMode() { return colorMode; }
    public int getDurationMs() { return durationMs; }
    public int getFixedDurationMs() { return fixedDurationMs; }
    public int getMaxOnScreen() { return maxOnScreen; }
    public float getScrollAreaRatio() { return scrollAreaRatio; }
    public float getScrollGapRatio() { return scrollGapRatio; }
    public float getLineSpacing() { return lineSpacing; }
    public int getMaxScrollLines() { return maxScrollLines; }
    public int getMaxTopLines() { return maxTopLines; }
    public int getMaxBottomLines() { return maxBottomLines; }
    public boolean getShowScroll() { return showScroll; }
    public boolean getShowTop() { return showTop; }
    public boolean getShowBottom() { return showBottom; }
    public boolean getShowReverse() { return showReverse; }
    public boolean getShowPositioned() { return showPositioned; }
    public boolean getShowSubtitle() { return showSubtitle; }
    public boolean getShowSpecial() { return showSpecial; }

    public static class Builder {
        private final DanmakuConfig out = new DanmakuConfig();
        public Builder setTimeOffsetMs(long v) { out.timeOffsetMs = v; return this; }
        public Builder setTextScale(float v) { out.textScale = v; return this; }
        public Builder setTransparency(float v) { out.transparency = v; return this; }
        public Builder setTextBold(boolean v) { out.textBold = v; return this; }
        public Builder setStyleMode(int v) { out.styleMode = v; return this; }
        public Builder setTypeface(Object typeface) { out.typeface = typeface; return this; }
        public Builder setShadowTransparency(float v) { out.shadowTransparency = v; return this; }
        public Builder setStrokeWidthMultiplier(float v) { out.strokeWidthMultiplier = v; return this; }
        public Builder setProjectionOffsetXMultiplier(float v) { out.projectionOffsetXMultiplier = v; return this; }
        public Builder setProjectionOffsetYMultiplier(float v) { out.projectionOffsetYMultiplier = v; return this; }
        public Builder setProjectionTransparency(float v) { out.projectionTransparency = v; return this; }
        public Builder setColorMode(int v) { out.colorMode = v; return this; }
        public Builder setDurationMs(long v) { out.durationMs = v; return this; }
        public Builder setFixedDurationMs(int v) { out.fixedDurationMs = v; return this; }
        public Builder setMaxOnScreen(int v) { out.maxOnScreen = v; return this; }
        public Builder setScrollAreaRatio(float v) { out.scrollAreaRatio = v; return this; }
        public Builder setScrollGapRatio(float v) { out.scrollGapRatio = v; return this; }
        public Builder setLineSpacing(float v) { out.lineSpacing = v; return this; }
        public Builder setMaxScrollLines(int v) { out.maxScrollLines = v; return this; }
        public Builder setMaxTopLines(int v) { out.maxTopLines = v; return this; }
        public Builder setMaxBottomLines(int v) { out.maxBottomLines = v; return this; }
        public Builder setShowScroll(boolean v) { out.showScroll = v; return this; }
        public Builder setShowTop(boolean v) { out.showTop = v; return this; }
        public Builder setShowBottom(boolean v) { out.showBottom = v; return this; }
        public Builder setShowReverse(boolean v) { out.showReverse = v; return this; }
        public Builder setShowPositioned(boolean v) { out.showPositioned = v; return this; }
        public Builder setShowSubtitle(boolean v) { out.showSubtitle = v; return this; }
        public Builder setShowSpecial(boolean v) { out.showSpecial = v; return this; }
        public DanmakuConfig build() { return out; }
    }

}
