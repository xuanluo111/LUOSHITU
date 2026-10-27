// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// libass 字幕渲染会话：本产品字幕走内置渲染，桩恒为不可用。
package androidx.media3.exoplayer.libass;

import androidx.media3.extractor.ExtractorsFactory;

import androidx.media3.exoplayer.Renderer;
import androidx.media3.extractor.DefaultExtractorsFactory;

import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.text.SubtitleTranscript;
import androidx.media3.exoplayer.text.SubtitleTranscriptParserFactory;
import androidx.media3.exoplayer.trackselection.SubtitleSelectionState;

public class LibassPlaybackSession {

    public static class MediaComponents {
        public ExtractorsFactory extractorsFactory;
        public androidx.media3.extractor.text.SubtitleParser.Factory subtitleParserFactory;
            }

    public boolean isAvailable() { return false; }

    public void setPreloadMediaItem(Object p0) { }
    public void close() { }
    public Object createClockRenderer() { return null; }
    public void setBottomPositionFraction(float p0) { }
    public void setSecondaryBottomPositionFraction(float p0) { }
    public void setFontScale(float p0, boolean p1) { }

    public SubtitleTranscript forMediaItem(MediaItem mediaItem) { return null; }

    public MediaComponents createMediaComponents(MediaItem p0, ExtractorsFactory p1, SubtitleTranscript p2) {
        return new MediaComponents();
    }

    public interface ExtractorsFactory {
        androidx.media3.extractor.DefaultExtractorsFactory setMp4ClearKeys(byte[][] keys);
    }
}
