// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 内部持有官方 DefaultTimeBar（TimeBar 接口的官方实现），进度显示与拖动全部走官方逻辑。
package androidx.media3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;

public class PlayerSeekView extends FrameLayout {

    private final TimeBar timeBar;

    public PlayerSeekView(Context context) {
        super(context);
        timeBar = createBar(context, null);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        timeBar = createBar(context, attrs);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        timeBar = createBar(context, attrs);
    }

    private TimeBar createBar(Context context, @Nullable AttributeSet attrs) {
        TimeBar bar = new DefaultTimeBar(context, attrs);
        addView((View) bar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return bar;
    }

    public void setPlayer(Player player) { }

    public interface DurationSupplier { long get(); }

    public void setDurationSupplier(DurationSupplier supplier) { }

    public TimeBar getTimeBar() { return timeBar; }
}
