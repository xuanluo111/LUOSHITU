// @hand 洛视频补齐桩：上游私有 aar 将 CaptionStyleCompat 置于 androidx.media3.ui（官方 1.11.1 无此类）。手工维护。
package androidx.media3.ui;

import android.graphics.Typeface;
import android.view.accessibility.CaptioningManager;

public class CaptionStyleCompat {

    public static final int EDGE_TYPE_NONE = 0;
    public static final int EDGE_TYPE_DROP_SHADOW = 1;
    public static final int EDGE_TYPE_RAISED = 2;
    public static final int EDGE_TYPE_OUTLINE = 3;

    public static final float DEFAULT_EDGE_WIDTH = 1.0f;
    public static final float DEFAULT_SHADOW_OFFSET = 1f;
    public static final CaptionStyleCompat DEFAULT = new CaptionStyleCompat();

    public int foregroundColor = 0xFFFFFFFF;
    public int backgroundColor = 0x00000000;
    public int windowColor = 0x00000000;
    public int edgeType = EDGE_TYPE_NONE;
    public int edgeColor = 0xFFFFFFFF;
    public float edgeWidth = 1f;
    public float shadowOffset = 1f;
    public Typeface typeface = Typeface.DEFAULT;

    public CaptionStyleCompat() { }
    public CaptionStyleCompat(int foregroundColor, int backgroundColor, int windowColor, int edgeType, int edgeColor, Object typeface, float edgeWidth, float shadowOffset) {
        this.foregroundColor = foregroundColor; this.backgroundColor = backgroundColor; this.windowColor = windowColor;
        this.edgeType = edgeType; this.edgeColor = edgeColor; this.edgeWidth = edgeWidth; this.shadowOffset = shadowOffset;
    }

    public static CaptionStyleCompat createFromCaptionStyle(Object style) {
        return new CaptionStyleCompat(0xFFFFFFFF, 0x00000000, 0x00000000, 0, 0xFFFFFFFF, Typeface.DEFAULT, 0, 0);
    }
}
