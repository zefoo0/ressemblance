package org.zbaudoin.ressemblance.service.comparator.video;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

public class VideoComparatorService {
    private static final int MAX_FINGERPRINT_THREADS = 8;

    private Map<File, Future<List<Long>>> fingerprints;
    private ExecutorService fingerprintExecutor;
    private CompletionService<List<Long>> completedFingerprints;
    private volatile BooleanSupplier cancellationRequested = () -> false;

    public void startCompare() {
        startCompare(() -> false);
    }

    public void startCompare(BooleanSupplier cancellationRequested) {
        fingerprints = new ConcurrentHashMap<>();
        this.cancellationRequested = cancellationRequested;
        fingerprintExecutor = Executors.newFixedThreadPool(MAX_FINGERPRINT_THREADS);
        completedFingerprints = new ExecutorCompletionService<>(fingerprintExecutor);
    }

    public void finishCompare() {
        Map<File, Future<List<Long>>> cache = fingerprints;
        if (cache != null) {
            for (Future<List<Long>> fingerprint : cache.values()) {
                fingerprint.cancel(true);
            }
        }
        if (fingerprintExecutor != null) {
            fingerprintExecutor.shutdownNow();
        }
        fingerprints = null;
        fingerprintExecutor = null;
        completedFingerprints = null;
    }

    public int scheduleFingerprints(Iterable<File> videos) {
        for (File video : videos) {
            getFingerprintFuture(video);
        }
        return fingerprints.size();
    }

    public Future<List<Long>> pollCompletedFingerprint(long timeoutMillis) throws InterruptedException {
        return completedFingerprints.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public int compare(File videoA, File videoB) throws Exception {

        System.out.println("###### COMPARE : " + videoA.getName() + " <-> " + videoB.getName());
        List<Long> hashesA;
        List<Long> hashesB;
        Map<File, Future<List<Long>>> cache = fingerprints;
        if (cache == null) {
            hashesA = new VideoFrameExtractor().extract(videoA);
            hashesB = new VideoFrameExtractor().extract(videoB);
        } else {
            Future<List<Long>> fingerprintA = getFingerprintFuture(videoA);
            Future<List<Long>> fingerprintB = getFingerprintFuture(videoB);
            hashesA = awaitFingerprint(fingerprintA);
            hashesB = awaitFingerprint(fingerprintB);
        }

        VideoSimilarityService service = new VideoSimilarityService();
        ComparisonResult result = service.compare(hashesA, hashesB);
        System.out.println("### similarity=" + result.similarity + "% ("
            + result.matchingFrames + "/" + result.comparedFrames
            + " aligned samples, offset=" + result.offsetSeconds + " s)");
        return (int) result.similarity;
    }

    private List<Long> awaitFingerprint(Future<List<Long>> fingerprint) throws Exception {
        try {
            while (true) {
                checkCancellation(cancellationRequested);
                try {
                    return fingerprint.get(200, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ignored) {
                    // Recheck cancellation while waiting for FFmpeg to finish.
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Échec du calcul de l'empreinte vidéo", cause);
        }
    }

    private Future<List<Long>> getFingerprintFuture(File video) {
        File key;
        try {
            key = video.getCanonicalFile();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de résoudre le chemin de " + video, e);
        }
        return fingerprints.computeIfAbsent(key,
            file -> completedFingerprints.submit(
                () -> new VideoFrameExtractor().extract(file, cancellationRequested)));
    }

    private static void checkCancellation(BooleanSupplier cancellationRequested) {
        if (cancellationRequested.getAsBoolean()) {
            throw new CancellationException("Video fingerprinting was cancelled");
        }
    }
}