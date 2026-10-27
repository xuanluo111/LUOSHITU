// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
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
