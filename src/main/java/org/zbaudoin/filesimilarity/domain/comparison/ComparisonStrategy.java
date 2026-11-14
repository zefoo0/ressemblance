package org.zbaudoin.filesimilarity.domain.comparison;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CancellationException;

public interface ComparisonStrategy extends Serializable {

    int MAX_RESULT = 100;

    String getLibelle();

    Set<String> getExtensions();

    void setExtensions(Set<String> extensions);

    float getCoefficient();

    void setCoefficient(float coefficient);

    int getTotalProgressUnits(ComparisonBatch comparisons);

    void compare(ComparisonBatch comparisons, ComparisonProgress progress) throws CancellationException;
}
