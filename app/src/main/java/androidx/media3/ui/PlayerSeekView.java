// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.media3.common.Player;

public class PlayerSeekView extends View {

    public PlayerSeekView(Context context) { super(context); }

    public PlayerSeekView(Context context, AttributeSet attrs) { super(context, attrs); }

    public PlayerSeekView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public interface DurationSupplier { long get(); }

    public void setPlayer(Player player) { }
    public void setDurationSupplier(DurationSupplier supplier) { }
    public TimeBar getTimeBar() { return null; }
}
