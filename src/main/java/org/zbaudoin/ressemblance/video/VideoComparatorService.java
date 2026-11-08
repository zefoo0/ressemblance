package org.zbaudoin.ressemblance.video;

import java.io.File;

public class VideoComparatorService {

    public int compare(File videoA, File videoB) throws Exception {

        VideoFrameExtractor extractor = new VideoFrameExtractor();
        var framesA = extractor.extract(videoA);
        var framesB = extractor.extract(videoB);

        VideoSimilarityService service = new VideoSimilarityService();

        ComparisonResult result = service.compare(framesA, framesB);

        return (int)result.similarity;
    }
}