// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common.audio;

public class AudioChannelMix {

    public static float[][] createFrontCenterGainMix(int channelCount, Object factor) { return new float[channelCount][channelCount]; }
    public static float[][] createStereoMix(int channelCount, boolean reverse) { return new float[channelCount][channelCount]; }
    public static float[][] createMonoMix(int channelCount) { return new float[channelCount][channelCount]; }
    public static float[][] createFrontBalanceMix(int channelCount, float balance) { return new float[channelCount][channelCount]; }
    public static float[][] compose(float[][] a, float[][] b) { return a; }
}
