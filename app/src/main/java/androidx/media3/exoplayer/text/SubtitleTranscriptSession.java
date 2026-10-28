// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.text;

import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.text.SubtitleContent;
import androidx.media3.exoplayer.ExoPlayer;

public class SubtitleTranscriptSession {

    public SubtitleTranscriptSession() { }
    public void close() { }
    public void retryContent(Format format) { }
    public boolean canRetryContent(Format format) { return false; }
    public void setContentEnabled(boolean enabled) { }
    public void setLoader(Object loader) { }
    public void setPlayer(ExoPlayer player) { }
    public SubtitleTranscript forMediaItem(MediaItem mediaItem) { return null; }
    public void setPreloadMediaItem(MediaItem mediaItem) { }
    // 洛视频：PlaybackContentPanel 直接读 content.state，返回 null 会 NPE。
    public SubtitleContent getContent(Format format, long offsetMs) { return SubtitleContent.UNSUPPORTED; }
}
