// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工修正后维护。
package androidx.media3.ui.danmaku;

import java.util.List;
import androidx.media3.ui.PlayerView;

import okhttp3.OkHttpClient;

public class DanmakuPlayerViewController {
	public List<Danmaku> getItems() { return java.util.Collections.emptyList(); }
	public boolean bind(PlayerView p0) { return false; }
	public void setOkHttpClient(OkHttpClient p0) { }
	public void setEnabled(boolean p0) { }
	public void setConfig(DanmakuConfig p0) { }
	public void setDataSource(Object p0) { }
	public void sendNow(String p0) { }
	public void close() { }
}
