// 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.mpvplayer;

public class MpvPlayer {
	public Object VIDEO_EFFECTS_SUPPORTED;
	public Object VIDEO_EFFECTS_UNSUPPORTED_DIRECT_DOLBY_VISION_OUTPUT;
	public Object AUDIO_EFFECTS_SUPPORTED;
	public Object AUDIO_EFFECTS_UNSUPPORTED_PASSTHROUGH;
	public boolean isAvailable(Object p0) { return false; }
	public Object getApplicationLooper(Object p0) { return null; }
	public void setLogListener(int p0, Object p1) { }
	public void setStartupScripts(Object p0) { }
	public Object getScriptData(String p0) { return null; }
	public Object getInputBindings(Object p0) { return null; }
	public Object unloadScript(long p0) { return null; }
	public Object runScriptBinding(String p0) { return null; }
	public void setVideoEqualizer(MpvVideoEqualizer p0) { }
	public void setVideoEqualizer(Object p0) { }
	public void setAudioFilterListener(Object p0) { }
	public Object getVideoEffectsSupport(Object p0) { return null; }
	public boolean isVideoSharpnessSupported(Object p0) { return false; }
	public Object getAudioEffectsSupport(Object p0) { return null; }
	public Object getAudioChannelCount(Object p0) { return null; }
	public void setAudioFilter(Object p0) { }
	public void setAudioFilter(MpvAudioFilter p0) { }
	public void setAudioOutputListener(Object p0) { }
	public void addListener(MpvPlayerEngine p0) { }
	public void setVerboseLoggingEnabled(boolean p0) { }
	public void removeListener(MpvPlayerEngine p0) { }
	public void release(Object p0) { }
	public Object getSubtitleSelectionState(Object p0) { return null; }
	public Object getSubtitleContentForSelection(TrackSelectionOverride p0) { return null; }
	public Object getSubtitleOffsets(Object p0) { return null; }
	public void setSubtitleOffsets(SubtitleOffsets p0) { }
	public void setSubtitleContentEnabled(boolean p0) { }
	public boolean canRetrySubtitleContent(Format p0) { return false; }
	public Object retrySubtitleContent(Format p0) { return null; }
	public void setSecondaryTextTrackSelectionOverride(TrackSelectionOverride p0) { }
	public void setSecondaryTextTrackAutoSelectionEnabled(boolean p0) { }
	public Object getCurrentMediaItem(Object p0) { return null; }
	public Object getPlaybackState(Object p0) { return null; }
	public void addSubtitle(SubtitleConfiguration p0) { }
	public Object getVideoDecoderMode(Object p0) { return null; }
	public void setDiscDataSourceFactory(Factory p0) { }
	public void setMediaItem(MediaItem p0, long p1) { }
	public void stop(Object p0) { }
	public void resetSecondaryTextTrackSelection(Object p0) { }
	public void setSubtitleContentLoader(SubtitleFileContent p0) { }
	public void addListener(Listener p0) { }
	public void setSubtitleOptions(MpvSubtitleOptions p0) { }
	public Object getTrackSelectionParameters(Object p0) { return null; }
	public static class Builder { }
}
