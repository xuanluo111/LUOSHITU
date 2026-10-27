// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.text;

import java.util.Collections;
import java.util.List;

public class SubtitleContent {

    public enum State { LOADING, COMPLETE, ERROR, UNSUPPORTED, PARTIAL, TRUNCATED }

    public static class Entry {
        public boolean image;
        public String text;
        public long startTimeUs;
        public long endTimeUs;
    }

    public static final SubtitleContent LOADING = new SubtitleContent(State.LOADING);
    public static final SubtitleContent UNSUPPORTED = new SubtitleContent(State.UNSUPPORTED);

    public State state;
    public List<Entry> entries = Collections.emptyList();

    public SubtitleContent() { this.state = State.LOADING; }
    public SubtitleContent(State state) { this.state = state; }

    public boolean isEmpty() { return entries.isEmpty(); }
}
