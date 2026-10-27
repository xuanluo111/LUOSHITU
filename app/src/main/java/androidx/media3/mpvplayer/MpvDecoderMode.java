// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.mpvplayer;
import androidx.media3.common.DecoderMode;

public class MpvDecoderMode {
    public static final MpvDecoderMode AUTO = new MpvDecoderMode();
    public static final MpvDecoderMode HARDWARE = new MpvDecoderMode();
    public static final MpvDecoderMode SOFTWARE = new MpvDecoderMode();
    public static final MpvDecoderMode FFMPEG = new MpvDecoderMode();

    public androidx.media3.common.DecoderMode toCommonDecoderMode() { return androidx.media3.common.DecoderMode.AUTO; }
    public static androidx.media3.exoplayer.DecoderMode fromCommonDecoderMode(DecoderMode p0) { return androidx.media3.exoplayer.DecoderMode.AUTO; }
}
