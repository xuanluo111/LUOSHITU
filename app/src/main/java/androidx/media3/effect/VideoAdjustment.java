// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 调色参数载体；EXO 路径下效果按需应用，未实现的维度为无操作。
package androidx.media3.effect;

public class VideoAdjustment {

    public float saturation, contrast, brightness, gamma, hue, redGain, blueGain, sharpness, threshold, shadowLift;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final VideoAdjustment out = new VideoAdjustment();
        public Builder setSaturation(float v) { out.saturation = v; return this; }
        public Builder setContrast(float v) { out.contrast = v; return this; }
        public Builder setBrightness(float v) { out.brightness = v; return this; }
        public Builder setGamma(float v) { out.gamma = v; return this; }
        public Builder setHue(float v) { out.hue = v; return this; }
        public Builder setRedGain(float v) { out.redGain = v; return this; }
        public Builder setBlueGain(float v) { out.blueGain = v; return this; }
        public Builder setSharpness(float v) { out.sharpness = v; return this; }
        public Builder setThreshold(float v) { out.threshold = v; return this; }
        public Builder setShadowLift(float v) { out.shadowLift = v; return this; }
        public VideoAdjustment build() { return out; }
    }
}
