// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 双字幕选择状态；本产品副字幕路径停用，字段承载默认值。
package androidx.media3.common.text;

import androidx.media3.common.TrackSelectionOverride;

import java.util.Collections;
import java.util.List;

public class SubtitleSelectionState {

    public static final SubtitleSelectionState EMPTY = new SubtitleSelectionState();

    public TrackSelectionOverride primarySelection;
    public TrackSelectionOverride activeSecondarySelection;
    public TrackSelectionOverride requestedSecondarySelection;
    public List<TrackSelectionOverride> secondaryCandidates = Collections.emptyList();
    public boolean secondaryPromotedToPrimary;

    public boolean isPrimary(TrackSelectionOverride override) { return false; }
    public boolean isActiveSecondary(TrackSelectionOverride override) { return false; }
}
