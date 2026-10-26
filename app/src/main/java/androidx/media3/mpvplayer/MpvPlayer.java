// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// MPV 引擎在本产品停用（EXO 单内核）：isAvailable() 恒为 false，本类仅存在于编译期，运行时不会被实例化。
package androidx.media3.mpvplayer;

import android.net.Uri;

import androidx.media3.common.C;
import androidx.media3.common.DiscMenuController;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.text.SubtitleContent;
import androidx.media3.common.text.SubtitleOffsets;
import androidx.media3.common.text.SubtitleSelectionState;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.DecoderMode;

public abstract class MpvPlayer extends DiscMenuController implements Player {

    public boolean isAvailable(Object p0) { return false; }

    public static final int VIDEO_EFFECTS_SUPPORTED = 0;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_DRM = 1;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_RENDERER = 2;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_TUNNELING = 3;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_DIRECT_DRAW = 4;
    public static final int AUDIO_EFFECTS_SUPPORTED = 0;
    public static final int AUDIO_EFFECTS_UNSUPPORTED_PASSTHROUGH = 1;

    public Object getVideoEffectsSupport(Object p0) { return VIDEO_EFFECTS_UNSUPPORTED_RENDERER; }
    public Object getAudioEffectsSupport(Object p0) { return AUDIO_EFFECTS_UNSUPPORTED_PASSTHROUGH; }
    public boolean isVideoSharpnessSupported(Object p0) { return false; }
    public void setVideoEqualizer(Object p0) { }
    public void setAudioFilter(MpvAudioFilter p0) { }
    public void setSubtitleContentLoader(SubtitleFileContent p0) { }
    public void setSubtitleOptions(MpvSubtitleOptions p0) { }

    public static class Builder {
        public Builder setVulkanEnabled(boolean b) { return this; }
        public Builder setGpuNextEnabled(boolean b) { return this; }
        public Builder setFontsDirectory(String... dirs) { return this; }
        public Builder setPosition(float v) { return this; }
        public Builder setVerticalOffset(float v) { return this; }
        public Builder setScale(float v) { return this; }
        public Builder setBitmapSubtitleBrightness(float v) { return this; }
        public Builder setSecondarySubtitlePosition(float v) { return this; }
        public Builder setSecondaryVerticalOffset(float v) { return this; }
        public Builder setSecondaryAssStyleOverride(boolean b) { return this; }
        public Builder setFontFamily(String family) { return this; }
        public Builder setCustomStyle(Object style) { return this; }
        public Builder setSystemCaptionStyle(Object style) { return this; }
        public Builder setDefaultStyle(Object style) { return this; }
        public Builder setTextScale(float v) { return this; }
        public Builder setFontConfig(Object config) { return this; }
        public Builder setEqualizerBands(Object bands) { return this; }
        public Builder setAdblock(boolean b) { return this; }
        public Builder setDataSourceFactory(Object factory) { return this; }
        public Builder setShaderCacheDirectory(String dir) { return this; }
        public Builder addPreInitStringOption(String k, String v) { return this; }
        public Builder addTlsCaFileFromAsset(String asset) { return this; }
        public Builder addDiskCacheOptions(Object options) { return this; }
        public Builder addDynamicNormalization(Object o) { return this; }
        public Builder addCompressor(Object o) { return this; }
        public Builder addVolume(Object o) { return this; }
        public Builder addEqualizer(Object o) { return this; }
        public Builder addLimiter(Object o) { return this; }
        public Builder addRuntimeChannelMix(Object o) { return this; }
        public MpvPlayer build() { return null; }
    }

    public interface LogListener { void onLogMessage(String prefix, int level, String text); }
    public interface ScriptLoadListener { void onLoaded(boolean success, long clientId); }
    public interface AudioOutputListener { void onAudioOutputApplied(); }
    public interface AudioFilterListener { void onAudioFilterApplied(); }

    public void setLogListener(int p0, Object p1) { }
    public Object unloadScript(long p0) { }
    public void setStartupScripts(Object p0) { return false; }
    public String getScriptData(String p0) { return null; }
    public Object getInputBindings(Object p0) { return null; }
    public Object runScriptBinding(String p0) { return false; }
    public void setAudioOutputListener(Object p0) { }
    public void setAudioFilterListener(Object p0) { }
    public void setVerboseLoggingEnabled(boolean p0) { }
    public Object getAudioChannelCount(Object p0) { return 0; }
    public Object getSubtitleSelectionState(Object p0) { return null; }
    public SubtitleContent getSubtitleContentForSelection(TrackSelectionOverride p0) { return null; }
    public SubtitleOffsets getSubtitleOffsets(Object p0) { return null; }
    public void setSubtitleOffsets(SubtitleOffsets p0) { }
    public void setSubtitleContentEnabled(boolean p0) { }
    public boolean canRetrySubtitleContent(Format p0) { return false; }
    public Object retrySubtitleContent(Format p0) { }
    public void setSecondaryTextTrackSelectionOverride(TrackSelectionOverride p0) { }
    public void setSecondaryTextTrackAutoSelectionEnabled(boolean p0) { }
    public void resetSecondaryTextTrackSelection(Object p0) { }
    public void addSubtitle(SubtitleConfiguration p0) { return false; }
    public Object getVideoDecoderMode(Object p0) { return null; }
    public void setVideoDecoderMode(DecoderMode mode) { }
    public void setDiscDataSourceFactory(Factory p0) { }
    public void setMediaItem(MediaItem p0, long p1) { }
    public Uri getLocalConfigurationUri() { return null; }
	public Object getApplicationLooper(Object p0) { }
	public void addListener(Listener p0) { }
	public void removeListener(MpvPlayerEngine p0) { }
	public void release(Object p0) { }
	public Object getCurrentMediaItem(Object p0) { }
	public Object getPlaybackState(Object p0) { }
	public void stop(Object p0) { }
	public Object getTrackSelectionParameters(Object p0) { }

	public static Object VIDEO_EFFECTS_UNSUPPORTED_DIRECT_DOLBY_VISION_OUTPUT;

	public Object loadScript(String p0, Object p1) { }
}
