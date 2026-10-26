// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.mpvplayer;

public class MpvAndroidOptions {

    public static class Builder {
        public Builder setShaderCacheDirectory(Object dir) { return this; }
        public Builder setVulkanEnabled(boolean b) { return this; }
        public Builder setGpuNextEnabled(boolean b) { return this; }
        public Builder setAudioPassthroughEnabled(boolean b) { return this; }
        public Builder setDolbyVisionOutputPolicy(int policy) { return this; }
        public MpvAndroidOptions build() { return new MpvAndroidOptions(); }
    }
}
