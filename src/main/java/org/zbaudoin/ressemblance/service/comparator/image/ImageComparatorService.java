package org.zbaudoin.ressemblance.service.comparator.image;

import org.zbaudoin.ressemblance.service.comparator.PerceptualHash;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageComparatorService {

    private static final int HISTOGRAM_SIZE = 64;
    private static final int COLOR_BINS = 4;
    private static final int GRID_SIZE = 2;
    private static final double COLOR_WEIGHT = 0.6;
    private static final double SPATIAL_COLOR_WEIGHT = 0.5;
    private final Map<File, ImageFingerprint> fingerprints = new HashMap<>();

    public void clearFingerprints() {
        fingerprints.clear();
    }

    public int compare(File imageA, File imageB) throws IOException {
        ImageFingerprint fingerprintA = getFingerprint(imageA);
        ImageFingerprint fingerprintB = getFingerprint(imageB);

        int distance = PerceptualHash.distance(fingerprintA.perceptualHash,
                fingerprintB.perceptualHash);
        double shapeSimilarity =
                (double) (PerceptualHash.HASH_BITS - distance) / PerceptualHash.HASH_BITS;
        double globalColorSimilarity = histogramIntersection(fingerprintA.colorHistogram,
                fingerprintB.colorHistogram);
        double spatialColorSimilarity =
                spatialHistogramIntersection(fingerprintA.spatialColorHistograms,
                fingerprintB.spatialColorHistograms);
        double colorSimilarity = globalColorSimilarity * (1.0 - SPATIAL_COLOR_WEIGHT)
                + spatialColorSimilarity * SPATIAL_COLOR_WEIGHT;
        double similarity = shapeSimilarity * (1.0 - COLOR_WEIGHT) + colorSimilarity * COLOR_WEIGHT;
        return (int) Math.round(100.0 * similarity);
    }

    private ImageFingerprint getFingerprint(File imageFile) throws IOException {
        File key = imageFile.getCanonicalFile();
        ImageFingerprint fingerprint = fingerprints.get(key);
        if (fingerprint==null) {
            BufferedImage image = ImageIO.read(key);
            if (image==null) {
                throw new IOException("Format d'image non pris en charge : " + imageFile);
            }
            fingerprint = createFingerprint(image);
            fingerprints.put(key, fingerprint);
        }
        return fingerprint;
    }

    private static ImageFingerprint createFingerprint(BufferedImage image) {
        int[] colorHistogram = new int[COLOR_BINS * COLOR_BINS * COLOR_BINS];
        int[][] spatialColorHistograms = new int[GRID_SIZE * GRID_SIZE][colorHistogram.length];
        BufferedImage thumbnail = new BufferedImage(HISTOGRAM_SIZE, HISTOGRAM_SIZE,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();
        graphics.drawImage(image, 0, 0, HISTOGRAM_SIZE, HISTOGRAM_SIZE, null);
        graphics.dispose();

        for (int y = 0; y < HISTOGRAM_SIZE; y++) {
            for (int x = 0; x < HISTOGRAM_SIZE; x++) {
                int rgb = thumbnail.getRGB(x, y);
                int redBin = ((rgb >> 16) & 0xff) * COLOR_BINS / 256;
                int greenBin = ((rgb >> 8) & 0xff) * COLOR_BINS / 256;
                int blueBin = (rgb & 0xff) * COLOR_BINS / 256;
                int bin = redBin * COLOR_BINS * COLOR_BINS + greenBin * COLOR_BINS + blueBin;
                colorHistogram[bin]++;
                int gridX = x * GRID_SIZE / HISTOGRAM_SIZE;
                int gridY = y * GRID_SIZE / HISTOGRAM_SIZE;
                spatialColorHistograms[gridY * GRID_SIZE + gridX][bin]++;
            }
        }

        return new ImageFingerprint(PerceptualHash.compute(image), colorHistogram,
                spatialColorHistograms);
    }

    private static double histogramIntersection(int[] histogramA, int[] histogramB) {
        int matchingPixels = 0;
        int totalPixels = 0;
        for (int i = 0; i < histogramA.length; i++) {
            matchingPixels += Math.min(histogramA[i], histogramB[i]);
            totalPixels += histogramA[i];
        }
        return (double) matchingPixels / totalPixels;
    }

    private static double spatialHistogramIntersection(int[][] histogramsA, int[][] histogramsB) {
        double matchingFraction = 0;
        for (int i = 0; i < histogramsA.length; i++) {
            matchingFraction += histogramIntersection(histogramsA[i], histogramsB[i]);
        }
        return matchingFraction / histogramsA.length;
    }

    private static class ImageFingerprint {
        private final long perceptualHash;
        private final int[] colorHistogram;
        private final int[][] spatialColorHistograms;

        private ImageFingerprint(long perceptualHash, int[] colorHistogram,
                                 int[][] spatialColorHistograms) {
            this.perceptualHash = perceptualHash;
            this.colorHistogram = colorHistogram;
            this.spatialColorHistograms = spatialColorHistograms;
        }
    }
}
