// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.common.audio;

public class AudioChannelMode {

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





}
