// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.common;

public class DolbyVisionOutputPolicy {

    public static final int AUTO = 0;
    public static final int ASSUME_UNSUPPORTED = 1;

    public @interface Mode { }
}
