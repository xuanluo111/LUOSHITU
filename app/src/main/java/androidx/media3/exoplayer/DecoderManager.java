// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// fork 的硬解/软解动态切换运行时停用（S1 决策，EXO 单内核）：Auto 模式 = 官方默认解码行为。
package androidx.media3.exoplayer;

import android.content.Context;

import androidx.media3.common.Player;

import androidx.media3.exoplayer.trackselection.DecodeTrackSelector;

public class DecoderManager {

    private final DecoderMode audioMode;
    private final DecoderMode videoMode;

    public DecoderManager(DecoderMode audioMode, DecoderMode videoMode) {
        this.audioMode = audioMode;
        this.videoMode = videoMode;
    }

    public DecoderManager(Object audioMode, Object videoMode) {
        this(DecoderMode.AUTO, DecoderMode.AUTO);
    }

    public void attach(Player player) { }

    public DecoderMode getAudioMode() { return audioMode; }

    public DecoderMode getVideoMode() { return videoMode; }

    public DecodeTrackSelector createTrackSelector(Context context) {
        return new DecodeTrackSelector(context);
    }

    public Object selectAudioDecoder(DecoderMode mode) { return null; }

    public Object selectVideoDecoder(DecoderMode mode) { return null; }

    public void setDecoderModes(DecoderMode audioMode, DecoderMode videoMode) { }

    public Object detach() { return null; }
}
