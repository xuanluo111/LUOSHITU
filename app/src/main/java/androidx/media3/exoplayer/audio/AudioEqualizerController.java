// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.audio;

public class AudioEqualizerController {

    public AudioEqualizerController(Object context) { }
    public void release() { }

    public boolean apply(Object player, androidx.media3.common.audio.AudioEffectParameters parameters) { return false; }
    public androidx.media3.common.audio.AudioProcessor getProcessor() { return null; }
}
