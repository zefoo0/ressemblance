package org.zbaudoin.filesimilarity.infrastructure.comparison.strategy;

import org.zbaudoin.filesimilarity.application.comparison.FileComparisonService;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileContentComparisonStrategyTest {

    @Test
    public void detectsIdenticalFileContents() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-file-comparator");
        Path firstFile = directory.resolve("first.bin");
        Path secondFile = directory.resolve("second.bin");
        byte[] content = new byte[] {0, 1, 2, 3, 4};
        Files.write(firstFile, content);
        Files.write(secondFile, content);

        try {
            FileComparison comparison = compareFilesIn(directory).get(0);

            assertEquals(100, comparison.getResult().getSimilarite());
        } finally {
            Files.deleteIfExists(firstFile);
            Files.deleteIfExists(secondFile);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void rejectsDifferentContentsWithTheSameLength() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-file-comparator");
        Path firstFile = directory.resolve("first.bin");
        Path secondFile = directory.resolve("second.bin");
        Files.write(firstFile, new byte[] {0, 1, 2, 3, 4});
        Files.write(secondFile, new byte[] {0, 1, 2, 3, 5});

        try {
            FileComparison comparison = compareFilesIn(directory).get(0);

            assertEquals(0, comparison.getResult().getSimilarite());
        } finally {
            Files.deleteIfExists(firstFile);
            Files.deleteIfExists(secondFile);
            Files.deleteIfExists(directory);
        }
    }

    private static List<FileComparison> compareFilesIn(Path directory) {
        AppConfig param = new AppConfig();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Collections.singletonList(new FileContentComparisonStrategy()));
        return new FileComparisonService(param).run((operation, completed, total) -> {}, () -> false);
    }
}
