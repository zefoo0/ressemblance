package org.zbaudoin.ressemblance.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ComparisonLines extends ArrayList<ComparisonLine> {

    public ComparisonLines(Collection<ComparisonLine> init) {
        super(init);
    }

    public Set<File> getAllFiles() {
        Set<File> files = new HashSet<>();
        for (ComparisonLine line : this) {
            files.add(line.getFile1());
            files.add(line.getFile2());
        }
        return files;
    }

    public boolean removeComparisonsContaining(File file) {
        Objects.requireNonNull(file, "file");
        return removeIf(line -> file.equals(line.getFile1()) || file.equals(line.getFile2()));
    }
}
