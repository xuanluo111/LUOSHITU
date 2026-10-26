// @hand 洛视频兼容层：官方 media3 PlayerView 上不存在上游私有 aar 的扩展方法（光盘菜单/调试视图/视频变换），
// 经此类提供无操作实现。见 docs/S0-立项报告.md 第 9 节。
package androidx.media3.ui;

import android.view.MotionEvent;

import androidx.media3.common.DiscMenuController;
import androidx.media3.common.VideoTransform;

public final class PlayerViewFork {

    private PlayerViewFork() { }

    public void setDiscMenuController(PlayerView p0, DiscMenuController p1) { }
    public Object dispatchDiscMenuTouchEvent(PlayerView p0, MotionEvent p1) { return false; }
    public boolean isDebugViewVisible(PlayerView p0) { return false; }
    public Object toggleDebugView(PlayerView p0) { }
}
