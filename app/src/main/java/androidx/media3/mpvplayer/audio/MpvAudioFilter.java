// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.mpvplayer.audio;

public class MpvAudioFilter {

    public static final MpvAudioFilter EMPTY = new MpvAudioFilter();

    public MpvAudioFilter() { }

    public static class Builder {
        public Builder addDynamicNormalization(String id, double a, double b) { return this; }
        public Builder addCompressor(String id, float a, float b, float c, float d) { return this; }
        public Builder addVolume(String id, double level) { return this; }
        public Builder addEqualizer(String id, int band, double value) { return this; }
public Builder addEqualizer(String id, double centerFrequency, short level) { return this; }
        public Builder addLimiter(String id, double limit) { return this; }
        public Builder addRuntimeChannelMix(String id, float[][] mix) { return this; }
        public MpvAudioFilter build() { return new MpvAudioFilter(); }
    }
}
