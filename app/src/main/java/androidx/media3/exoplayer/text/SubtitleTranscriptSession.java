// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.exoplayer.text;

import androidx.media3.exoplayer.ExoPlayer;

import androidx.media3.common.Format;

import androidx.media3.common.MediaItem;
import com.fongmi.android.tv.player.subtitle.SubtitleFileContent;

public class SubtitleTranscriptSession {
	public androidx.media3.common.text.SubtitleContent getContent(Format p0, long p1) { return null; }
	public void setContentEnabled(boolean p0) { }
	public boolean canRetryContent(Format p0) { return false; }
	public Object retryContent(Format p0) { return null; }
	public void setLoader(SubtitleFileContent p0) { }
	public void setPlayer(ExoPlayer p0) { }
	public void setPreloadMediaItem(MediaItem p0) { }
	public void setPreloadMediaItem(MediaItem p0) { }
	public void close() { }
	public SubtitleTranscript forMediaItem(MediaItem p0) { return null; }
}
