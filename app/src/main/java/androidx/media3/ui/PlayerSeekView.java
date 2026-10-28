// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 内部持有官方 DefaultTimeBar（TimeBar 接口的官方实现），进度显示与拖动全部走官方逻辑。
package androidx.media3.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;

import java.util.Locale;

public class PlayerSeekView extends FrameLayout {

    private final TimeBar timeBar;
    private final TextView positionView;
    private final TextView durationView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /** 洛视频：播放中 Player 不会为每一帧位置变化回调 onEvents，必须自己按时刷新。 */
    private final Runnable ticker = new Runnable() {

        @Override
        public void run() {
            sync();
        }
    };

    private final Player.Listener listener = new Player.Listener() {

        @Override
        public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
            sync();
        }
    };

    private Player player;
    private DurationSupplier durationSupplier;
    private OnSeekListener seekListener;
    private boolean scrubbing;

    /** 洛视频：MediaController.seekTo 是跨进程异步的，跳转完成前先按目标位置显示，避免进度条闪回旧位置。 */
    private long pendingPosition = C.TIME_UNSET;

    private final Runnable clearPending = new Runnable() {

        @Override
        public void run() {
            pendingPosition = C.TIME_UNSET;
            sync();
        }
    };

    public PlayerSeekView(Context context) {
        this(context, null);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        positionView = createText(context, "00:00");
        durationView = createText(context, "--:--");
        positionView.setId(androidx.media3.ui.R.id.exo_position);
        durationView.setId(androidx.media3.ui.R.id.exo_duration);
        timeBar = new DefaultTimeBar(context, attrs);
        timeBar.addListener(new TimeBar.OnScrubListener() {

            @Override
            public void onScrubStart(TimeBar bar, long positionMs) {
                scrubbing = true;
            }

            @Override
            public void onScrubMove(TimeBar bar, long positionMs) {
                // 洛视频：拖动时把当前时长换成拖动位置，松手后再交回给播放器。
                positionView.setText(format(positionMs));
            }

            @Override
            public void onScrubStop(TimeBar bar, long positionMs, boolean canceled) {
                scrubbing = false;
                if (!canceled) pendingPosition = positionMs;
                sync();
                handler.removeCallbacks(clearPending);
                handler.postDelayed(clearPending, 1500);
            }
        });
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        // 洛视频：内容贴底，把多出来的高度全部留给上方透明区当触控区（视觉位置不变，手指更好点）。
        params.gravity = Gravity.BOTTOM;
        addView(createContent(context), params);
        setMinimumHeight(dp(context, 44));
    }

    private View createContent(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView((View) timeBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 18)));
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(positionView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(durationView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return root;
    }

    private TextView createText(Context context, String text) {
        TextView view = new TextView(context);
        view.setTextColor(0xFFFFFFFF);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        view.setText(text);
        return view;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static String format(long timeMs) {
        if (timeMs == C.TIME_UNSET || timeMs < 0) return "--:--";
        long total = timeMs / 1000;
        long hour = total / 3600;
        long minute = total % 3600 / 60;
        long second = total % 60;
        if (hour > 0) return String.format(Locale.US, "%d:%02d:%02d", hour, minute, second);
        return String.format(Locale.US, "%02d:%02d", minute, second);
    }

    /**
     * 洛视频：上游这个方法是空实现，导致进度条永远停在 0、时长永远不显示。
     * 这里真正把 TimeBar 绑到 Player 上，并在播放期间按 500ms 刷新。
     */
    public void setPlayer(Player player) {
        if (this.player == player) return;
        if (this.player != null) this.player.removeListener(listener);
        this.player = player;
        if (this.player != null) this.player.addListener(listener);
        sync();
    }

    public interface DurationSupplier { long get(); }

    /**
     * 洛视频：DefaultTimeBar 自带的触摸判定只有 barBounds ± barHeight（约 30px），
     * 手指根本点不准，也无法稳定拖动。这里整块接管触摸，按宽度比例算位置。
     */
    public interface OnSeekListener {

        void onScrubbing(boolean scrubbing);

        void onSeek(long positionMs);
    }

    public void setOnSeekListener(OnSeekListener listener) {
        this.seekListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return getDurationMs() > 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        long duration = getDurationMs();
        if (duration <= 0) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                scrubbing = true;
                handler.removeCallbacks(ticker);
                handler.removeCallbacks(clearPending);
                if (seekListener != null) seekListener.onScrubbing(true);
                updateScrub(toPosition(event.getX(), duration));
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scrubbing) return false;
                updateScrub(toPosition(event.getX(), duration));
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!scrubbing) return false;
                boolean commit = event.getActionMasked() == MotionEvent.ACTION_UP;
                long position = toPosition(event.getX(), duration);
                updateScrub(position);
                scrubbing = false;
                // 洛视频：松手后先按目标位置显示，等播放器追上来再交回真实位置。
                pendingPosition = commit ? position : C.TIME_UNSET;
                if (seekListener != null) {
                    seekListener.onScrubbing(false);
                    if (commit) seekListener.onSeek(position);
                }
                sync();
                handler.removeCallbacks(clearPending);
                handler.postDelayed(clearPending, 1500);
                return true;
            default:
                return false;
        }
    }

    private void updateScrub(long position) {
        timeBar.setPosition(position);
        positionView.setText(format(position));
    }

    private long toPosition(float x, long duration) {
        float left = getPaddingStart();
        float width = getWidth() - getPaddingStart() - getPaddingEnd();
        if (width <= 0) return 0;
        float ratio = Math.max(0f, Math.min(1f, (x - left) / width));
        return (long) (ratio * duration);
    }

    private long getDurationMs() {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        if (duration == C.TIME_UNSET || duration <= 0) duration = durationSupplier != null ? durationSupplier.get() : C.TIME_UNSET;
        return duration;
    }

    /**
     * 洛视频：部分站点（尤其是解析出来的直链）Player.getDuration() 长时间返回 TIME_UNSET，
     * 由外部提供兜底时长，避免总时长一直显示 --:--。
     */
    public void setDurationSupplier(DurationSupplier supplier) {
        this.durationSupplier = supplier;
        sync();
    }

    public TimeBar getTimeBar() {
        return timeBar;
    }

    private void sync() {
        handler.removeCallbacks(ticker);
        // 洛视频：player == null（网页播放）时进度由 updateWebSeek 外部驱动，这里不能清零——
        // 否则每次拖动松手/clearPending 触发的 sync 都会把进度闪回 00:00。
        if (player == null) return;
        long duration = getDurationMs();
        if (!scrubbing) {
            long current = player.getCurrentPosition();
            if (pendingPosition != C.TIME_UNSET) {
                if (Math.abs(current - pendingPosition) <= 1500) pendingPosition = C.TIME_UNSET;
                else current = pendingPosition;
            }
            timeBar.setPosition(current);
            positionView.setText(format(current));
        }
        if (duration != C.TIME_UNSET && duration > 0) {
            timeBar.setDuration(duration);
            durationView.setText(format(duration));
        }
        long buffered = player.getBufferedPosition();
        if (buffered != C.TIME_UNSET) timeBar.setBufferedPosition(buffered);
        if (player.isPlaying() && !scrubbing) handler.postDelayed(ticker, 500);
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(ticker);
        super.onDetachedFromWindow();
    }
}
