package org.zbaudoin.filesimilarity.application.comparison;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.AbstractComparisonStrategy;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FileComparisonServiceProgressTest {

    @Test
    public void reportsWeightedProgressWhileProcessingOneComparatorAtATime() throws Exception {
        Path directory = Files.createTempDirectory("ressemblance-progress");
        Path firstText = Files.createFile(directory.resolve("first.txt"));
        Path secondText = Files.createFile(directory.resolve("second.txt"));
        Path binary = Files.createFile(directory.resolve("file.bin"));
        List<String> executionOrder = new ArrayList<>();
        RecordingComparator allFiles = new RecordingComparator("*", 1, "all", executionOrder);
        RecordingComparator textFiles = new RecordingComparator("txt", 4, "text", executionOrder);
        AppConfig param = new AppConfig();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Arrays.asList(allFiles, textFiles));

        List<Long> completedProgress = new ArrayList<>();
        List<Long> totalProgress = new ArrayList<>();
        List<String> currentOperations = new ArrayList<>();
        try {
            List<FileComparison> lines = new FileComparisonService(param).run((operation, completed, total) -> {
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
        AppConfig param = getParam(directory);
        List<String> operations = new ArrayList<>();

        try {
            new FileComparisonService(param).run((operation, completed, total) -> operations.add(operation), () -> false);

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

    private static AppConfig getParam(Path directory) {
        RecordingComparator comparator = new RecordingComparator("*", 1, "Contenu vidéo",
                new ArrayList<>()) {
            @Override
            public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
                progress.setCurrentSubtask("Calcul des empreintes vidéo");
                progress.plus();
                progress.setCurrentSubtask("Comparaison des vidéos");
                progress.plus();
            }
        };
        AppConfig param = new AppConfig();
        param.setFiles(Collections.singletonList(directory.toFile()));
        param.setComparators(Collections.singletonList(comparator));
        return param;
    }

    private static class RecordingComparator extends AbstractComparisonStrategy {
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
        public int getTotalProgressUnits(ComparisonBatch comparisons) {
            return comparisons.size() * progressUnits;
        }

        @Override
        public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
            for (FileComparison comparison : comparisons) {
                executionOrder.add(label);
                progress.plus(progressUnits);
            }
        }
    }
}
