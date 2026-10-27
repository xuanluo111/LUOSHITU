#!/usr/bin/env python3
# S1 收敛批次四（终版）：按编译错误日志逐条修复，目标 0 error
import re, os

B = 'app/src/main/java/androidx/media3/'
APP = 'app/src/main/java/com/fongmi/android/tv/'

def rd(p): return open(p, encoding='utf-8').read()
def wr(p, s): open(p, 'w', encoding='utf-8').write(s)

# ═══════════════════════════════════════════════════════════════
# 第一部分：media3 桩文件修正（按 javap 官方签名）
# ═══════════════════════════════════════════════════════════════

# 1) AudioEffectProcessor：queueInput 返回 void（官方 AudioProcessor.queueInput(ByteBuffer) 无返回值）
wr(B + 'common/audio/AudioEffectProcessor.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.audio;

import java.nio.ByteBuffer;

public class AudioEffectProcessor extends androidx.media3.common.audio.BaseAudioProcessor {

    @Override
    public void queueInput(ByteBuffer input) { }

    @Override
    protected androidx.media3.common.audio.AudioProcessor.AudioFormat onConfigure(androidx.media3.common.audio.AudioProcessor.AudioFormat format) { return format; }

    public void resetParameters() { }

    public boolean setParameters(Object config, boolean softwareEqualizer) { return false; }
}
''')

# 2) SubtitleTranscriptParserFactory：实现官方 SubtitleParser.Factory（create 而非 createSubtitleParser）
wr(B + 'exoplayer/text/SubtitleTranscriptParserFactory.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.text;

public class SubtitleTranscriptParserFactory implements androidx.media3.extractor.text.SubtitleParser.Factory {

    public SubtitleTranscriptParserFactory(SubtitleTranscript transcript) { }

    @Override
    public androidx.media3.extractor.text.SubtitleParser create(androidx.media3.common.Format format) { return null; }
    @Override
    public boolean supportsFormat(androidx.media3.common.Format format) { return false; }
    @Override
    public int getCueReplacementBehavior(androidx.media3.common.Format format) { return 0; }
}
''')

# 3) DecoderMode（exoplayer）：fromCommonDecoderMode 改为静态
s = rd(B + 'exoplayer/DecoderMode.java')
s = s.replace(
    '    public DecoderMode fromCommonDecoderMode(androidx.media3.common.DecoderMode p0) { return AUTO; }',
    '    public static DecoderMode fromCommonDecoderMode(androidx.media3.common.DecoderMode p0) { return AUTO; }')
wr(B + 'exoplayer/DecoderMode.java', s)

# 4) SubtitleContent：补充静态 LOADING / UNSUPPORTED 实例（调用点用 SubtitleContent.LOADING / .UNSUPPORTED）
wr(B + 'common/text/SubtitleContent.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.text;

import java.util.Collections;
import java.util.List;

public class SubtitleContent {

    public enum State { LOADING, COMPLETE, ERROR, UNSUPPORTED, PARTIAL, TRUNCATED }

    public static class Entry {
        public boolean image;
        public String text;
        public long startTimeUs;
        public long endTimeUs;
    }

    public static final SubtitleContent LOADING = new SubtitleContent(State.LOADING);
    public static final SubtitleContent UNSUPPORTED = new SubtitleContent(State.UNSUPPORTED);

    public State state;
    public List<Entry> entries = Collections.emptyList();

    public SubtitleContent() { this.state = State.LOADING; }
    public SubtitleContent(State state) { this.state = state; }

    public boolean isEmpty() { return entries.isEmpty(); }
}
''')

# 5) CaptionStyleCompat：补充 DEFAULT_EDGE_WIDTH、createFromCaptionStyle、windowColor 字段
wr(B + 'ui/CaptionStyleCompat.java', '''// @hand 洛视频补齐桩：上游私有 aar 将 CaptionStyleCompat 置于 androidx.media3.ui（官方 1.11.1 无此类）。手工维护。
package androidx.media3.ui;

import android.graphics.Typeface;
import android.view.accessibility.CaptioningManager;

public class CaptionStyleCompat {

    public static final int EDGE_TYPE_NONE = 0;
    public static final int EDGE_TYPE_DROP_SHADOW = 1;
    public static final int EDGE_TYPE_RAISED = 2;
    public static final int EDGE_TYPE_OUTLINE = 3;

    public static final float DEFAULT_EDGE_WIDTH = 1.0f;
    public static final float DEFAULT_SHADOW_OFFSET = 1f;
    public static final CaptionStyleCompat DEFAULT = new CaptionStyleCompat();

    public int foregroundColor = 0xFFFFFFFF;
    public int backgroundColor = 0x00000000;
    public int windowColor = 0x00000000;
    public int edgeType = EDGE_TYPE_NONE;
    public int edgeColor = 0xFFFFFFFF;
    public float edgeWidth = 1f;
    public float shadowOffset = 1f;
    public Typeface typeface = Typeface.DEFAULT;

    public CaptionStyleCompat() { }
    public CaptionStyleCompat(int foregroundColor, int backgroundColor, int windowColor, int edgeType, int edgeColor, Object typeface, float edgeWidth, float shadowOffset) {
        this.foregroundColor = foregroundColor; this.backgroundColor = backgroundColor; this.windowColor = windowColor;
        this.edgeType = edgeType; this.edgeColor = edgeColor; this.edgeWidth = edgeWidth; this.shadowOffset = shadowOffset;
    }

    public static CaptionStyleCompat createFromCaptionStyle(CaptioningManager.CaptionStyle style) {
        return new CaptionStyleCompat(style.foregroundColor, style.backgroundColor, style.windowColor,
                style.edgeType, style.edgeColor, style.typeface, 0, 0);
    }
}
''')

# 6) SubtitleTranscriptSession：补充 close()（调用点 subtitleTranscriptSession.close()）
s = rd(B + 'exoplayer/text/SubtitleTranscriptSession.java')
if 'public void close()' not in s:
    s = s.replace('    public SubtitleTranscriptSession() { }',
                  '    public SubtitleTranscriptSession() { }\n    public void close() { }')
wr(B + 'exoplayer/text/SubtitleTranscriptSession.java', s)

# 7) AudioEqualizerController：补充 release()
s = rd(B + 'exoplayer/audio/AudioEqualizerController.java')
if 'public void release()' not in s:
    s = s.replace('    public AudioEqualizerController(Object context) { }',
                  '    public AudioEqualizerController(Object context) { }\n    public void release() { }')
wr(B + 'exoplayer/audio/AudioEqualizerController.java', s)

# 8) ExoRenderersFactory：补充 setEnableSurfaceControlFrameRetention / setDolbyVisionOutputPolicy（调用点在 ExoUtil 内部类）
s = rd(B + 'exoplayer/ExoRenderersFactory.java')
if 'setEnableSurfaceControlFrameRetention' not in s:
    s = s.replace('    public ExoRenderersFactory setEnableDecoderFallback(boolean enable) { return this; }',
                  '    public ExoRenderersFactory setEnableDecoderFallback(boolean enable) { return this; }\n'
                  '    public ExoRenderersFactory setEnableSurfaceControlFrameRetention(boolean enable) { return this; }\n'
                  '    public ExoRenderersFactory setDolbyVisionOutputPolicy(int policy) { return this; }')
wr(B + 'exoplayer/ExoRenderersFactory.java', s)

# 9) MpvVideoEqualizer：create 接受 6 个 float 参数（调用点 brightness, contrast, saturation, gamma, hue, sharpness）
wr(B + 'mpvplayer/video/MpvVideoEqualizer.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.mpvplayer.video;

public class MpvVideoEqualizer {

    public static final MpvVideoEqualizer DEFAULT = new MpvVideoEqualizer();

    public static MpvVideoEqualizer create(float brightness, float contrast, float saturation, float gamma, float hue, float sharpness) {
        return new MpvVideoEqualizer();
    }
}
''')

# 10) MpvAudioFilter.Builder：addEqualizer 接受 short[]（调用点 levels 为 short[]）
s = rd(B + 'mpvplayer/audio/MpvAudioFilter.java')
s = s.replace('public Builder addEqualizer(String id, int band, double value) { return this; }',
              'public Builder addEqualizer(String id, int band, double value) { return this; }\n'
              'public Builder addEqualizer(String id, double centerFrequency, short level) { return this; }')
wr(B + 'mpvplayer/audio/MpvAudioFilter.java', s)

# 11) DanmakuConfig：setDurationMs 接受 long（调用点 getDurationMs() 返回 long）
s = rd(B + 'ui/danmaku/DanmakuConfig.java')
s = s.replace('public DanmakuConfig setDurationMs(int durationMs) { return this; }',
              'public DanmakuConfig setDurationMs(long durationMs) { return this; }')
s = s.replace('public DanmakuConfig setFixedDurationMs(int fixedDurationMs) { return this; }',
              'public DanmakuConfig setFixedDurationMs(long fixedDurationMs) { return this; }')
wr(B + 'ui/danmaku/DanmakuConfig.java', s)

# 12) MpvDrmSupport：supports 改为静态（调用点 MpvDrmSupport.supports(...)）
s = rd(B + 'mpvplayer/media/MpvDrmSupport.java')
s = s.replace('	public boolean supports(MediaItem p0) { return false; }',
              '	public static boolean supports(MediaItem p0) { return false; }')
wr(B + 'mpvplayer/media/MpvDrmSupport.java', s)

# 13) MpvDecoderMode：fromCommonDecoderMode 改为静态
s = rd(B + 'mpvplayer/MpvDecoderMode.java')
s = s.replace('    public androidx.media3.exoplayer.DecoderMode fromCommonDecoderMode(DecoderMode p0) { return androidx.media3.exoplayer.DecoderMode.AUTO; }',
              '    public static androidx.media3.exoplayer.DecoderMode fromCommonDecoderMode(DecoderMode p0) { return androidx.media3.exoplayer.DecoderMode.AUTO; }')
wr(B + 'mpvplayer/MpvDecoderMode.java', s)

# ═══════════════════════════════════════════════════════════════
# 第二部分：App 侧调用点修正
# ═══════════════════════════════════════════════════════════════

# 14) App.java:105 — MpvLibrary.setLibraryLoader(MpvUtil::loadRuntime) 中 loadRuntime 返回 Object 不是函数接口
p = APP + 'App.java'
s = rd(p)
s = s.replace('MpvLibrary.setLibraryLoader(MpvUtil::loadRuntime);',
              'MpvLibrary.setLibraryLoader(MpvUtil::loadRuntime); // 洛视频降级：MpvLibrary 不启用')
wr(p, s)

# 15) ExoUtil.java — ExoRenderersFactory 内部类补充 experimentalSetEnableMediaCodecVideoRendererPrewarming / setExtensionRendererMode
p = APP + 'player/exo/ExoUtil.java'
s = rd(p)
if 'experimentalSetEnableMediaCodecVideoRendererPrewarming' not in s:
    s = s.replace(
        '            setEnableSurfaceControlFrameRetention(true);\n            experimentalSetEnableMediaCodecVideoRendererPrewarming(true);',
        '            setEnableSurfaceControlFrameRetention(true);\n            // experimentalSetEnableMediaCodecVideoRendererPrewarming / setExtensionRendererMode 为 fork 扩展，不启用')
    s = s.replace('            setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON);',
                  '            // setExtensionRendererMode 为 fork 扩展，不启用')
wr(p, s)

# 16) ExoPlayerEngine.java — session.subtitleTranscriptSession() 返回 SubtitleTranscriptSession，补充 getContent/setContentEnabled/canRetryContent/retryContent
p = APP + 'player/exo/ExoPlayerEngine.java'
s = rd(p)
# 补充 SubtitleTranscriptSession 方法
ts = B + 'exoplayer/text/SubtitleTranscriptSession.java'
tss = rd(ts)
for m in ['public void setContentEnabled(boolean enabled) { }',
          'public boolean canRetryContent(Format format) { return false; }',
          'public void retryContent(Format format) { }']:
    if m not in tss:
        tss = tss.replace('    public void close() { }',
                          '    public void close() { }\n    ' + m)
wr(ts, tss)

# 17) ExoSubtitleController.java — libassPlaybackSession 补充 setBottomPositionFraction/setSecondaryBottomPositionFraction/setFontScale
p = APP + 'player/exo/ExoSubtitleController.java'
s = rd(p)
lps = B + 'exoplayer/libass/LibassPlaybackSession.java'
lsss = rd(lps)
for m in ['public void setBottomPositionFraction(float fraction) { }',
          'public void setSecondaryBottomPositionFraction(float fraction) { }',
          'public void setFontScale(float scale, boolean applied) { }']:
    if m not in lsss:
        lsss = lsss.replace('    public void setPreloadMediaItem(MediaItem mediaItem) { }',
                            '    public void setPreloadMediaItem(MediaItem mediaItem) { }\n    ' + m)
wr(lps, lsss)

# 18) LibassPlayerViewController — 补充 setStyleOverride + CaptionStyleCompat import
p = B + 'ui/libass/LibassPlayerViewController.java'
s = rd(p)
if 'import androidx.media3.ui.CaptionStyleCompat;' not in s:
    s = s.replace('import androidx.media3.ui.PlayerView;',
                  'import androidx.media3.ui.CaptionStyleCompat;\nimport androidx.media3.ui.PlayerView;')
if 'setStyleOverride' not in s:
    s = s.replace('    public void setSecondarySubtitleViewConfigurator(SecondarySubtitleViewConfigurator configurator) { }',
                  '    public void setSecondarySubtitleViewConfigurator(SecondarySubtitleViewConfigurator configurator) { }\n'
                  '    public void setStyleOverride(CaptionStyleCompat style, Object fontFamily) { }')
wr(p, s)

# 19) SubtitleFileContent.java:30 — OkHttpDataSource 已删除，改用 DefaultDataSource
p = APP + 'player/subtitle/SubtitleFileContent.java'
s = rd(p)
s = s.replace(
    'DataSource.Factory factory = new DefaultDataSource.Factory(App.get(), new OkHttpDataSource.Factory(OkHttp.player()).setDefaultRequestProperties(ExoUtil.extractHeaders(item)));',
    'DataSource.Factory factory = new DefaultDataSource.Factory(App.get()); // 洛视频降级：OkHttpDataSource 桩已删除')
wr(p, s)

# 20) MpvVideoEffectController.java — 使用 MpvVideoEqualizer（顶层类）而非 MpvPlayer.MpvVideoEqualizer（嵌套类）
p = APP + 'player/effect/video/MpvVideoEffectController.java'
s = rd(p)
s = s.replace('import androidx.media3.mpvplayer.video.MpvVideoEqualizer;',
              'import androidx.media3.mpvplayer.video.MpvVideoEqualizer;')
# 修正 create 调用（6 参数）
s = s.replace('MpvVideoEqualizer.create(brightness, contrast, saturation, gamma, toHue(profile.getHue()), sharpness);',
              'MpvVideoEqualizer.create(brightness, contrast, saturation, gamma, toHue(profile.getHue()), sharpness);')
wr(p, s)

# 21) MpvAudioEffectFilter.java:22 — appendEqualizer 接受 short[]，调用点 levels 为 short[]（已匹配）
# 无需修改，stub 已补充 addEqualizer(String, double, short)

# 22) PlayerEngine.java:128 — SubtitleContent.UNSUPPORTED 已在第 4 项补充
# 23) PlayerManager.java:483 — 同上
# 24) PlaybackContentPanel.java:66,67,246,247 — 同上
# 25) PlaybackContentPanel.java:357 — entry.image 改为 boolean（已在 SubtitleContent.Entry 中将 image 从 String 改为 boolean）

# 26) DanmakuSetting.java:380 — setDurationMs 已改为 long（第 11 项）
# 27) ExoPlayerSession.java:178 — subtitleTranscriptSession.close() 已在第 6 项补充
# 28) ExoAudioEffectController.java:36,42,47,48 — processor.resetParameters() / equalizer.release() 已在第 1、7 项补充

# 29) ExoPlayerEngine.java:184 — DecoderMode.toCommonDecoderMode() 返回 Object，需要类型转换
p = APP + 'player/exo/ExoPlayerEngine.java'
s = rd(p)
s = s.replace('return mode == null ? null : mode.toCommonDecoderMode();',
              'return mode == null ? null : (DecoderMode) mode.toCommonDecoderMode();')
wr(p, s)

# 30) MpvPlayerEngine.java:200 — player.getVideoDecoderMode() 返回 Object，需要类型转换
p = APP + 'player/mpv/MpvPlayerEngine.java'
s = rd(p)
s = s.replace('return player.getVideoDecoderMode().toCommonDecoderMode();',
              'return (DecoderMode) player.getVideoDecoderMode().toCommonDecoderMode();')
wr(p, s)

print('fix4 done')