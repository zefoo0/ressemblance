package org.zbaudoin.ressemblance.service.comparator;

import org.zbaudoin.ressemblance.model.ComparisonLine;
import org.zbaudoin.ressemblance.model.ComparisonLines;
import org.zbaudoin.ressemblance.service.Progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FileComparator extends AbstractComparator {

    private final MessageDigest digest;
    private Map<File, String> empruntes;

    public FileComparator() {
        digest = initDigest();
        setExtensions(Set.of("*"));
    }

    @Override
    public String getLibelle() {
        return "Fichier";
    }

    @Override
    public int getTotalProgressUnits(ComparisonLines comparisons) {
        return comparisons.size();
    }

    @Override
    public void compare(ComparisonLines comparisons, Progress progress) {
        empruntes = new HashMap<>();
        for (ComparisonLine comparison : comparisons) {
            progress.checkCancellation();
            if (!comparison.isResultMax()) {
                int valeur = compare(comparison.getFile1(), comparison.getFile2());
                setResult(comparison, valeur);
            }
            progress.plus();
        }
    }

//    @Override
//    public void startCompare() {
//        empruntes = new HashMap<>();
//    }
//
//    @Override
//    public void finishCompare() {
//        empruntes = null;
//    }

    private int compare(File f1, File f2) {
        int result = 0;
        if (f1.length() == f2.length()) {
            try {
                String emprunte1 = empruntes.get(f1);
                if (emprunte1 == null) {
                    emprunte1 = computeImprint(f1, digest);
                    empruntes.put(f1, emprunte1);
                }
                String emprunte2 = empruntes.get(f2);
                if (emprunte2 == null) {
                    emprunte2 = computeImprint(f2, digest);
                    empruntes.put(f2, emprunte2);
                }
                if (emprunte1.equals(emprunte2)) {
                    result = 100;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String computeImprint(File file, MessageDigest digest) throws IOException {
        digest.update(getByte(file));
        byte[] b1 = digest.digest();
        return toHexString(b1);
    }

    private static byte[] getByte(File f) throws IOException {

        byte[] tableau = new byte[(int)f.length()];

        try (FileInputStream in = new FileInputStream(f)) {
            in.read(tableau);
        }

        return tableau;
    }

    /*
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();

        for (byte b : block) {
            byte2hex(b, buf);
        }
        return buf.toString();
    }

    /*
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    private MessageDigest initDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}
