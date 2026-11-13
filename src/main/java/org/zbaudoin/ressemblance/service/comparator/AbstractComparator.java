package org.zbaudoin.ressemblance.service.comparator;

import org.zbaudoin.ressemblance.model.*;

import java.util.Set;
import java.util.StringJoiner;

public abstract class AbstractComparator implements Comparator {

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

    protected final void setResult(ComparisonLine comparison, int valeur) {
        int valeur0 = (int)((float)valeur * getCoefficient());
        if (comparison.getResult() == null || valeur0 > comparison.getResult().getSimilarite()) {
            comparison.setResult(new ComparisonLineResult(valeur0, this));
        }
    }

    protected final void addErreur(ComparisonLine comparison, ComparatorErrorException e) {
        comparison.addErreur(new ComparisonLineError(this, e));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractComparator.class.getSimpleName() + "[", "]")
                .add("libelle=" + getLibelle())
                .add("extensions=" + extensions)
                .add("coefficient=" + coefficient)
                .toString();
    }
}
