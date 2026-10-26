// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.exoplayer.text;
import com.fongmi.android.tv.player.subtitle.SubtitleFileContent;


public class SubtitleTranscriptSession {
	public Object getContent(Format p0, Object p1) { return null; }
	public void setContentEnabled(boolean p0) { }
	public boolean canRetryContent(Format p0) { return false; }
	public Object retryContent(Format p0) { return null; }
	public void setLoader(SubtitleFileContent p0) { }
	public void setPlayer(ExoPlayer p0) { }
	public void setPreloadMediaItem(MediaItem p0) { }
	public void setPreloadMediaItem(Object p0) { }
	public void close(Object p0) { }
	public SubtitleTranscript forMediaItem(MediaItem p0) { return null; }
	public static class Format { }
	public static class ExoPlayer { }
	public static class MediaItem { }
}
