// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// fork 的"二次字幕选择增强"运行时停用（S1 决策）：本类仅委托主 TrackSelector 工厂创建官方选择器。
package androidx.media3.exoplayer.trackselection;

import android.content.Context;

public class SecondaryTextTrackSelector {

    public static class Factory implements androidx.media3.exoplayer.trackselection.TrackSelector.Factory {

        private final androidx.media3.exoplayer.trackselection.TrackSelector.Factory delegate;

        public Factory(androidx.media3.exoplayer.trackselection.TrackSelector.Factory delegate) {
            this.delegate = delegate;
        }

        @Override
        public androidx.media3.exoplayer.trackselection.TrackSelector createTrackSelector(Context context) {
            return delegate.createTrackSelector(context);
        }
    }
}
