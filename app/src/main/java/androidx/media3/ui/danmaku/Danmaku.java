// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.ui.danmaku;

public class Danmaku {

    public static final int TYPE_SCROLL = 0;
    public static final int TYPE_TOP = 1;
    public static final int TYPE_BOTTOM = 2;
    public static final int TYPE_REVERSE = 3;

    public long timeMs;
    public String text;
    public String image;
}
