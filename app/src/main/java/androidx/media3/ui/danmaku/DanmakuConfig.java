// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 弹幕样式配置；本产品不启用弹幕（F8），仅承载编译期签名。
package androidx.media3.ui.danmaku;

public class DanmakuConfig {

    public static class Builder {
        public Builder setTextScale(float v) { return this; }
        public Builder setTransparency(float v) { return this; }
        public Builder setTextBold(boolean b) { return this; }
        public Builder setTypeface(Object typeface) { return this; }
        public Builder setStyleMode(int mode) { return this; }
        public Builder setShowReverse(boolean b) { return this; }
        public Builder setShowPositioned(boolean b) { return this; }
        public Builder setShowSubtitle(boolean b) { return this; }
        public Builder setShowSpecial(boolean b) { return this; }
        public DanmakuConfig build() { return new DanmakuConfig(); }
    }
}
