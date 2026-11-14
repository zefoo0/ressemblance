package org.zbaudoin.ressemblance.service.comparator.video;

import org.zbaudoin.ressemblance.service.comparator.PerceptualHash;

import java.util.*;

class VideoSimilarityService {

    private static final int MAX_DISTANCE = 10;

    ComparisonResult compare(
            List<Long> hashesA,
            List<Long> hashesB) {

        if (hashesA.isEmpty() || hashesB.isEmpty()) {
            throw new IllegalArgumentException(
                    "Les vidéos doivent contenir des images."
            );
        }

        // Recherche du décalage temporel dominant
        int[] offsets = new int[hashesA.size() + hashesB.size() - 1];

        for (int i = 0; i < hashesA.size(); i++) {
            for (int j = 0; j < hashesB.size(); j++) {

                int distance = PerceptualHash.distance(
                        hashesA.get(i), hashesB.get(j)
                );

                if (distance <= MAX_DISTANCE) {
                    offsets[j - i + hashesA.size() - 1]++;
                }
            }
        }

        int bestOffset = 0;
        int bestOffsetMatches = 0;
        for (int i = 0; i < offsets.length; i++) {
            if (offsets[i] > bestOffsetMatches) {
                bestOffsetMatches = offsets[i];
                bestOffset = i - hashesA.size() + 1;
            }
        }

        // Comparaison des images alignées
        int matches = 0;
        int compared = 0;

        int startA = Math.max(0, -bestOffset);
        int endA = Math.min(
                hashesA.size(),
                hashesB.size() - bestOffset
        );

        for (int i = startA; i < endA; i++) {

            int j = i + bestOffset;

            int distance = PerceptualHash.distance(
                    hashesA.get(i),
                    hashesB.get(j)
            );

            compared++;

            if (distance <= MAX_DISTANCE) {
                matches++;
            }
        }

        double similarity = compared==0
                ? 0
                :100.0 * matches / compared;

        return new ComparisonResult(
                similarity,
                compared,
                matches,
                bestOffset
        );
    }
}