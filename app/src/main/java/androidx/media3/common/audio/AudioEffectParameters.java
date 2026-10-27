// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.audio;

public class AudioEffectParameters {

    public static final AudioEffectParameters DEFAULT = new AudioEffectParameters();

    public AudioEffectParameters() { }

    public static boolean isCenterGainAvailable(int channelCount) { return false; }
    public boolean hasEffect() { return false; }
    public boolean hasBands() { return false; }
    public boolean hasCenterGain(int channelCount) { return false; }
    public boolean isLoudnessEnabled() { return false; }
    public boolean shouldLimitOutput(int channelCount) { return false; }
    public float getBalance() { return 0f; }
    public float getStability() { return 0f; }
    public float getBoostMillibels() { return 0f; }
    public int getBoostMillibelsInt() { return 0; }
    public float getCenterGainFactor() { return 0f; }
    public int getChannelMode() { return 0; }
    public short[] getEqualizerLevelsMillibels() { return new short[0]; }
    public int getPreampMillibels() { return 0; }

    public static class Builder {
        public Builder setEqualizerBands(int[] frequencies, short[] levels) { return this; }
        public Builder setStability(float v) { return this; }
        public Builder setBoostMillibels(float v) { return this; }
        public Builder setPreampMillibels(int v) { return this; }
        public Builder setCenterGainMillibels(float v) { return this; }
        public Builder setBalance(float v) { return this; }
        public Builder setChannelMode(int v) { return this; }
        public Builder setLoudnessEnabled(boolean b) { return this; }
        public AudioEffectParameters build() { return new AudioEffectParameters(); }
    }
}
