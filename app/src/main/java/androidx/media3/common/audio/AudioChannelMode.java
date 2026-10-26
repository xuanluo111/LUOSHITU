// 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.common.audio;


public class AudioChannelMode {
	public Object STEREO;
	public Object MONO;
	public Object REVERSE;
	public Object AUTO;
	public boolean isAvailable(int p0, int p1) { return false; }
	public int resolve(int p0, int p1) { return 0; }
	public boolean isAvailable(int p0) { return false; }
	public boolean canBalance(int p0) { return false; }
	public boolean isAuto(int p0) { return false; }
	public Object clamp(int p0) { return null; }
}
