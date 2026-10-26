// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common;

public class DecoderMode {

    public static final DecoderMode AUTO = new DecoderMode("AUTO");
    public static final DecoderMode HARDWARE = new DecoderMode("HARDWARE");
    public static final DecoderMode SOFTWARE = new DecoderMode("SOFTWARE");
    public static final DecoderMode FFMPEG = new DecoderMode("FFMPEG");

    private final String name;

    private DecoderMode(String name) { this.name = name; }

    public String name() { return name; }

    public static DecoderMode[] values() { return new DecoderMode[]{AUTO, HARDWARE, SOFTWARE, FFMPEG}; }
}
