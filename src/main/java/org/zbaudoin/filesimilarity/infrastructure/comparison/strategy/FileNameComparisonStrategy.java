package org.zbaudoin.filesimilarity.infrastructure.comparison.strategy;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;

import info.debatty.java.stringsimilarity.JaroWinkler;
import java.io.File;
import java.util.Set;

public class FileNameComparisonStrategy extends AbstractComparisonStrategy {

    private final JaroWinkler jaroWinkler = new JaroWinkler();

    public FileNameComparisonStrategy() {
        setExtensions(Set.of("*"));
        setCoefficient(0.25f);
    }

    @Override
    public String getLibelle() {
        return "Nom de fichier";
    }

    @Override
    public int getTotalProgressUnits(ComparisonBatch comparisons) {
        return comparisons.size();
    }

    public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
        for (FileComparison comparison : comparisons) {
            progress.checkCancellation();
            if (comparison.canImproveResult()) {
                int valeur = compare(comparison.getFile1(), comparison.getFile2());
                setResult(comparison, valeur);
            }
            progress.plus();
        }
    }

    public int compare(File f1, File f2) {
        return (int)(jaroWinkler.similarity(supprimerExtension(f1.getName()), supprimerExtension(f2.getName())) * 100);
    }

    private String supprimerExtension(String fileName) {
        String result = fileName;
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            result = result.substring(0, index);
        }
        return result;
    }
}
