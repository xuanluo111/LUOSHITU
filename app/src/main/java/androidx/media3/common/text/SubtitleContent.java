// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.text;

import java.util.Collections;
import java.util.List;

public class SubtitleContent {

    public static final SubtitleContent LOADING = new SubtitleContent();
    public static final SubtitleContent ERROR = new SubtitleContent();
    public static final SubtitleContent UNSUPPORTED = new SubtitleContent();
    public static final SubtitleContent PARTIAL = new SubtitleContent();
    public static final SubtitleContent TRUNCATED = new SubtitleContent();

    public enum State { LOADING, COMPLETE, ERROR, UNSUPPORTED, PARTIAL, TRUNCATED }

    public static class Entry {
        public String image;
        public String text;
        public long startTimeUs;
        public long endTimeUs;
    }

    public int state = State.COMPLETE;
    public List<Entry> entries = Collections.emptyList();

    public boolean isEmpty() { return entries.isEmpty(); }

	public static Object state;
	public static Object entries;
}
