package org.zbaudoin.ressemblance.service.comparator.video;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.concurrent.CancellationException;

class VideoFrameExtractor {

    private static final long SAMPLE_INTERVAL_MICROS = 1_000_000;

    List<Long> extract(File file) throws Exception {
        return extract(file, () -> false);
    }

    List<Long> extract(File file, BooleanSupplier cancellationRequested) throws Exception {

        List<Long> hashes = new ArrayList<>();

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {

            grabber.start();

            long nextSampleTime = 0;
            Frame frame;
            while (true) {
                checkCancellation(cancellationRequested);
                frame = grabber.grabImage();
                if (frame == null) {
                    break;
                }
                long timestamp = grabber.getTimestamp();
                if (timestamp < nextSampleTime) {
                    continue;
                }

                BufferedImage image = converter.convert(frame);
                if (image != null) {
                    hashes.add(PerceptualHash.compute(image));
                    do {
                        nextSampleTime += SAMPLE_INTERVAL_MICROS;
                    } while (nextSampleTime <= timestamp);
                }
            }
        }

        return hashes;
    }

    private static void checkCancellation(BooleanSupplier cancellationRequested) {
        if (Thread.currentThread().isInterrupted() || cancellationRequested.getAsBoolean()) {
            throw new CancellationException("Video fingerprinting was cancelled");
        }
    }
}