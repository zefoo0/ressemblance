package org.zbaudoin.ressemblance.service.comparator;

import org.junit.Test;
import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.ressemblance.service.Ressemblance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileComparatorTest {

    @Test
    public void detectsIdenticalFileContents() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-file-comparator");
        Path firstFile = directory.resolve("first.bin");
        Path secondFile = directory.resolve("second.bin");
        byte[] content = new byte[] {0, 1, 2, 3, 4};
        Files.write(firstFile, content);
        Files.write(secondFile, content);

        try {
            ComparisonLine comparison = compareFilesIn(directory).get(0);

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
            ComparisonLine comparison = compareFilesIn(directory).get(0);

            assertEquals(0, comparison.getResult().getSimilarite());
        } finally {
            Files.deleteIfExists(firstFile);
            Files.deleteIfExists(secondFile);
            Files.deleteIfExists(directory);
        }
    }

    private static List<ComparisonLine> compareFilesIn(Path directory) {
        Param param = new Param();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Collections.singletonList(new FileComparator()));
        return new Ressemblance(param).run((operation, completed, total) -> {}, () -> false);
    }
}
