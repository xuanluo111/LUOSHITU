// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.common.audio;

public class AudioEffectParameters {
    public AudioEffectParameters() { }
	public Object DEFAULT;
	public boolean isCenterGainAvailable(int p0) { return false; }
	public boolean hasEffect() { return false; }
	public boolean hasBands() { return false; }
	public int getChannelMode() { return 0; }
	public boolean isLoudnessEnabled() { return false; }
	public int getBoostMillibels() { return 0; }
	public short[] getEqualizerLevelsMillibels() { return null; }
	public boolean shouldLimitOutput(int p0) { return false; }
	public boolean hasCenterGain(int p0) { return false; }
	public float[][] getCenterGainFactor() { return null; }
	public float getStability() { return 0f; }
	public static class Builder {
		public Builder setEqualizerBands(int[] frequencies, short[] levels) { return this; }
		public Builder setStability(float v) { return this; }
		public Builder setBoostMillibels(float v) { return this; }
		public Builder setPreampMillibels(int v) { return this; }
		public Builder setCenterGainMillibels(float v) { return this; }
		public Builder setBalance(float v) { return this; }
		public Builder setChannelMode(int v) { return this; }
		public Builder setLoudnessEnabled(boolean b) { return this; }
		public Builder addDynamicNormalization(String id, double a, double b) { return this; }
		public Builder addCompressor(String id, float a, float b, float c, float d) { return this; }
		public Builder addVolume(String id, double level) { return this; }
		public Builder addEqualizer(String id, int band, double value) { return this; }
		public Builder addLimiter(String id, double limit) { return this; }
		public Builder addRuntimeChannelMix(String id, float[] mix) { return this; }
		public AudioEffectParameters build() { return new AudioEffectParameters(); }
	}
}
