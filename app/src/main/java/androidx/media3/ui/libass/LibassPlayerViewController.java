// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.ui.libass;

import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;

public class LibassPlayerViewController {

    public LibassPlayerViewController(Object player, Object session, Object controller) { }

    public interface SecondarySubtitleViewConfigurator { void apply(SubtitleView view); }
    public void setSecondarySubtitleViewConfigurator(SecondarySubtitleViewConfigurator configurator) { }
    public void setStyleOverride(CaptionStyleCompat style, Object fontFamily) { }
    public void close() { }
    public boolean bind(PlayerView view) { return false; }
}
