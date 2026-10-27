// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 尾部裁剪在本产品不启用（调用点已降级）。
package androidx.media3.exoplayer.source;

public class EndTrimMediaSource {

    public EndTrimMediaSource(MediaSource source, long endTrimMs) { }
    public void setEndTrimDurationMs(long ms) { }
    public boolean isEndTrimActive() { return false; }
    public long getOriginalDurationMs() { return 0; }
}
