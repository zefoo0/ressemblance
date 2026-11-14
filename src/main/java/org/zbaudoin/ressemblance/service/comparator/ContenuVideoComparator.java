package org.zbaudoin.ressemblance.service.comparator;

import org.zbaudoin.ressemblance.model.ComparatorErrorException;
import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.service.Progress;
import org.zbaudoin.ressemblance.service.comparator.video.VideoComparatorService;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

public class ContenuVideoComparator extends AbstractComparator {

    private static final int PROGRESSION_EMPREINTE = 10;
    private static final int PROGRESSION_COMPARE = 1;

    private static final Set<String> DEFAULT = Set.of("AVI", "MOV", "FLV", "WMV", "MP4", "MPG");
    private final VideoComparatorService service = new VideoComparatorService();

    public ContenuVideoComparator() {
        setExtensions(DEFAULT);
    }

    @Override
    public String getLibelle() {
        return "Contenu vidéo";
    }

    @Override
    public int getTotalProgressUnits(ComparisonLines comparisons) {
        return comparisons.getAllFiles().size() * PROGRESSION_EMPREINTE + comparisons.size() * PROGRESSION_COMPARE;
    }

    @Override
    public void compare(ComparisonLines comparisons, Progress progress) {
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
            for (ComparisonLine comparison : comparisons) {
                progress.checkCancellation();
                if (comparison.isResultMax()) {
                    try {
                        int valeur = compare(comparison.getFile1(), comparison.getFile2());
                        setResult(comparison, valeur);
                    } catch (ComparatorErrorException e) {
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

    private int compare(File f1, File f2) throws ComparatorErrorException {
        try {
            return service.compare(f1, f2);
        } catch (CancellationException e) {
            throw e;
        } catch (Exception e) {
            throw new ComparatorErrorException(e);
        }
    }
}
