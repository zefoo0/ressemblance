package org.zbaudoin.ressemblance.service;

import org.junit.Test;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.ressemblance.service.comparator.AbstractComparator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RessemblanceExtensionFilteringTest {

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
        Param param = new Param();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Collections.singletonList(comparator));

        try {
            new Ressemblance(param).run((operation, completed, total) -> {}, () -> false);
            return comparator.comparisonCount;
        } finally {
            Files.deleteIfExists(firstText);
            Files.deleteIfExists(secondText);
            Files.deleteIfExists(noExtension);
            Files.deleteIfExists(directory);
        }
    }

    private static class RecordingComparator extends AbstractComparator {
        private int comparisonCount;

        @Override
        public String getLibelle() {
            return "Recording";
        }

        @Override
        public int getTotalProgressUnits(ComparisonLines comparisons) {
            return comparisons.size();
        }

        @Override
        public void compare(ComparisonLines comparisons, Progress progress) {
            comparisonCount = comparisons.size();
            progress.plus(comparisonCount);
        }
    }
}
