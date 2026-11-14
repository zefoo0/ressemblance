package org.zbaudoin.filesimilarity.infrastructure.comparison.strategy;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonProgress;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonException;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileContentComparisonStrategy extends AbstractComparisonStrategy {

    private final MessageDigest digest;
    private Map<File, byte[]> empruntes;

    public FileContentComparisonStrategy() {
        digest = initDigest();
        setExtensions(Set.of("*"));
    }

    @Override
    public String getLibelle() {
        return "Fichier";
    }

    @Override
    public int getTotalProgressUnits(ComparisonBatch comparisons) {
        return comparisons.size();
    }

    @Override
    public void compare(ComparisonBatch comparisons, ComparisonProgress progress) {
        empruntes = new HashMap<>();
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
            progress.plus();
        }
    }

    private int compare(File f1, File f2) throws ComparisonException {
        int result = 0;
        if (f1.length() == f2.length()) {
            try {
                byte[] emprunte1 = empruntes.get(f1);
                if (emprunte1 == null) {
                    emprunte1 = computeImprint(f1, digest);
                    empruntes.put(f1, emprunte1);
                }
                byte[] emprunte2 = empruntes.get(f2);
                if (emprunte2 == null) {
                    emprunte2 = computeImprint(f2, digest);
                    empruntes.put(f2, emprunte2);
                }
                if (MessageDigest.isEqual(emprunte1, emprunte2)) {
                    result = 100;
                }
            } catch (IOException e) {
                throw new ComparisonException(e);
            }
        }
        return result;
    }

    private static byte[] computeImprint(File file, MessageDigest digest) throws IOException {
        byte[] buffer = new byte[8192];
        digest.reset();
        try {
            try (InputStream input = java.nio.file.Files.newInputStream(file.toPath())) {
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return digest.digest();
        } finally {
            digest.reset();
        }
    }

    private MessageDigest initDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 digest is not available", e);
        }
    }

}
