package org.zbaudoin.filesimilarity.infrastructure.comparison.strategy;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonException;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;
import org.zbaudoin.filesimilarity.infrastructure.comparison.video.VideoComparisonService;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

public class VideoContentComparisonStrategy extends AbstractComparisonStrategy {

    private static final int PROGRESSION_EMPREINTE = 10;
    private static final int PROGRESSION_COMPARE = 1;

    private static final Set<String> DEFAULT = Set.of("AVI", "MOV", "FLV", "WMV", "MP4", "MPG");
    private final VideoComparisonService service = new VideoComparisonService();

    public VideoContentComparisonStrategy() {
        setExtensions(DEFAULT);
    }

    @Override
    public String getLibelle() {
        return "Contenu vidéo";
    }

    @Override
    public int getTotalProgressUnits(ComparisonBatch comparisons) {
        return comparisons.getAllFiles().size() * PROGRESSION_EMPREINTE + comparisons.size() * PROGRESSION_COMPARE;
    }

    @Override
    public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
        service.startCompare(() -> {
            progress.checkCancellation();
            return false;
        });
        try {
            progress.checkCancellation();
            progress.setCurrentSubtask("Calcul des empreintes");
            int fingerprintsToCompute = service.scheduleFingerprints(comparisons.getAllFiles());
            int fingerprintsCompleted = 0;
            while (fingerprintsCompleted < fingerprintsToCompute) {
                progress.checkCancellation();
                Future<?> completed = service.pollCompletedFingerprint(200);
                if (completed != null) {
                    progress.plus(PROGRESSION_EMPREINTE);
                    fingerprintsCompleted++;
                }
            }

            progress.setCurrentSubtask("Comparaison");
            for (FileComparison comparison : comparisons) {
                progress.checkCancellation();
                if (comparison.canImproveResult()) {
                    try {
                        int valeur = compare(comparison.getFile1(), comparison.getFile2());
                        setResult(comparison, valeur);
                    } catch (ComparisonException e) {
                        addErreur(comparison, e);
                    }
                }
                progress.plus(PROGRESSION_COMPARE);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CancellationException cancellation = new CancellationException("Calcul des empreintes vidéo interrompu");
            cancellation.initCause(e);
            throw cancellation;
        } finally {
            service.finishCompare();
        }
    }

    private int compare(File f1, File f2) throws ComparisonException {
        try {
            return service.compare(f1, f2);
        } catch (CancellationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComparisonException(e);
        }
    }
}
