// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.text;

import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.text.SubtitleContent;

public class DefaultSubtitleContentLoader {

    public DefaultSubtitleContentLoader(androidx.media3.datasource.DataSource.Factory factory) { }

    // 洛视频：返回结果会被直接读状态字段，null 会 NPE；此处统一上报“不支持”。
    public SubtitleContent load(MediaItem item, android.net.Uri uri, Format format) { return SubtitleContent.UNSUPPORTED; }
}
