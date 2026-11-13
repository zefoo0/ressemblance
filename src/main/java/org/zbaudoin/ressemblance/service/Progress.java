package org.zbaudoin.ressemblance.service;

import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

public class Progress {

    @FunctionalInterface
    public interface Listener {
        void accept(String operation, long completed, long total);
    }

    private final BooleanSupplier cancellationRequested;
    private final Listener progressListener;
    private final long max;
    private long cpt;
    private String operation = "";
    private String subtask = "";

    Progress(long max, Listener progressListener,
             BooleanSupplier cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
        this.progressListener = progressListener;
        this.max = max;
        cpt = 0;

        notifyProgress();
    }

    public final void setCurrentOperation(String operation) {
        this.operation = operation;
        this.subtask = "";
        notifyProgress();
    }

    public final void setCurrentSubtask(String subtask) {
        this.subtask = subtask;
        notifyProgress();
    }

    public final void checkCancellation() {
        if (cancellationRequested.getAsBoolean()) {
            throw new CancellationException("Ressemblance processing was cancelled");
        }
    }

    public final void plus() {
        plus(1);
    }

    public final void plus(int delta) {
        cpt = cpt + delta;
        notifyProgress();
    }

    private void notifyProgress() {
        String label = subtask.isEmpty() ? operation : operation + " - " + subtask;
        progressListener.accept(label, cpt, max);
    }
}
