// @hand 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。手工维护。
package androidx.media3.exoplayer.text;

public class SubtitleTranscriptParserFactory implements androidx.media3.extractor.text.SubtitleParser.Factory {

    public SubtitleTranscriptParserFactory(SubtitleTranscript transcript) { }

    @Override
    public androidx.media3.extractor.text.SubtitleParser create(androidx.media3.common.Format format) { return null; }
    @Override
    public boolean supportsFormat(androidx.media3.common.Format format) { return false; }
    @Override
    public int getCueReplacementBehavior(androidx.media3.common.Format format) { return 0; }
}
