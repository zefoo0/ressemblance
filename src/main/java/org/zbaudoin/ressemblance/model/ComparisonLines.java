package org.zbaudoin.ressemblance.model;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparisonLines extends ArrayList<ComparisonLine> {

    public ComparisonLines(Collection<ComparisonLine> init) {
        super(init);
    }

    public ComparisonLines excludeResultMax(ComparisonLines lines) {
        return new ComparisonLines(lines.stream().filter(l -> !l.isResultMax()).collect(Collectors.toSet()));
    }

    public Set<File> getAllFiles() {
        Set<File> files = this.stream().map(c -> c.getFile1()).collect(Collectors.toSet());
        files.addAll(this.stream().map(c -> c.getFile2()).collect(Collectors.toSet()));
        return files;
    }
}
