package org.zbaudoin.filesimilarity.domain.comparison;

public interface ComparisonProgress {
    void setCurrentSubtask(String subtask);

    void checkCancellation();

    void plus();

    void plus(int delta);
}
