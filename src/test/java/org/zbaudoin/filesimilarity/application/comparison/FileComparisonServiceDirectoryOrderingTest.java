package org.zbaudoin.filesimilarity.application.comparison;

import org.junit.Test;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.AbstractComparisonStrategy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileComparisonServiceDirectoryOrderingTest {

    @Test
    public void keepsFilesFromEachConfiguredDirectoryInTheSameResultColumn() throws Exception {
        Path baseDirectory = Files.createTempDirectory("ressemblance-directory-order");
        Path firstDirectory = Files.createDirectory(baseDirectory.resolve("first"));
        Path secondDirectory = Files.createDirectory(baseDirectory.resolve("second"));
        Path firstFile1 = Files.createFile(firstDirectory.resolve("1.txt"));
        Path firstFile2 = Files.createFile(firstDirectory.resolve("2.txt"));
        Path secondFile1 = Files.createFile(secondDirectory.resolve("1.txt"));
        Path secondFile2 = Files.createFile(secondDirectory.resolve("2.txt"));

        AppConfig config = new AppConfig();
        config.setFiles(Arrays.asList(firstDirectory.toFile(), secondDirectory.toFile()));
        config.setComparators(Collections.singletonList(new IdenticalScoreComparator()));

        try {
            FileComparisonService service = new FileComparisonService(config);
            List<String> expected = Arrays.asList(
                    pair(firstFile1, firstFile2),
                    pair(firstFile1, secondFile1),
                    pair(firstFile1, secondFile2),
                    pair(firstFile2, secondFile1),
                    pair(firstFile2, secondFile2),
                    pair(secondFile1, secondFile2));

            assertEquals(expected, getPairs(service.run((operation, completed, total) -> {}, () -> false)));
            assertEquals(expected, getPairs(service.run((operation, completed, total) -> {}, () -> false)));
        } finally {
            Files.deleteIfExists(firstFile1);
            Files.deleteIfExists(firstFile2);
            Files.deleteIfExists(secondFile1);
            Files.deleteIfExists(secondFile2);
            Files.deleteIfExists(firstDirectory);
            Files.deleteIfExists(secondDirectory);
            Files.deleteIfExists(baseDirectory);
        }
    }

    private static String pair(Path file1, Path file2) {
        return file1.toAbsolutePath() + " -> " + file2.toAbsolutePath();
    }

    private static List<String> getPairs(List<FileComparison> comparisons) {
        List<String> pairs = new ArrayList<>();
        for (FileComparison comparison : comparisons) {
            pairs.add(comparison.getFile1().getAbsolutePath() + " -> "
                    + comparison.getFile2().getAbsolutePath());
        }
        return pairs;
    }

    private static class IdenticalScoreComparator extends AbstractComparisonStrategy {
        @Override
        public String getLibelle() {
            return "Identical score";
        }

        @Override
        public int getTotalProgressUnits(ComparisonBatch comparisons) {
            return comparisons.size();
        }

        @Override
        public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
            for (FileComparison comparison : comparisons) {
                setResult(comparison, 100);
                progress.plus();
            }
        }
    }
}
