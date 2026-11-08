package org.zbaudoin.ressemblance.video;

import java.util.*;
import java.util.stream.Collectors;

class VideoSimilarityService {

    private static final int MAX_DISTANCE = 10;

    ComparisonResult compare(
            List<VideoFrameExtractor.VideoFrame> framesA,
            List<VideoFrameExtractor.VideoFrame> framesB) {

        if (framesA.isEmpty() || framesB.isEmpty()) {
            throw new IllegalArgumentException(
                "Les vidéos doivent contenir des images."
            );
        }

        List<Long> hashesA = framesA.stream()
            .map(f -> PerceptualHash.compute(f.image))
            .collect(Collectors.toList());

        List<Long> hashesB = framesB.stream()
            .map(f -> PerceptualHash.compute(f.image))
                .collect(Collectors.toList());

        // Recherche du décalage temporel dominant
        Map<Integer, Integer> offsets = new HashMap<>();

        for (int i = 0; i < hashesA.size(); i++) {
            for (int j = 0; j < hashesB.size(); j++) {

                int distance = PerceptualHash.distance(
                    hashesA.get(i), hashesB.get(j)
                );

                if (distance <= MAX_DISTANCE) {
                    int offset = j - i;
                    offsets.merge(offset, 1, Integer::sum);
                }
            }
        }

        int bestOffset = offsets.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(0);

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

        double similarity = compared == 0
            ? 0
            : 100.0 * matches / compared;

        return new ComparisonResult(
            similarity,
            compared,
            matches,
            bestOffset
        );
    }
}