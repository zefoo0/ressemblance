package org.zbaudoin.filesimilarity.application.comparison;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.AbstractComparisonStrategy;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FileComparisonServiceExtensionFilteringTest {

    @Test
    public void nullOrEmptyExtensionsMatchNoFiles() throws Exception {
        assertEquals(0, countComparisons(null));
        assertEquals(0, countComparisons(Collections.emptySet()));
        assertEquals(0, countComparisons(Collections.singleton(" ")));
    }

    @Test
    public void wildcardMatchesFilesWithAndWithoutExtensions() throws Exception {
        assertEquals(3, countComparisons(Collections.singleton("*")));
    }

    @Test
    public void selectedExtensionsAreCaseInsensitive() throws Exception {
        assertEquals(1, countComparisons(Collections.singleton("txt")));
    }

    private static int countComparisons(Set<String> extensions) throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-extensions");
        Path firstText = Files.createFile(directory.resolve("first.txt"));
        Path secondText = Files.createFile(directory.resolve("second.TXT"));
        Path noExtension = Files.createFile(directory.resolve("third"));
        RecordingComparator comparator = new RecordingComparator();
        comparator.setExtensions(extensions);
        AppConfig param = new AppConfig();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Collections.singletonList(comparator));

        try {
            new FileComparisonService(param).run((operation, completed, total) -> {}, () -> false);
            return comparator.comparisonCount;
        } finally {
            Files.deleteIfExists(firstText);
            Files.deleteIfExists(secondText);
            Files.deleteIfExists(noExtension);
            Files.deleteIfExists(directory);
        }
    }

    private static class RecordingComparator extends AbstractComparisonStrategy {
        private int comparisonCount;

        @Override
        public String getLibelle() {
            return "Recording";
        }

        @Override
        public int getTotalProgressUnits(ComparisonBatch comparisons) {
            return comparisons.size();
        }

        @Override
        public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
            comparisonCount = comparisons.size();
            progress.plus(comparisonCount);
        }
    }
}
