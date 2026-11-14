package org.zbaudoin.filesimilarity.domain.comparison;


import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComparisonBatchTest {

    @Test
    public void collectsUniqueFilesFromBothSidesOfEveryComparison() {
        File first = new File("/files/first.txt");
        File second = new File("/files/second.txt");
        File third = new File("/files/third.txt");
        ComparisonBatch lines = new ComparisonBatch(Arrays.asList(
                new FileComparison(first, second),
                new FileComparison(second, third)));

        assertEquals(new HashSet<>(Arrays.asList(first, second, third)), lines.getAllFiles());
        assertEquals(Collections.emptySet(), new ComparisonBatch(Collections.emptyList()).getAllFiles());
    }

    @Test
    public void removesComparisonsContainingTheDeletedFile() {
        File deletedFile = new File("/files/deleted.txt");
        FileComparison matchingFirstFile = new FileComparison(deletedFile, new File("/files/first.txt"));
        FileComparison matchingSecondFile = new FileComparison(new File("/files/second.txt"), deletedFile);
        FileComparison remaining = new FileComparison(new File("/files/third.txt"), new File("/files/fourth.txt"));
        ComparisonBatch lines = new ComparisonBatch(Arrays.asList(
                matchingFirstFile, matchingSecondFile, remaining));

        assertTrue(lines.removeComparisonsContaining(new File("/files/deleted.txt")));

        assertEquals(List.of(remaining), lines);
        assertFalse(lines.removeComparisonsContaining(deletedFile));
    }
}
