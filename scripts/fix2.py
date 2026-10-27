#!/usr/bin/env python3
# S1 收敛定向修复批次二：去重 + 类型精化 + import 补齐（只写 androidx 桩）
import re, os
STUB = 'app/src/main/java/androidx/media3'
n = [0]

def rd(p): return open(p, encoding='utf-8').read()
def wr(p, s): open(p, 'w', encoding='utf-8').write(s); n[0] += 1
def addimp(p, imp):
    s = rd(p)
    if imp in s: return
    s = re.sub(r'(package [\w.]+;)', lambda m: m.group(1) + '\n\n' + imp, s, count=1)
    wr(p, s)
def dedupe_p0(rel):
    p = STUB + '/' + rel
    if not os.path.exists(p): return
    s = rd(p); orig = s
    for m in re.finditer(r'\n\tpublic [^\n]*\b(\w+)\((?:[^)]*)? p0\) \{ [^\n]*\}', s):
        name = m.group(1)
        rest = s.replace(m.group(0), '', 1)
        if re.search(r'public [^\n]*\b' + name + r'\((?!.* p0\))', rest):
            s = rest
    for m in re.finditer(r'\n\tpublic static Object ([A-Za-z_0-9]+);', s):
        if re.search(r'\b' + m.group(1) + r'\b\s*[=;]', s.replace(m.group(0), '', 1)):
            s = s.replace(m.group(0), '')
    if s != orig: wr(p, s)
def fix(rel, pairs):
    p = STUB + '/' + rel
    if not os.path.exists(p): print('MISS', rel); return
    s = rd(p)
    for a, b in pairs:
        s2 = re.sub(a, b, s)
        if s2 != s: n[0] += 1
        s = s2
    wr(p, s)

# 1) 去重
for rel in ['common/audio/AudioChannelMode.java','exoplayer/DecoderManager.java','exoplayer/iso/BdjRuntime.java',
            'exoplayer/text/SubtitleTranscriptSession.java','exoplayer/ExoRenderersFactory.java',
            'exoplayer/libass/LibassPlaybackSession.java','mpvplayer/MpvDecoderMode.java',
            'exoplayer/trackselection/SecondaryTextTrackSelector.java','exoplayer/libass/LibassSubtitleController.java',
            'ui/libass/LibassPlayerViewController.java','exoplayer/audio/AudioEqualizerController.java',
            'exoplayer/text/SubtitleTranscriptParserFactory.java','mpvplayer/MpvConfigFileInspector.java',
            'exoplayer/DecoderFallback.java','ui/danmaku/DanmakuPlayerViewController.java','ui/PlayerSeekView.java']:
    dedupe_p0(rel)

# 2) MpvPlayer 类型精化 + imports
fix('mpvplayer/MpvPlayer.java', [
  (r'import androidx.media3.common.MediaItem;',
   'import androidx.media3.common.MediaItem;\nimport androidx.media3.common.Format;\nimport androidx.media3.common.TrackSelectionOverride;\nimport androidx.media3.common.text.SubtitleSelectionState;\nimport androidx.media3.common.text.SubtitleContent;\nimport androidx.media3.common.text.SubtitleOffsets;\nimport androidx.media3.datasource.DataSource;\nimport androidx.media3.exoplayer.DecoderMode;\nimport androidx.media3.mpvplayer.audio.MpvAudioFilter;'),
  (r'public Object getAudioChannelCount\(\) \{ return null; \}', 'public int getAudioChannelCount() { return 0; }'),
  (r'public Object getSubtitleSelectionState\(\) \{ return null; \}', 'public SubtitleSelectionState getSubtitleSelectionState() { return null; }'),
  (r'public Object getVideoDecoderMode\(\) \{ return null; \}', 'public DecoderMode getVideoDecoderMode() { return null; }'),
  (r'public void setVideoDecoderMode\(DecoderMode p0\) \{ \}', 'public void setVideoDecoderMode(DecoderMode p0) { }'),
  (r'public void setLogListener\(int p0, LogListener p1\) \{ \}', 'public void setLogListener(int level, LogListener listener) { }'),
  (r'public Object loadScript\(String p0, ScriptLoadListener p1\) \{ return null; \}', 'public boolean loadScript(String path, ScriptLoadListener listener) { return false; }'),
  (r'public Object getInputBindings\(\) \{ return null; \}', 'public String getInputBindings() { return null; }'),
  (r'public Object runScriptBinding\([^)]*\) \{ return null; \}', 'public boolean runScriptBinding(String binding) { return false; }'),
  (r'public void setAudioOutputListener\(Object p0\) \{ \}', 'public void setAudioOutputListener(AudioOutputListener listener) { }'),
  (r'public void setAudioFilterListener\(Object p0\) \{ \}', 'public void setAudioFilterListener(AudioFilterListener listener) { }'),
  (r'public void removeListener\(MpvPlayerEngine p0\) \{ \}', 'public void removeListener(Player.Listener listener) { }'),
  (r'public void addListener\(MpvPlayerEngine p0\) \{ \}', 'public void addListener(Player.Listener listener) { }'),
  (r'public Object setVideoEffectsSupport\([^)]*\) \{ return null; \}', 'public int getVideoEffectsSupport() { return VIDEO_EFFECTS_UNSUPPORTED_RENDERER; }') if 0 else (r'$', '')])

# 3) 其它定向
fix('exoplayer/DecoderManager.java', [
  (r'import androidx.media3.common.Player;', 'import androidx.media3.common.Player;\nimport androidx.media3.exoplayer.trackselection.DecodeTrackSelector;'),
  (r'public Object getAudioMode\(\) \{ return null; \}', 'public DecoderMode getAudioMode() { return null; }'),
  (r'public Object getVideoMode\(\) \{ return null; \}', 'public DecoderMode getVideoMode() { return null; }')])
fix('exoplayer/DecoderFallback.java', [
  (r'public boolean retry\(Object error\) \{ return false; \}', 'public boolean retry(androidx.media3.exoplayer.ExoPlaybackException error) { return false; }')])
fix('exoplayer/libass/LibassSubtitleController.java', [
  (r'import androidx.media3.ui.PlayerView;', 'import androidx.media3.common.TrackSelectionOverride;\nimport androidx.media3.ui.PlayerView;')])
fix('mpvplayer/MpvDecoderMode.java', [
  (r'public static MpvDecoderMode fromCommonDecoderMode\(androidx\.media3\.common\.DecoderMode mode\) \{ return new MpvDecoderMode\(\); \}',
   'public static androidx.media3.exoplayer.DecoderMode fromCommonDecoderMode(androidx.media3.common.DecoderMode mode) { return androidx.media3.exoplayer.DecoderMode.AUTO; }')])
fix('common/text/SubtitleContent.java', [
  (r'public int state = State\.COMPLETE;', 'public State state = State.LOADING;')])
fix('ui/PlayerSeekView.java', [
  (r'public Object getTimeBar\(Object p0\) \{ return null; \}', 'public TimeBar getTimeBar() { return null; }'),
  (r'public View findViewById\(int p0\) \{ return null; \}', 'public <T extends View> T findViewById(int p0) { return null; }')])
fix('ui/danmaku/DanmakuConfig.java', [
  (r'public Builder setDurationMs\(int v\) \{ out\.durationMs = v; return this; \}', 'public Builder setDurationMs(long v) { out.durationMs = v; return this; }')])
fix('exoplayer/text/SubtitleTranscriptSession.java', [
  (r'public Object forMediaItem\(MediaItem p0\) \{ return null; \}', 'public SubtitleTranscript forMediaItem(MediaItem p0) { return null; }'),
  (r'public Object getContent\(Format p0, Object p1\) \{ return null; \}', 'public androidx.media3.common.text.SubtitleContent getContent(Format p0, long p1) { return null; }'),
  (r'public Object setPlayer\(ExoPlayer p0\) \{ return null; \}', 'public void setPlayer(ExoPlayer p0) { }')])
fix('exoplayer/ExoRenderersFactory.java', [
  (r'public void setDecoderManager\(DecoderManager p0\) \{ return this; \}', 'public ExoRenderersFactory setDecoderManager(DecoderManager p0) { return this; }'),
  (r'public void setEnableSurfaceControlFrameRetention\(boolean p0\) \{ return this; \}', 'public ExoRenderersFactory setEnableSurfaceControlFrameRetention(boolean p0) { return this; }'),
  (r'public void setDolbyVisionOutputPolicy\(int p0\) \{ return this; \}', 'public ExoRenderersFactory setDolbyVisionOutputPolicy(int p0) { return this; }'),
  (r'public void setEnableDecoderFallback\(boolean p0\) \{ \}', 'public ExoRenderersFactory setEnableDecoderFallback(boolean p0) { return this; }')])
fix('common/audio/AudioEffectParameters.java', [
  (r'public int getChannelMode\(\) \{ return null; \}', 'public int getChannelMode() { return 0; }'),
  (r'public float getStability\(\) \{ return null; \}', 'public float getStability() { return 0f; }'),
  (r'public float getBalance\(\) \{ return null; \}', 'public float getBalance() { return 0f; }'),
  (r'public int getBoostMillibels\(\) \{ return null; \}', 'public int getBoostMillibels() { return 0; }'),
  (r'public int getPreampMillibels\(\) \{ return null; \}', 'public int getPreampMillibels() { return 0; }'),
  (r'public float getCenterGainFactor\(\) \{ return null; \}', 'public float getCenterGainFactor() { return 0f; }'),
  (r'public short\[\] getEqualizerLevelsMillibels\(\) \{ return null; \}', 'public short[] getEqualizerLevelsMillibels() { return new short[0]; }')])
fix('mpvplayer/audio/MpvAudioFilter.java', [
  (r'public Object getStability\(\) \{ return null; \}', 'public float getStability() { return 0f; }'),
  (r'public Object getBalance\(\) \{ return null; \}', 'public float getBalance() { return 0f; }'),
  (r'public Object getBoostMillibels\(\) \{ return null; \}', 'public float getBoostMillibels() { return 0f; }'),
  (r'public Object getCenterGainFactor\(\) \{ return null; \}', 'public float getCenterGainFactor() { return 0f; }'),
  (r'public Object getChannelMode\(\) \{ return null; \}', 'public int getChannelMode() { return 0; }'),
  (r'public Object getEqualizerLevelsMillibels\(\) \{ return null; \}', 'public short[] getEqualizerLevelsMillibels() { return new short[0]; }'),
  (r'public Object getPreampMillibels\(\) \{ return null; \}', 'public int getPreampMillibels() { return 0; }')])

# 4) import 补齐（定向）
addimp(STUB + '/exoplayer/DecoderManager.java', 'import androidx.media3.exoplayer.trackselection.DecodeTrackSelector;')
addimp(STUB + '/exoplayer/libass/LibassSubtitleController.java', 'import androidx.media3.common.TrackSelectionOverride;')
addimp(STUB + '/mpvplayer/MpvPlayer.java', 'import androidx.media3.mpvplayer.audio.MpvAudioFilter;')

# 5) CaptionStyleCompat ctor（如缺）
p = STUB + '/ui/CaptionStyleCompat.java'
s = rd(p)
if 'public CaptionStyleCompat(int' not in s:
    s = s.replace('    public static final CaptionStyleCompat DEFAULT = new CaptionStyleCompat();',
'''    public static final float DEFAULT_SHADOW_OFFSET = 1f;

    public static final CaptionStyleCompat DEFAULT = new CaptionStyleCompat();

    public CaptionStyleCompat(int foregroundColor, int backgroundColor, int windowColor, int edgeType, int edgeColor, Object typeface, float edgeWidth, float shadowOffset) {
        this.foregroundColor = foregroundColor; this.backgroundColor = backgroundColor; this.edgeType = edgeType; this.edgeColor = edgeColor; this.edgeWidth = edgeWidth; this.shadowOffset = shadowOffset;
    }''')
    wr(p, s)
# SubtitleView 常量（官方类，确认不被本地桩遮蔽）
print('fix2 applied, files touched:', n[0])
