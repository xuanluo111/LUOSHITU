// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// MPV 引擎在本产品停用（EXO 单内核）：isAvailable() 恒为 false，本类仅存在于编译期，运行时不会被实例化。
package androidx.media3.mpvplayer;

import android.net.Uri;

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
import androidx.media3.mpvplayer.audio.MpvAudioFilter;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.mpvplayer.video.MpvVideoEqualizer;

public abstract class MpvPlayer extends DiscMenuController implements Player {

    public static boolean isAvailable() { return false; }

    public static final int VIDEO_EFFECTS_SUPPORTED = 0;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_DRM = 1;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_RENDERER = 2;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_TUNNELING = 3;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_DIRECT_DOLBY_VISION_OUTPUT = 4;
    public static final int AUDIO_EFFECTS_SUPPORTED = 0;
    public static final int AUDIO_EFFECTS_UNSUPPORTED_PASSTHROUGH = 1;

    public interface LogListener { void onLogMessage(String prefix, int level, String text); }
    public interface ScriptLoadListener { void onLoaded(boolean success, long clientId); }
    public interface AudioOutputListener { void onAudioOutputApplied(); }
    public interface AudioFilterListener { void onAudioFilterApplied(boolean success); }

    public int getVideoEffectsSupport() { return VIDEO_EFFECTS_UNSUPPORTED_RENDERER; }
    public int getAudioEffectsSupport() { return AUDIO_EFFECTS_UNSUPPORTED_PASSTHROUGH; }
    public boolean isVideoSharpnessSupported() { return false; }
    public void setVideoEqualizer(MpvVideoEqualizer p0) { }
    public boolean setAudioFilter(MpvAudioFilter p0) { return false; }
    public void setSubtitleContentLoader(MpvSubtitleContentLoader loader) { }
    public void setSubtitleOptions(MpvSubtitleOptions p0) { }
    public void setLogListener(int level, LogListener listener) { }
    public boolean loadScript(String path, ScriptLoadListener listener) { return false; }
    public boolean setStartupScripts(Object scripts) { return false; }
    public String getScriptData(String key) { return null; }
    public String getInputBindings() { return null; }
    public boolean runScriptBinding(String binding) { return false; }
    public void unloadScript(long clientId) { }
    public void setAudioOutputListener(AudioOutputListener listener) { }
    public void setAudioFilterListener(AudioFilterListener listener) { }
    public void setVerboseLoggingEnabled(boolean enabled) { }
    public int getAudioChannelCount() { return 0; }
    public SubtitleSelectionState getSubtitleSelectionState() { return null; }
    public SubtitleContent getSubtitleContentForSelection(TrackSelectionOverride selection) { return null; }
    public SubtitleOffsets getSubtitleOffsets() { return null; }
    public void setSubtitleOffsets(SubtitleOffsets offsets) { }
    public void setSubtitleContentEnabled(boolean enabled) { }
    public boolean canRetrySubtitleContent(Format format) { return false; }
    public void retrySubtitleContent(Format format) { }
    public void setSecondaryTextTrackSelectionOverride(TrackSelectionOverride selection) { }
    public void setSecondaryTextTrackAutoSelectionEnabled(boolean enabled) { }
    public void resetSecondaryTextTrackSelection() { }
    public boolean addSubtitle(MediaItem.SubtitleConfiguration config) { return false; }
    public DecoderMode getVideoDecoderMode() { return null; }
    public void setVideoDecoderMode(DecoderMode mode) { }
    public void setDiscDataSourceFactory(DataSource.Factory factory) { }
    public void setMediaItem(MediaItem item, long startPositionMs) { }
    public Uri getLocalConfigurationUri() { return null; }
    public void setBackendPlayer(Object p0) { }  // 洛视频降级：EXO 单内核，MPV 停用，后端玩家仅存编译期签名

    public static class Builder {
        public Builder(Object context) { }
        public Builder setVideoDecoderMode(MpvDecoderMode mode) { return this; }
        public Builder setConfig(Object config) { return this; }
        public Builder setVulkanEnabled(boolean b) { return this; }
        public Builder setGpuNextEnabled(boolean b) { return this; }
        public Builder setFontsDirectory(String... dirs) { return this; }
        public Builder setPosition(double v) { return this; }
        public Builder setVerticalOffset(float v) { return this; }
        public Builder setScale(float v) { return this; }
        public Builder setBitmapSubtitleBrightness(float v) { return this; }
        public Builder setSecondarySubtitlePosition(double v) { return this; }
        public Builder setSecondaryVerticalOffset(double v) { return this; }
        public Builder setSecondaryAssStyleOverride(boolean b) { return this; }
        public Builder setFontFamily(String family) { return this; }
        public Builder setCustomStyle(int a, int b, int c, int d, float e, float f) { return this; }
        public Builder setSystemCaptionStyle() { return this; }
        public Builder setDefaultStyle(Object a, Object b, Object c, Object d, Object e, Object f) { return this; }
        public Builder setTextScale(float v) { return this; }
        public Builder setFontConfig(Object config) { return this; }
        public Builder setEqualizerBands(Object bands) { return this; }
        public Builder setAdblock(boolean b) { return this; }
        public Builder setDataSourceFactory(Object factory) { return this; }
        public Builder setShaderCacheDirectory(Object dir) { return this; }
        public Builder addPreInitStringOption(String k, String v) { return this; }
        public Builder addTlsCaFileFromAsset(Object app, String asset, Object target) { return this; }
        public Builder addAndroidSubtitleOptions(Object app, Object options) { return this; }
        public Builder addDiskCacheOptions(Object cacheDir, int seconds) { return this; }
        public Builder addDefaultFontDirectories(Object... dirs) { return this; }
        public Builder addConfigDirectory(Object dir) { return this; }
        public Builder addAndroidDefaults(Object cacheDir) { return this; }
        public Builder addDynamicNormalization(Object o) { return this; }
        public Builder addCompressor(Object o) { return this; }
        public Builder addVolume(Object o) { return this; }
        public Builder addEqualizer(Object o) { return this; }
        public Builder addLimiter(Object o) { return this; }
        public Builder addRuntimeChannelMix(Object o) { return this; }
        public MpvPlayer build() { return null; }
    }

}
