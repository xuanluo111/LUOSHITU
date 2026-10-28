// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.audio;

import java.nio.ByteBuffer;

public class AudioEffectProcessor extends androidx.media3.common.audio.BaseAudioProcessor {

    /**
     * AudioProcessor 的硬性契约是「必须把 input 全部吃掉，并通过 replaceOutputBuffer 产出等量数据」。
     * 这里做纯透传：不调用 replaceOutputBuffer 会让 outputBuffer 永远停在 EMPTY_BUFFER，
     * AudioProcessingPipeline.getOutput 的 while 循环既拿不到数据也等不到 isEnded，
     * 从而把 ExoPlayer:Playback 线程永久卡死在音频管线里（表现为缓冲冻结、连接超时）。
     */
    @Override
    public void queueInput(ByteBuffer input) {
        if (!input.hasRemaining()) return;
        ByteBuffer output = replaceOutputBuffer(input.remaining());
        output.put(input);
        output.flip();
    }

    @Override
    protected androidx.media3.common.audio.AudioProcessor.AudioFormat onConfigure(androidx.media3.common.audio.AudioProcessor.AudioFormat format) { return format; }

    public void resetParameters() { }

    public boolean setParameters(Object config, boolean softwareEqualizer) { return false; }
}
