// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.trackselection;

public class SecondaryTextTrackSelector {

    public static class Factory implements androidx.media3.exoplayer.trackselection.TrackSelector.Factory {
        public Factory(Object delegateFactory) { }
        @Override
        public androidx.media3.exoplayer.trackselection.TrackSelector createTrackSelector(android.content.Context context) { return null; }
    }
}
