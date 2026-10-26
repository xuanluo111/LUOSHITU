// 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.common.audio;


public class AudioEffectParameters {
	public Object DEFAULT;
	public boolean isCenterGainAvailable(int p0) { return false; }
	public boolean hasEffect(Object p0) { return false; }
	public boolean hasBands(Object p0) { return false; }
	public Object getChannelMode(Object p0) { return null; }
	public Object getBalance(Object p0) { return null; }
	public boolean isLoudnessEnabled(Object p0) { return false; }
	public Object getBoostMillibels(Object p0) { return null; }
	public Object getPreampMillibels(Object p0) { return null; }
	public Object getEqualizerLevelsMillibels(Object p0) { return null; }
	public boolean shouldLimitOutput(int p0) { return false; }
	public boolean hasCenterGain(int p0) { return false; }
	public Object getCenterGainFactor(Object p0) { return null; }
	public Object getStability(Object p0) { return null; }
	public static class Builder { }
}
