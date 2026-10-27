// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.mpvplayer;

public class MpvPlayerConfig {

    public static class Builder {
        public Builder setDataSourceFactory(Object factory) { return this; }
        public Builder addPreInitStringOption(String key, String value) { return this; }
        public Builder addTlsCaFileFromAsset(Object app, String asset, Object target) { return this; }
        public Builder addAndroidSubtitleOptions(Object app, androidx.media3.mpvplayer.MpvSubtitleOptions options) { return this; }
        public Builder addDiskCacheOptions(Object cacheDir, int seconds) { return this; }
        public Builder setDefaultFontDirectories(Object... dirs) { return this; }
        public Builder addConfigDirectory(Object dir) { return this; }
        public Builder addAndroidDefaults(Object cacheDir) { return this; }
        public MpvPlayerConfig build() { return new MpvPlayerConfig(); }
    }
}
