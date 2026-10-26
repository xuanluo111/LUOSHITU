// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 双字幕选择状态；本产品停用副字幕路径，字段仅承载默认值。
package androidx.media3.common.text;

import androidx.media3.common.TrackSelectionOverride;

public class SubtitleSelectionState {

    public static class State {
        public static final int LOADING = 0;
        public static final int COMPLETE = 1;
        public final int value;
        public State(int value) { this.value = value; }
        public static final State LOADING_I = new State(0);
        public static final State COMPLETE_I = new State(1);
    }

    public State primarySelection = new State(State.COMPLETE);
    public State secondaryCandidates = new State(State.LOADING);
    public State requestedSecondarySelection = new State(State.LOADING);
    public boolean secondaryPromotedToPrimary;
    public State activeSecondarySelection = new State(State.LOADING);

    public static final SubtitleSelectionState EMPTY = new SubtitleSelectionState();

    public boolean isPrimary(TrackSelectionOverride p0) { return false; }
    public boolean isActiveSecondary(TrackSelectionOverride p0) { return false; }

	public static Object activeSecondarySelection;
}
