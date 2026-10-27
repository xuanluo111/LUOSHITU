// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 弹幕在本产品不启用（F8）；仅承载编译期签名。
package androidx.media3.ui.danmaku;

import android.net.Uri;

import androidx.media3.ui.PlayerView;

import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;

public class DanmakuPlayerViewController {

    public boolean bind(PlayerView view) { return false; }
    public void setOkHttpClient(OkHttpClient client) { }
    public void setEnabled(boolean enabled) { }
    public void setConfig(DanmakuConfig config) { }
    public void setDataSource(Uri uri) { }
    public void sendNow(String text) { }
    public void close() { }
    public void post(Danmaku danmaku) { }
    public List<Danmaku> getItems() { return Collections.emptyList(); }
}
