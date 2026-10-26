// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.text;

public class SubtitleOffsets {

    public final long primaryMs;
    public final long secondaryMs;

    public static final SubtitleOffsets ZERO = new SubtitleOffsets(0, 0);

    public SubtitleOffsets(long primaryMs, long secondaryMs) {
        this.primaryMs = primaryMs;
        this.secondaryMs = secondaryMs;
    }

}
