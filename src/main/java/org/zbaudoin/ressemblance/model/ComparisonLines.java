package org.zbaudoin.ressemblance.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparisonLines extends ArrayList<ComparisonLine> {

    public ComparisonLines(Collection<ComparisonLine> init) {
        super(init);
    }

    public Set<File> getAllFiles() {
        Set<File> files = this.stream().map(ComparisonLine::getFile1).collect(Collectors.toSet());
        files.addAll(this.stream().map(ComparisonLine::getFile2).collect(Collectors.toSet()));
        return files;
    }

    public boolean removeComparisonsContaining(File file) {
        Objects.requireNonNull(file, "file");
        return removeIf(line -> file.equals(line.getFile1()) || file.equals(line.getFile2()));
    }
}
