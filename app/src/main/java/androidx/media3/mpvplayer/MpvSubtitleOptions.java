// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。
package androidx.media3.mpvplayer;

public class MpvSubtitleOptions {
    public MpvSubtitleOptions() { }
	public static class Builder {
		public Builder setFontsDirectory(String dir) { return this; }
		public Builder setPosition(double v) { return this; }
		public Builder setVerticalOffset(float v) { return this; }
		public Builder setScale(float v) { return this; }
		public Builder setBitmapSubtitleBrightness(float v) { return this; }
		public Builder setSecondarySubtitlePosition(double v) { return this; }
		public Builder setSecondaryVerticalOffset(double v) { return this; }
		public Builder setSecondaryAssStyleOverride(boolean b) { return this; }
		public Builder setFontFamily(String family) { return this; }
		public Builder setCustomStyle(int fg, int bg, int edgeType, int edgeColor, float edgeWidth, float shadow) { return this; }
		public Builder setSystemCaptionStyle() { return this; }
		public Builder setDefaultStyle(Object fg, Object bg, Object edgeType, Object edgeColor, Object edgeWidth, Object shadow) { return this; }
		public MpvSubtitleOptions build() { return new MpvSubtitleOptions(); }
	}
}
