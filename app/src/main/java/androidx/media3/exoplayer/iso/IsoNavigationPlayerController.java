// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// ISO 光盘导航在本产品不启用：prepareMediaSource 直通；持有传入 player，getPlayer 返回同一实例。
package androidx.media3.exoplayer.iso;

import androidx.media3.common.DiscMenuController;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;

import com.fongmi.android.tv.App;

public class IsoNavigationPlayerController extends DiscMenuController {

    private final ExoPlayer player;

    public IsoNavigationPlayerController(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() { return player; }

    public void addListener(Player.Listener listener) {
        if (player != null) player.addListener(listener);
    }

    public MediaSource prepareMediaSource(App app, MediaSource source, DataSource.Factory factory) { return source; }
    public boolean prepareMediaSource(Object p0) { return false; }
}
