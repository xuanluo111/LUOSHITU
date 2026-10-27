#!/usr/bin/env python3
# S1 收敛批次三：全量签名/静态/构造器修正（缩进无关）
import re, os
B = 'app/src/main/java/androidx/media3/'
n = [0]
def rd(p): return open(p, encoding='utf-8').read()
def wr(p, s): open(p, 'w', encoding='utf-8').write(s); n[0] += 1
def rw(p, pairs):
    s = rd(p)
    for a, b in pairs:
        s2 = re.sub(a, b, s, flags=re.M)
        if s2 != s: n[0] += 1
        s = s2
    wr(p, s)
def addimp(p, imp):
    s = rd(p)
    if imp in s: return
    wr(p, re.sub(r'(package [\w.]+;)', lambda m: m.group(1) + '\n\n' + imp, s, count=1))
def ensure(path, anchor, addition):
    s = rd(path)
    if addition.strip() in s: return
    wr(path, s.replace(anchor, anchor + '\n' + addition, 1))

# 1) 全桩去重：同名方法保留"信息更多"的一行
for dirpath, _, files in os.walk('app/src/main/java/androidx'):
    for fn in files:
        if not fn.endswith('.java'): continue
        p = os.path.join(dirpath, fn)
        s = rd(p); orig = s
        seen = {}
        remove = []
        for m in re.finditer(r'\n[ \t]*public ([^\n{]+\()([^\n)]*)\) \{[^}\n]*\}', s):
            params = m.group(2)
            nm = re.search(r'(\w+)\(', m.group(0)).group(1)
            key = nm
            quality = len(re.findall(r'\w+', params)) * 2 + (0 if 'p0' in params or 'Object' in params else 1)
            if key in seen:
                if quality > seen[key][0]:
                    remove.append(seen[key][1]); seen[key] = (quality, m.group(0))
                else:
                    remove.append(m.group(0))
            else:
                seen[key] = (quality, m.group(0))
        for r in remove:
            s = s.replace(r, '\n', 1)
        s = re.sub(r'\n{3,}', '\n\n', s)
        if s != orig: wr(p, s)

# 2) MpvPlayer 终版签名
rw(B + 'mpvplayer/MpvPlayer.java', [
  (r'public Object getAudioChannelCount\(\) \{ return null; \}', 'public int getAudioChannelCount() { return 0; }'),
  (r'public Object getSubtitleSelectionState\(\) \{ return null; \}', 'public androidx.media3.common.text.SubtitleSelectionState getSubtitleSelectionState() { return null; }'),
  (r'public void setVideoDecoderMode\(DecoderMode p0\) \{ \}', 'public void setVideoDecoderMode(androidx.media3.exoplayer.DecoderMode p0) { }'),
  (r'public Object getVideoDecoderMode\(\) \{ return null; \}', 'public androidx.media3.exoplayer.DecoderMode getVideoDecoderMode() { return null; }'),
  (r'public void setLogListener\(int p0, LogListener p1\) \{ \}', 'public void setLogListener(int level, LogListener listener) { }'),
  (r'public Object loadScript\([^)]*\) \{ return null; \}', 'public boolean loadScript(String path, ScriptLoadListener listener) { return false; }'),
  (r'public Object getInputBindings\(\) \{ return null; \}', 'public String getInputBindings() { return null; }'),
  (r'public Object runScriptBinding\([^)]*\) \{ return null; \}', 'public boolean runScriptBinding(String binding) { return false; }'),
  (r'public void setAudioOutputListener\(Object p0\) \{ \}', 'public void setAudioOutputListener(AudioOutputListener listener) { }'),
  (r'public void setAudioFilterListener\(Object p0\) \{ \}', 'public void setAudioFilterListener(AudioFilterListener listener) { }'),
  (r'public void setAudioFilter\(MpvAudioFilter p0\) \{ \}', 'public boolean setAudioFilter(MpvAudioFilter p0) { return false; }'),
  (r'public boolean isAvailable\(\) \{ return false; \}', 'public static boolean isAvailable() { return false; }'),
  (r'(public static class Builder \{\n)', r'\1        public Builder(Object context) { }\n        public Builder setVideoDecoderMode(androidx.media3.mpvplayer.MpvDecoderMode mode) { return this; }\n        public Builder setConfig(Object config) { return this; }\n        public MpvPlayer build() { return null; }\n'),
  (r'import androidx.media3.common.MediaItem;', 'import androidx.media3.common.MediaItem;\nimport androidx.media3.common.Player;\nimport androidx.media3.common.text.SubtitleSelectionState;\nimport androidx.media3.common.text.SubtitleContent;\nimport androidx.media3.common.text.SubtitleOffsets;\nimport androidx.media3.common.TrackSelectionOverride;\nimport androidx.media3.datasource.DataSource;\nimport androidx.media3.exoplayer.DecoderMode;\nimport androidx.media3.mpvplayer.audio.MpvAudioFilter;')])
addimp(B + 'mpvplayer/MpvPlayer.java', 'import androidx.media3.common.Player;')

# 3) MpvDecoderMode 常量
rw(B + 'mpvplayer/MpvDecoderMode.java', [
  (r'(public class MpvDecoderMode \{\n)', r'\1    public static final MpvDecoderMode AUTO = new MpvDecoderMode();\n    public static final MpvDecoderMode HARDWARE = new MpvDecoderMode();\n    public static final MpvDecoderMode SOFTWARE = new MpvDecoderMode();\n    public static final MpvDecoderMode FFMPEG = new MpvDecoderMode();\n')])

# 4) AudioChannelMode 终版（静态全量）
rw(B + 'common/audio/AudioChannelMode.java', [
  (r'public class AudioChannelMode \{\n(?:    public static int resolve\(int mode, int channelCount\) \{ return mode; \}\n)?',
   '''public class AudioChannelMode {

    public static final int AUTO = 0;
    public static final int STEREO = 1;
    public static final int MONO = 2;
    public static final int REVERSE = 3;

    public static int resolve(int mode, int channelCount) { return mode; }
    public static int clamp(int mode) { return mode; }
    public static boolean isAuto(int mode) { return mode == AUTO; }
    public static boolean canBalance(int mode) { return false; }
    public static boolean isAvailable(int mode, int channelCount) { return false; }
    public static boolean isAvailable(int channelCount) { return false; }
''')])

# 5) AudioChannelMix 静态方法
p = B + 'common/audio/AudioChannelMix.java'
s = rd(p) if os.path.exists(p) else None
if s is None:
    wr(p, '''// @hand 洛视频补齐桩：docs/S0-立项报告.md 第 9 节。
package androidx.media3.common.audio;

public class AudioChannelMix {
    public static AudioChannelMix createFrontCenterGainMix(int channelCount, Object factor) { return new AudioChannelMix(); }
    public static AudioChannelMix createStereoMix(int channelCount, boolean reverse) { return new AudioChannelMix(); }
    public static AudioChannelMix createMonoMix(int channelCount) { return new AudioChannelMix(); }
    public static AudioChannelMix createFrontBalanceMix(int channelCount, float balance) { return new AudioChannelMix(); }
    public static AudioChannelMix compose(Object a, Object b) { return new AudioChannelMix(); }
}
''')
else:
    if 'createFrontCenterGainMix' not in s:
        s = s.replace('public class AudioChannelMix {', '''public class AudioChannelMix {
    public static AudioChannelMix createFrontCenterGainMix(int channelCount, Object factor) { return new AudioChannelMix(); }
    public static AudioChannelMix createStereoMix(int channelCount, boolean reverse) { return new AudioChannelMix(); }
    public static AudioChannelMix createMonoMix(int channelCount) { return new AudioChannelMix(); }
    public static AudioChannelMix createFrontBalanceMix(int channelCount, float balance) { return new AudioChannelMix(); }
    public static AudioChannelMix compose(Object a, Object b) { return new AudioChannelMix(); }''')
        wr(p, s)

# 6) AudioEffectParameters 静态与 getter
rw(B + 'common/audio/AudioEffectParameters.java', [
  (r'public float getBalance\(\) \{ return null; \}', 'public float getBalance() { return 0f; }'),
  (r'public int getBoostMillibels\(\) \{ return null; \}', 'public int getBoostMillibels() { return 0; }'),
  (r'public float getCenterGainFactor\(\) \{ return null; \}', 'public float getCenterGainFactor() { return 0f; }'),
  (r'public int getChannelMode\(\) \{ return null; \}', 'public int getChannelMode() { return 0; }'),
  (r'public short\[\] getEqualizerLevelsMillibels\(\) \{ return null; \}', 'public short[] getEqualizerLevelsMillibels() { return new short[0]; }'),
  (r'public int getPreampMillibels\(\) \{ return null; \}', 'public int getPreampMillibels() { return 0; }'),
  (r'public float getStability\(\) \{ return null; \}', 'public float getStability() { return 0f; }'),
  (r'(public class AudioEffectParameters \{\n)', r'\1    public static final AudioEffectParameters DEFAULT = new AudioEffectParameters();\n    public static boolean isCenterGainAvailable(int channelCount) { return false; }\n')])

# 7) MpvVideoEqualizer.create
rw(B + 'mpvplayer/video/MpvVideoEqualizer.java', [
  (r'(public static final MpvVideoEqualizer DEFAULT = new MpvVideoEqualizer\(\);\n)',
   r'\1    public static MpvVideoEqualizer create(float brightness, float contrast, float saturation, float gamma, float hue) { return new MpvVideoEqualizer(); }\n')])

# 8) MpvAudioFilter.EMPTY（确保 static final）
rw(B + 'mpvplayer/audio/MpvAudioFilter.java', [
  (r'public static final MpvAudioFilter EMPTY = new MpvAudioFilter\(\);', 'public static final MpvAudioFilter EMPTY = new MpvAudioFilter();')])
if 'EMPTY' not in rd(B + 'mpvplayer/audio/MpvAudioFilter.java'):
    wr(B + 'mpvplayer/audio/MpvAudioFilter.java', '''// @hand 洛视频补齐桩：docs/S0-立项报告.md 第 9 节。
package androidx.media3.mpvplayer.audio;

public class MpvAudioFilter {
    public MpvAudioFilter() { }
    public static final MpvAudioFilter EMPTY = new MpvAudioFilter();

    public static class Builder {
        public Builder addDynamicNormalization(String id, double a, double b) { return this; }
        public Builder addCompressor(String id, float a, float b, float c, float d) { return this; }
        public Builder addVolume(String id, double level) { return this; }
        public Builder addEqualizer(String id, int band, double value) { return this; }
        public Builder addLimiter(String id, double limit) { return this; }
        public Builder addRuntimeChannelMix(String id, float[][] mix) { return this; }
        public MpvAudioFilter build() { return new MpvAudioFilter(); }
    }

    public boolean hasEffect() { return false; }
    public boolean hasBands() { return false; }
    public boolean hasCenterGain(int band) { return false; }
    public float getStability() { return 0f; }
    public float getBalance() { return 0f; }
    public float getBoostMillibels() { return 0f; }
    public float getCenterGainFactor() { return 0f; }
    public int getChannelMode() { return 0; }
    public short[] getEqualizerLevelsMillibels() { return new short[0]; }
    public int getPreampMillibels() { return 0; }
}
''')
# addRuntimeChannelMix(float[][]) 修正
rw(B + 'mpvplayer/audio/MpvAudioFilter.java', [
  (r'public Builder addRuntimeChannelMix\(String id, float\[\] mix\) \{ return this; \}', 'public Builder addRuntimeChannelMix(String id, float[][] mix) { return this; }')])

# 9) LocalClearKeyLicense 双静态重载
pass  # LocalClearKeyLicense 已直接重写
# 上面方式不安全，改为直接写文件
wr(B + 'common/util/LocalClearKeyLicense.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 本地 ClearKey 许可解析：本产品不启用 DRM 路径，恒返回空。
package androidx.media3.common.util;

public class LocalClearKeyLicense {

    public static byte[][] parse(androidx.media3.common.MediaItem.DrmConfiguration config) { return new byte[0][]; }
    public static String parse(String key) { return ""; }
}
''')

# 10) ProgressiveIsoCache 静态直通
rw(B + 'datasource/ProgressiveIsoCache.java', [
  (r'public Object withDiskCache\(DataSource\.Factory p0, Cache p1\) \{ return null; \}',
   'public static androidx.media3.datasource.DataSource.Factory withDiskCache(androidx.media3.datasource.DataSource.Factory factory, Object cache) { return factory; }'),
  (r'public static androidx\.media3\.datasource\.DataSource\.Factory withDiskCache\(androidx\.media3\.datasource\.DataSource\.Factory factory, Object cache\) \{ return null; \}',
   'public static androidx.media3.datasource.DataSource.Factory withDiskCache(androidx.media3.datasource.DataSource.Factory factory, Object cache) { return factory; }')])

# 11) IsoDiscPreparation.isIso 静态
rw(B + 'exoplayer/iso/IsoDiscPreparation.java', [
  (r'public boolean isIso\(MediaItem p0\) \{ return false; \}', 'public static boolean isIso(MediaItem p0) { return false; }'),
  (r'public static boolean isIso\(MediaItem p0\) \{ return false; \}\n\tpublic static boolean isIso\(MediaItem p0\) \{ return false; \}',
   'public static boolean isIso(MediaItem p0) { return false; }')])

# 12) IsoUri.withEditionIndex 静态
rw(B + 'datasource/IsoUri.java', [
  (r'public Object withEditionIndex\(Uri p0, int p1\) \{ return null; \}', 'public static Uri withEditionIndex(Uri p0, int p1) { return p0; }'),
  (r'public Uri withEditionIndex\(Uri p0, int p1\) \{ return null; \}', 'public static Uri withEditionIndex(Uri p0, int p1) { return p0; }')])

# 13) LibassFontConfig.write / LibassFontFile.getFamilyName 静态
rw(B + 'exoplayer/libass/LibassFontConfig.java', [
  (r'public Object write\(File p0, File p1, Object p2\) \{ return null; \}', 'public static Object write(File p0, File p1, Object p2) { return null; }'),
  (r'public \w+ write\(File p0, File p1, Object p2\) \{ return null; \}', 'public static Object write(File p0, File p1, Object p2) { return null; }')])
rw(B + 'exoplayer/libass/LibassFontFile.java', [
  (r'public String getFamilyName\(File p0\) \{ return null; \}', 'public static String getFamilyName(File p0) { return null; }')])

# 14) SubtitleTranscriptSession 终版
wr(B + 'exoplayer/text/SubtitleTranscriptSession.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.text;

import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.text.SubtitleContent;
import androidx.media3.exoplayer.ExoPlayer;

public class SubtitleTranscriptSession {

    public SubtitleTranscriptSession() { }
    public void setLoader(Object loader) { }
    public void setPlayer(ExoPlayer player) { }
    public SubtitleTranscript forMediaItem(MediaItem mediaItem) { return null; }
    public void setPreloadMediaItem(MediaItem mediaItem) { }
    public SubtitleContent getContent(Format format, long offsetMs) { return null; }
}
''')

# 15) LibassPlaybackSession：ctor + Renderer + MediaComponents 类型
rw(B + 'exoplayer/libass/LibassPlaybackSession.java', [
  (r'public androidx\.media3\.exoplayer\.Renderer createClockRenderer\(\) \{ return null; \}', 'public androidx.media3.exoplayer.Renderer createClockRenderer() { return null; }'),
  (r'public androidx\.media3\.extractor\.text\.SubtitleParser\.Factory subtitleParserFactory;', 'public androidx.media3.extractor.text.SubtitleParser.Factory subtitleParserFactory;\n        public androidx.media3.exoplayer.source.ExtractorsFactory asExtractorsFactory() { return null; }')])
addimp(B + 'exoplayer/libass/LibassPlaybackSession.java', 'import androidx.media3.exoplayer.source.ExtractorsFactory;')
# MediaComponents.extractorsFactory 类型改为 ExtractorsFactory
rw(B + 'exoplayer/libass/LibassPlaybackSession.java', [
  (r'public (ExtractorsFactory|Object) extractorsFactory;', 'public ExtractorsFactory extractorsFactory;')])

# 16) LibassSubtitleController：getSubtitleSelectionState + setSecondary...TrackSelectionOverride ✓ + setSecondarySubtitleViewConfigurator SAM
rw(B + 'exoplayer/libass/LibassSubtitleController.java', [
  (r'public Object getSubtitleSelectionState\(\) \{ return null; \}', 'public androidx.media3.common.text.SubtitleSelectionState getSubtitleSelectionState() { return null; }')])
# LibassPlayerViewController：SAM + setSecondarySubtitleViewConfigurator
rw(B + 'ui/libass/LibassPlayerViewController.java', [
  (r'(public class LibassPlayerViewController \{\n)',
   r'\1    public interface SecondarySubtitleViewConfigurator { void apply(); }\n    public void setSecondarySubtitleViewConfigurator(SecondarySubtitleViewConfigurator configurator) { }\n')])

# 17) AudioEqualizerController：ctor + apply + getProcessor
rw(B + 'exoplayer/audio/AudioEqualizerController.java', [
  (r'public AudioEqualizerController\(Object context\) \{ \}', 'public AudioEqualizerController(Object context) { }\n    public boolean apply(Object player, Object parameters) { return false; }\n    public androidx.media3.common.audio.AudioProcessor getProcessor() { return null; }')])

# 18) AudioEffectProcessor → BaseAudioProcessor
wr(B + 'common/audio/AudioEffectProcessor.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.audio;

public class AudioEffectProcessor extends androidx.media3.common.audio.BaseAudioProcessor {
}
''')

# 19) ColorToneAdjustEffect / DetailAdjustEffect implements Effect
rw(B + 'effect/ColorToneAdjustEffect.java', [
  (r'public class ColorToneAdjustEffect \{', 'public class ColorToneAdjustEffect implements androidx.media3.common.Effect {\n    @Override\n    public long getEffectId() { return 0; }')])
rw(B + 'effect/DetailAdjustEffect.java', [
  (r'public class DetailAdjustEffect \{', 'public class DetailAdjustEffect implements androidx.media3.common.Effect {\n    @Override\n    public long getEffectId() { return 0; }')])

# 20) MpvVideoEqualizer 添加缺失实例成员
rw(B + 'mpvplayer/video/MpvVideoEqualizer.java', [
  (r'(public static MpvVideoEqualizer create\([^)]*\) \{ return new MpvVideoEqualizer\(\); \}\n)',
   r'\1    public void applyTo(Object builder) { }\n')])

# 21) DefaultSubtitleContentLoader / SubtitleTranscriptParserFactory ctor 修正
wr(B + 'exoplayer/text/DefaultSubtitleContentLoader.java', '''// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.text;

import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.text.SubtitleContent;

public class DefaultSubtitleContentLoader {

    public DefaultSubtitleContentLoader(androidx.media3.datasource.DataSource.Factory factory) { }

    public SubtitleContent load(MediaItem item, android.net.Uri uri, Format format) { return null; }
}
''')
rw(B + 'exoplayer/text/SubtitleTranscriptParserFactory.java', [
  (r'public SubtitleTranscriptParserFactory\(Object transcript\) \{ \}', 'public SubtitleTranscriptParserFactory(Object transcript) { }')])

# 22) SecondaryTextTrackSelector.Factory ctor
rw(B + 'exoplayer/trackselection/SecondaryTextTrackSelector.java', [
  (r'public static class Factory \{\n        public Factory\(Object delegateFactory\) \{ \}\n    \}', 'public static class Factory {\n        public Factory(Object delegateFactory) { }\n    }')])
if 'public Factory(Object delegateFactory)' not in rd(B + 'exoplayer/trackselection/SecondaryTextTrackSelector.java'):
    s = rd(B + 'exoplayer/trackselection/SecondaryTextTrackSelector.java')
    s = s.replace('public class SecondaryTextTrackSelector {', 'public class SecondaryTextTrackSelector {\n\n    public static class Factory {\n        public Factory(Object delegateFactory) { }\n    }')
    wr(B + 'exoplayer/trackselection/SecondaryTextTrackSelector.java', s)

# 23) DecodeTrackSelector extends DefaultTrackSelector
p = B + 'exoplayer/trackselection/DecodeTrackSelector.java'
s = rd(p)
if 'extends' not in s:
    s = re.sub(r'public class DecodeTrackSelector \{', 'public class DecodeTrackSelector extends androidx.media3.exoplayer.trackselection.DefaultTrackSelector {\n    public DecodeTrackSelector(android.content.Context context) { super(context); }', s)
    wr(p, s)

# 24) IsoNavigationPlayerController：getPlayer 返回 ExoPlayer + prepareMediaSource 参数
rw(B + 'exoplayer/iso/IsoNavigationPlayerController.java', [
  (r'public Object getPlayer\(\) \{ return null; \}', 'public ExoPlayer getPlayer() { return null; }'),
  (r'public void prepareMediaSource\(App p0, MediaSource p1, Factory p2\) \{ \}', 'public MediaSource prepareMediaSource(App p0, MediaSource p1, androidx.media3.datasource.DataSource.Factory p2) { return null; }')])

# 25) BdjRuntime.Status
rw(B + 'exoplayer/iso/BdjRuntime.java', [
  (r'public static Object getStatus\(android\.app\.Application app\) \{ return null; \}',
   'public static Status getStatus(android.app.Application app) { return new Status(); }\n\n    public static class Status { public boolean restartRequired; }')])

# 26) MpvVideoEqualizer DEFAULT 确认
if 'public static final MpvVideoEqualizer DEFAULT' not in rd(B + 'mpvplayer/video/MpvVideoEqualizer.java'):
    s = rd(B + 'mpvplayer/video/MpvVideoEqualizer.java')
    s = s.replace('public class MpvVideoEqualizer {', 'public class MpvVideoEqualizer {\n    public static final MpvVideoEqualizer DEFAULT = new MpvVideoEqualizer();')
    wr(B + 'mpvplayer/video/MpvVideoEqualizer.java', s)

# 27) SubtitleView 遮蔽确认（应不存在）
print('fix3 done, files touched:', n[0])
