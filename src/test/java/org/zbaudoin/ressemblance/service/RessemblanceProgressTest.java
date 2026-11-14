package org.zbaudoin.ressemblance.service;

import org.junit.Test;
import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.ressemblance.service.comparator.AbstractComparator;
import org.zbaudoin.ressemblance.service.Progress;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RessemblanceProgressTest {

    @Test
    public void reportsWeightedProgressWhileProcessingOneComparatorAtATime() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-progress");
        Path firstText = Files.createFile(directory.resolve("first.txt"));
        Path secondText = Files.createFile(directory.resolve("second.txt"));
        Path binary = Files.createFile(directory.resolve("file.bin"));
        List<String> executionOrder = new ArrayList<>();
        RecordingComparator allFiles = new RecordingComparator("*", 1, "all", executionOrder);
        RecordingComparator textFiles = new RecordingComparator("txt", 4, "text", executionOrder);
        Param param = new Param();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Arrays.asList(allFiles, textFiles));

        List<Long> completedProgress = new ArrayList<>();
        List<Long> totalProgress = new ArrayList<>();
        List<String> currentOperations = new ArrayList<>();
        try {
            List<ComparisonLine> lines = new Ressemblance(param).run((operation, completed, total) -> {
                if (!operation.isEmpty()) {
                    currentOperations.add(operation);
                }
                completedProgress.add(completed);
                totalProgress.add(total);
            }, () -> false);

            assertEquals(3, lines.size());
            assertEquals(Arrays.asList("all", "all", "all", "text"), executionOrder);
            assertEquals(Arrays.asList("all", "all", "all", "all", "text", "text"), currentOperations);
            assertEquals(Arrays.asList(0L, 0L, 1L, 2L, 3L, 3L, 7L), completedProgress);
            for (Long total : totalProgress) {
                assertEquals(7L, total.longValue());
            }
        } finally {
            Files.deleteIfExists(firstText);
            Files.deleteIfExists(secondText);
            Files.deleteIfExists(binary);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void reportsCurrentSubtaskAlongsideComparatorLabel() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-progress-subtask");
        Path firstFile = Files.createFile(directory.resolve("first.txt"));
        Path secondFile = Files.createFile(directory.resolve("second.txt"));
        Param param = getParam(directory);
        List<String> operations = new ArrayList<>();

        try {
            new Ressemblance(param).run((operation, completed, total) -> operations.add(operation), () -> false);

            assertEquals(Arrays.asList(
                    "",
                    "Contenu vidéo",
                    "Contenu vidéo - Calcul des empreintes vidéo",
                    "Contenu vidéo - Calcul des empreintes vidéo",
                    "Contenu vidéo - Comparaison des vidéos",
                    "Contenu vidéo - Comparaison des vidéos"), operations);
        } finally {
            Files.deleteIfExists(firstFile);
            Files.deleteIfExists(secondFile);
            Files.deleteIfExists(directory);
        }
    }

    private static Param getParam(Path directory) {
        RecordingComparator comparator = new RecordingComparator("*", 1, "Contenu vidéo",
                new ArrayList<>()) {
            @Override
            public void compare(ComparisonLines comparisons, Progress progress) {
                progress.setCurrentSubtask("Calcul des empreintes vidéo");
                progress.plus();
                progress.setCurrentSubtask("Comparaison des vidéos");
                progress.plus();
            }
        };
        Param param = new Param();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Collections.singletonList(comparator));
        return param;
    }

    private static class RecordingComparator extends AbstractComparator {
        private final String label;
        private final int progressUnits;
        private final List<String> executionOrder;

        private RecordingComparator(String extension, int progressUnits, String label,
                                    List<String> executionOrder) {
            setExtensions(Set.of(extension));
            this.progressUnits = progressUnits;
            this.label = label;
            this.executionOrder = executionOrder;
        }

        @Override
        public String getLibelle() {
            return label;
        }

        @Override
        public int getTotalProgressUnits(ComparisonLines comparisons) {
            return comparisons.size() * progressUnits;
        }

        @Override
        public void compare(ComparisonLines comparisons, Progress progress) {
            for (ComparisonLine comparison : comparisons) {
                executionOrder.add(label);
                progress.plus(progressUnits);
            }
        }
    }
}
