// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 磁盘预加载在本产品跟随设置开关；桩实现为无操作容器。
package androidx.media3.exoplayer.source.preload;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.source.RenderersFactory;

@UnstableApi
public class DiskPreloadManager {

    public DiskPreloadManager(Object cache, DataSource.Factory upstream, RenderersFactory renderersFactory) { }

    public static class Builder {
        public Builder(Object cache, DataSource.Factory upstream, RenderersFactory renderersFactory) { }
        public Builder setPriorityTaskManager(Object manager) { return this; }
        public DiskPreloadManager build() { return new DiskPreloadManager(null, null, null); }
    }

    public static class Options {
        public static Options builder() { return new Options(); }
        public Options setDurationMs(long ms) { return this; }
        public Options setMaxThreads(int threads) { return this; }
        public Options build() { return this; }
    }

    public boolean preload(Object mediaItem) { return false; }
    public void start(ExoPlayer p0, MediaItem p1, Options p2) { }
    public void stop() { }
    public void release() { }
    public void cancel() { }
}
