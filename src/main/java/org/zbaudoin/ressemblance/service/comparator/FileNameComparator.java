package org.zbaudoin.ressemblance.service.comparator;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.service.Progress;

import java.io.File;
import java.util.Set;

public class FileNameComparator extends AbstractComparator {

    private final JaroWinkler jaroWinkler = new JaroWinkler();

    public FileNameComparator() {
        setExtensions(Set.of("*"));
        setCoefficient(0.25f);
    }

    @Override
    public String getLibelle() {
        return "Nom de fichier";
    }

    @Override
    public int getTotalProgressUnits(ComparisonLines comparisons) {
        return comparisons.size();
    }

    public void compare(ComparisonLines comparisons, Progress progress) {
        for (ComparisonLine comparison : comparisons) {
            progress.checkCancellation();
            if (comparison.isResultMax()) {
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
