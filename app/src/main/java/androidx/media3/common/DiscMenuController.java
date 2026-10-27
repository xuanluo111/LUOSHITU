// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// BD-J/ISO 光盘菜单能力在本产品不启用：所有探询返回不可用，open() 直接回调 UNAVAILABLE。
package androidx.media3.common;

import java.util.function.Consumer;

public class DiscMenuController {

    public enum OpenResult { RUNTIME_MISSING, UNAVAILABLE, TIMED_OUT }

    public boolean isNavigationPlayback() { return false; }
    public boolean isActive() { return false; }
    public boolean supportsPointer() { return false; }
    public boolean sendAction(String action) { return false; }
    public boolean open(String action, Consumer<OpenResult> callback) {
        if (callback != null) callback.accept(OpenResult.UNAVAILABLE);
        return false;
    }
    public void close() { }
    public boolean isStarted() { return false; }
}
