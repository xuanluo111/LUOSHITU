// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 光盘菜单在本产品停用：状态恒为 EMPTY。
package androidx.media3.common;

public class DiscMenuState {

    public static final int AVAILABILITY_AVAILABLE = 1;
    public static final int AVAILABILITY_UNAVAILABLE = 2;
    public static final int PHASE_IDLE = 0;
    public static final int PHASE_OPENING = 1;
    public static final int PHASE_ACTIVE = 2;

    public static final DiscMenuState EMPTY = new DiscMenuState();

    public int availability = AVAILABILITY_UNAVAILABLE;
    public int phase = PHASE_IDLE;
    public boolean navigationPlaybackOngoing;

	public static Object availability;

	public static Object phase;

}
