// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.mpvplayer;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class MpvConfigFileInspector {

    public MpvConfigFileInspector(File directory) { }

    public File readSubtitleFontsDirectory() { return null; }
    public Set<String> getEffectiveOptionNames(CharSequence content) { return Collections.emptySet(); }
}
