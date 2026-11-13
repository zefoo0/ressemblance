package org.zbaudoin.ressemblance.service.comparator.video;

import java.awt.*;
import java.awt.image.BufferedImage;

class PerceptualHash {

    static final int SIZE = 32;
    private static final int HASH_SIZE = 8;
    private static final double[][] COSINE = createCosineTable();

    static long compute(BufferedImage image) {

        BufferedImage resized = image;
        if (image.getWidth() != SIZE || image.getHeight() != SIZE
                || image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            resized = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = resized.createGraphics();
            g.drawImage(image, 0, 0, SIZE, SIZE, null);
            g.dispose();
        }

        double[][] pixels = new double[SIZE][SIZE];

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixels[y][x] =
                    resized.getRaster().getSample(x, y, 0);
            }
        }

        double[][] horizontalDct = new double[HASH_SIZE][SIZE];

        for (int u = 0; u < HASH_SIZE; u++) {
            for (int y = 0; y < SIZE; y++) {
                double sum = 0;
                for (int x = 0; x < SIZE; x++) {
                    sum += pixels[x][y] * COSINE[u][x];
                }
                horizontalDct[u][y] = sum;
            }
        }

        double[][] dct = new double[HASH_SIZE][HASH_SIZE];
        for (int u = 0; u < HASH_SIZE; u++) {
            for (int v = 0; v < HASH_SIZE; v++) {
                double sum = 0;
                for (int y = 0; y < SIZE; y++) {
                    sum += horizontalDct[u][y] * COSINE[v][y];
                }
                dct[u][v] = sum;
            }
        }

        double[] values = new double[63];
        int index = 0;

        for (int u = 0; u < HASH_SIZE; u++) {
            for (int v = 0; v < HASH_SIZE; v++) {

                if (u == 0 && v == 0) continue;

                values[index++] = dct[u][v];
            }
        }

        double[] sorted = values.clone();
        java.util.Arrays.sort(sorted);

        double median = sorted[sorted.length / 2];

        long hash = 0;
        int bit = 0;

        for (int u = 0; u < HASH_SIZE; u++) {
            for (int v = 0; v < HASH_SIZE; v++) {

                if (u == 0 && v == 0) continue;

                if (dct[u][v] > median) {
                    hash |= (1L << bit);
                }

                bit++;
            }
        }

        return hash;
    }

    private static double[][] createCosineTable() {
        double[][] cosine = new double[HASH_SIZE][SIZE];
        for (int frequency = 0; frequency < HASH_SIZE; frequency++) {
            for (int position = 0; position < SIZE; position++) {
                cosine[frequency][position] = Math.cos(
                    (2 * position + 1) * frequency * Math.PI / (2 * SIZE)
                );
            }
        }
        return cosine;
    }

    static int distance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }
}