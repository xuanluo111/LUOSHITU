// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.exoplayer.libass;

import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.text.SubtitleSelectionState;

public class LibassSubtitleController {
    public LibassSubtitleController(Object player, Object session, Object factory, Object output) { }
	public void close() { }
	// 洛视频：TrackDialog 与 SubtitleSettingPanel 拿到返回值后直接读字段，返回 null 会 NPE 闪退，必须给默认状态。
	public SubtitleSelectionState getSubtitleSelectionState() { return SubtitleSelectionState.EMPTY; }
	public void setSecondaryTextTrackSelectionOverride(TrackSelectionOverride p0) { }
	public void setSecondaryTextTrackAutoSelectionEnabled(boolean p0) { }
}
