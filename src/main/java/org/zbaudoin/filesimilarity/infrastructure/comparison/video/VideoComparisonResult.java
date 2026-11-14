package org.zbaudoin.filesimilarity.infrastructure.comparison.video;


class VideoComparisonResult {

    final double similarity;
    final int comparedFrames;
    final int matchingFrames;
    final int offsetSeconds;

    VideoComparisonResult(double similarity, int comparedFrames, int matchingFrames, int offsetSeconds) {
        this.similarity = similarity;
        this.comparedFrames = comparedFrames;
        this.matchingFrames = matchingFrames;
        this.offsetSeconds = offsetSeconds;
    }
}
