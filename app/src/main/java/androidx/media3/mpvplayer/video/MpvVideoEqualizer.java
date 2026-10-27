// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.mpvplayer.video;

public class MpvVideoEqualizer {

    public static final MpvVideoEqualizer DEFAULT = new MpvVideoEqualizer();
    public static MpvVideoEqualizer create(float brightness, float contrast, float saturation, float gamma, float hue) { return new MpvVideoEqualizer(); }
    public void applyTo(Object builder) { }

	public MpvVideoEqualizer create(float p0, float p1, float p2, float p3, float p4, float p5) { return null; }
}
