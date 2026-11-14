package org.zbaudoin.filesimilarity.infrastructure.comparison.strategy;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonError;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonException;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonResult;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonStrategy;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;

import java.util.Set;
import java.util.StringJoiner;

public abstract class AbstractComparisonStrategy implements ComparisonStrategy {

    private Set<String> extensions;
    private float coefficient = 1;

    @Override
    public Set<String> getExtensions() {
        return extensions;
    }

    @Override
    public void setExtensions(Set<String> extensions) {
        this.extensions = extensions;
    }

    @Override
    public void setCoefficient(float coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public float getCoefficient() {
        return coefficient;
    }

    protected final void setResult(FileComparison comparison, int valeur) {
        int valeur0 = (int)((float)valeur * getCoefficient());
        if (comparison.getResult() == null || valeur0 > comparison.getResult().getSimilarite()) {
            comparison.setResult(new ComparisonResult(valeur0, this));
        }
    }

    protected final void addErreur(FileComparison comparison, ComparisonException e) {
        comparison.addErreur(new ComparisonError(this, e));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractComparisonStrategy.class.getSimpleName() + "[", "]")
                .add("libelle=" + getLibelle())
                .add("extensions=" + extensions)
                .add("coefficient=" + coefficient)
                .toString();
    }
}
