// @hand 洛视频兼容层：官方 media3 类上不存在上游私有 aar 的 fork 扩展方法，
// 经此类提供固定实现（光盘菜单不可用、章节/版本为空、偏移与视频变换为无操作）。
// 见 docs/S0-立项报告.md 第 9 节。
package androidx.media3.fork;

import android.graphics.RectF;

import androidx.media3.common.DiscMenuState;
import androidx.media3.common.MediaChapter;
import androidx.media3.common.MediaEdition;
import androidx.media3.common.Player;
import androidx.media3.common.VideoTransform;
import androidx.media3.common.text.SubtitleOffsets;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public final class MediaFork {

    private MediaFork() { }

    // fork 扩展命令位：任意值即可，真实 EXO isCommandAvailable 对未知命令返回 false，调用点本就有守卫
    public static final int COMMAND_GET_TEXT_OFFSET = 0x4C550001;
    public static final int COMMAND_SET_TEXT_OFFSET = 0x4C550002;
    public static final int COMMAND_GET_AUDIO_OFFSET = 0x4C550003;
    public static final int COMMAND_SET_AUDIO_OFFSET = 0x4C550004;
    public static final int EVENT_DISC_MENU_STATE_CHANGED = 0x4C550005;

    public static DiscMenuState getDiscMenuState(Player player) { return DiscMenuState.EMPTY; }
    public static java.util.List<MediaChapter> getCurrentMediaChapters(Player player) { return java.util.Collections.emptyList(); }
    public static java.util.List<MediaEdition> getCurrentMediaEditions(Player player) { return java.util.Collections.emptyList(); }
    public static boolean selectChapter(Player player, MediaChapter chapter) { return false; }
    public static void selectEdition(Player player, MediaEdition edition) { }
    public static long getTextOffsetMs(Player player) { return 0; }
    public static void setTextOffsetMs(Player player, long offsetMs) { }
    public static long getAudioOffsetMs(Player player) { return 0; }
    public static void setAudioOffsetMs(Player player, long offsetMs) { }

    // ExoPlayer（官方接口）上的 fork 扩展：效果支持度按"不支持"处理，字幕偏移固定 0
    public static final int VIDEO_EFFECTS_SUPPORTED = 0;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_DRM = 1;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_RENDERER = 2;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_TUNNELING = 3;
    public static final int AUDIO_PROCESSING_SUPPORTED = 0;
    public static final int AUDIO_PROCESSING_UNSUPPORTED_PASSTHROUGH = 1;
    public static int getVideoEffectsSupport(ExoPlayer player) { return VIDEO_EFFECTS_UNSUPPORTED_RENDERER; }
    public static int getAudioProcessingSupport(ExoPlayer player) { return AUDIO_PROCESSING_UNSUPPORTED_PASSTHROUGH; }
    public static boolean isSkipSilenceSupported(ExoPlayer player) { return false; }
    public static SubtitleOffsets getSubtitleOffsets(ExoPlayer player) { return SubtitleOffsets.ZERO; }
    public static void setSubtitleOffsets(ExoPlayer player, SubtitleOffsets offsets) { }

    // PlayerView（官方类）上的 fork 扩展：视频变换/宽高比/渲染模式在 EXO 下为无操作
    public static VideoTransform getVideoTransform(PlayerView view) { return VideoTransform.DEFAULT; }
    public static void animateVideoTransform(PlayerView view, VideoTransform transform, int durationMs) { }
    public static void setVideoTransform(PlayerView view, VideoTransform transform) { }
    public static void finishVideoTransformAnimation(PlayerView view) { }
    public static RectF getVideoBounds(PlayerView view) { return new RectF(); }
    public static void setRender(PlayerView view, int render) { }
    public static void setBackendPlayer(PlayerView view, ExoPlayer player) { }
    public static float getVideoAspectRatioOverride(PlayerView view) { return 0f; }
    public static void setVideoAspectRatioOverride(PlayerView view, float ratio) { }
}
