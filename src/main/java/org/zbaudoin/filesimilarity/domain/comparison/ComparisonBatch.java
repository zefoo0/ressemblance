package org.zbaudoin.filesimilarity.domain.comparison;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ComparisonBatch extends ArrayList<FileComparison> {

    public ComparisonBatch(Collection<FileComparison> init) {
        super(init);
    }

    public Set<File> getAllFiles() {
        Set<File> files = new HashSet<>();
        for (FileComparison line : this) {
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
