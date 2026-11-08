package org.zbaudoin.ressemblance.video;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class VideoFrameExtractor {

    List<VideoFrame> extract(File file)
            throws Exception {

        List<VideoFrame> frames = new ArrayList<>();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {

            grabber.start();

            long duration = grabber.getLengthInTime();

            // Une image toutes les secondes
            for (long time = 0; time < duration; time += 1_000_000) {
                grabber.setTimestamp(time);
                var frame = grabber.grabImage();
                if (frame==null) {
                    continue;
                }

                BufferedImage image = converter.convert(frame);

                if (image!=null) {
                    frames.add(new VideoFrame(
                            time / 1_000_000.0,
                            image
                    ));
                }
            }

            grabber.stop();
        }

        return frames;
    }

    static class VideoFrame {
        double timestamp;
        BufferedImage image;

        private VideoFrame(double timestamp, BufferedImage image) {
            this.timestamp = timestamp;
            this.image = image;
        }
    }
}