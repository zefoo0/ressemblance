package org.zbaudoin.ressemblance.service.comparator;

import org.zbaudoin.ressemblance.model.ComparatorErrorException;
import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.service.Progress;
import org.zbaudoin.ressemblance.service.comparator.image.ImageComparatorService;

import java.io.IOException;
import java.util.Set;

public class ContenuImageComparator extends AbstractComparator {

    private static final Set<String> DEFAULT = Set.of("JPG", "JPEG", "PNG", "GIF", "BMP", "TIF",
            "TIFF", "WBMP");
    private final ImageComparatorService service = new ImageComparatorService();

    public ContenuImageComparator() {
        setExtensions(DEFAULT);
    }

    @Override
    public String getLibelle() {
        return "Contenu image";
    }

    @Override
    public int getTotalProgressUnits(ComparisonLines comparisons) {
        return comparisons.size();
    }

    @Override
    public void compare(ComparisonLines comparisons, Progress progress) {
        service.clearFingerprints();
        progress.setCurrentSubtask("Comparaison des images");
        for (ComparisonLine comparison : comparisons) {
            progress.checkCancellation();
            if (!comparison.isResultMax()) {
                try {
                    int similarity = service.compare(comparison.getFile1(), comparison.getFile2());
                    setResult(comparison, similarity);
                } catch (IOException e) {
                    addErreur(comparison, new ComparatorErrorException(e));
                }
            }
            progress.plus();
        }
    }
}
