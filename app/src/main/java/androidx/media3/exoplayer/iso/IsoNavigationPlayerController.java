// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// ISO 光盘导航在本产品不启用：prepareMediaSource 直通。
package androidx.media3.exoplayer.iso;

import androidx.media3.common.DiscMenuController;
import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;

import com.fongmi.android.tv.App;

public class IsoNavigationPlayerController extends DiscMenuController {

    public IsoNavigationPlayerController(ExoPlayer player) { }

    public ExoPlayer getPlayer() { return null; }
    public MediaSource prepareMediaSource(App app, MediaSource source, DataSource.Factory factory) { return source; }
    public boolean prepareMediaSource(Object p0) { return false; }
}
