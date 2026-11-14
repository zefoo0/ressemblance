package org.zbaudoin.ressemblance.model;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComparisonLinesTest {

    @Test
    public void removesComparisonsContainingTheDeletedFile() {
        File deletedFile = new File("/files/deleted.txt");
        ComparisonLine matchingFirstFile = new ComparisonLine(deletedFile, new File("/files/first.txt"));
        ComparisonLine matchingSecondFile = new ComparisonLine(new File("/files/second.txt"), deletedFile);
        ComparisonLine remaining = new ComparisonLine(new File("/files/third.txt"), new File("/files/fourth.txt"));
        ComparisonLines lines = new ComparisonLines(Arrays.asList(
                matchingFirstFile, matchingSecondFile, remaining));

        assertTrue(lines.removeComparisonsContaining(new File("/files/deleted.txt")));

        assertEquals(Arrays.asList(remaining), lines);
        assertFalse(lines.removeComparisonsContaining(deletedFile));
    }
}
