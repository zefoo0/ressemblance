package org.zbaudoin.filesimilarity.infrastructure.comparison.strategy;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonException;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;
import org.zbaudoin.filesimilarity.infrastructure.comparison.image.ImageComparisonService;

import java.io.IOException;
import java.util.Set;

public class ImageContentComparisonStrategy extends AbstractComparisonStrategy {

    private static final Set<String> DEFAULT = Set.of("JPG", "JPEG", "PNG", "GIF", "BMP", "TIF",
            "TIFF", "WBMP");
    private final ImageComparisonService service = new ImageComparisonService();

    public ImageContentComparisonStrategy() {
        setExtensions(DEFAULT);
    }

    @Override
    public String getLibelle() {
        return "Contenu image";
    }

    @Override
    public int getTotalProgressUnits(ComparisonBatch comparisons) {
        return comparisons.size();
    }

    @Override
    public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
        service.clearFingerprints();
        progress.setCurrentSubtask("Comparaison des images");
        for (FileComparison comparison : comparisons) {
            progress.checkCancellation();
            if (comparison.canImproveResult()) {
                try {
                    int similarity = service.compare(comparison.getFile1(), comparison.getFile2());
                    setResult(comparison, similarity);
                } catch (IOException e) {
                    addErreur(comparison, new ComparisonException(e));
                }
            }
            progress.plus();
        }
    }
}
