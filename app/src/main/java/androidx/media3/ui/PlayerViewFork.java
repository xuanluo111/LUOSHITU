// 洛视频兼容层：官方 media3 PlayerView 上不存在上游私有 aar 的扩展方法（光盘菜单/调试视图），
// 经此 helper 提供无操作实现。见 docs/S0-立项报告.md 第 9 节；洛视频中这两个能力停用。
package androidx.media3.ui;

import android.view.MotionEvent;

import androidx.media3.common.DiscMenuController;

public final class PlayerViewFork {

    private PlayerViewFork() { }

    public static void setDiscMenuController(PlayerView view, DiscMenuController controller) { }

    public static boolean dispatchDiscMenuTouchEvent(PlayerView view, MotionEvent event) { return false; }

    public static boolean isDebugViewVisible(PlayerView view) { return false; }

    public static void toggleDebugView(PlayerView view) { }
}
