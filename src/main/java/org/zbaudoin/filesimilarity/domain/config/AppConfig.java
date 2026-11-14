package org.zbaudoin.filesimilarity.domain.config;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonStrategy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class AppConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private List<ComparisonStrategy> comparators;
    private List<File> files;
    private ResultTableState resultTableState = new ResultTableState();

    public AppConfig() {
        comparators = new ArrayList<>();
        files = new ArrayList<>();
    }

    public List<ComparisonStrategy> getComparators() {
        return comparators;
    }

    public void setComparators(List<ComparisonStrategy> comparators) {
        this.comparators = comparators;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public ResultTableState getResultTableState() {
        return resultTableState;
    }

    public void setResultTableState(ResultTableState resultTableState) {
        this.resultTableState = resultTableState;
    }

    @Override
    public AppConfig clone() {
        try {
            return (AppConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AppConfig.class.getSimpleName() + "[", "]")
                .add("comparators=" + comparators)
                .add("files=" + files)
                .add("resultTableState=" + resultTableState)
                .toString();
    }
}
