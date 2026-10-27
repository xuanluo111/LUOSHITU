// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// libass 字幕渲染会话：本产品字幕走内置渲染，桩恒为不可用。
package androidx.media3.exoplayer.libass;

import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.text.SubtitleTranscript;
import androidx.media3.extractor.ExtractorsFactory;

public class LibassPlaybackSession {

    public LibassPlaybackSession(LibassConfiguration configuration, boolean enabled) { }

    public static class MediaComponents {
        public ExtractorsFactory extractorsFactory;
        public androidx.media3.extractor.text.SubtitleParser.Factory subtitleParserFactory;
    }

    public static boolean isAvailable() { return false; }

    public SubtitleTranscript forMediaItem(MediaItem mediaItem) { return null; }
    public MediaComponents createMediaComponents(MediaItem mediaItem, ExtractorsFactory extractorsFactory, SubtitleTranscript transcript) { return new MediaComponents(); }
    public void setPreloadMediaItem(MediaItem mediaItem) { }
    public void close() { }
    public Renderer createClockRenderer() { return null; }
    public void setBottomPositionFraction(float fraction) { }
    public void setSecondaryBottomPositionFraction(float fraction) { }
    public void setFontScale(float scale, boolean applied) { }
}
