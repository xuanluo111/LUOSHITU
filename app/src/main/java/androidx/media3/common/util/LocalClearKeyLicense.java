// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
// 本地 ClearKey 许可解析：本产品不启用 DRM 路径，恒返回空。
package androidx.media3.common.util;

public class LocalClearKeyLicense {

    public static byte[][] parse(androidx.media3.common.MediaItem.DrmConfiguration config) { return new byte[0][]; }
    public static String parse(String key) { return ""; }
}
