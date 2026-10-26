// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.libass;

public class LibassConfiguration {

    public static class Builder {
        public Builder setFontConfig(String path) { return this; }
        public Builder setFontFile(Object file) { return this; }
        public Builder setFontsDirectory(String dir) { return this; }
        public Builder setMaximumBitmapCacheSizeMb(int mb) { return this; }
        public Builder setMaximumGlyphCount(int count) { return this; }
        public Builder setMaximumRenderPixels(long pixels) { return this; }
        public Builder setDefaultFontFamily(String family) { return this; }
        public LibassConfiguration build() { return new LibassConfiguration(); }
    }
}
