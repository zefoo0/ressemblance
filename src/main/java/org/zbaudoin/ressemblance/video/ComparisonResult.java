package org.zbaudoin.ressemblance.video;

class ComparisonResult {

    double similarity;
    int comparedFrames;
    int matchingFrames;
    int offsetSeconds;

    ComparisonResult(double similarity, int comparedFrames, int matchingFrames, int offsetSeconds) {
        this.similarity = similarity;
        this.comparedFrames = comparedFrames;
        this.matchingFrames = matchingFrames;
        this.offsetSeconds = offsetSeconds;
    }
}
