package org.zbaudoin.ressemblance.model;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CancellationException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.zbaudoin.ressemblance.service.Progress;
import org.zbaudoin.ressemblance.service.comparator.ContenuVideoComparator;
import org.zbaudoin.ressemblance.service.comparator.FileComparator;
import org.zbaudoin.ressemblance.service.comparator.FileNameComparator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileComparator.class, name = "file"),
        @JsonSubTypes.Type(value = ContenuVideoComparator.class, name = "video-content"),
        @JsonSubTypes.Type(value = FileNameComparator.class, name = "file-name")
})
public interface Comparator extends Serializable {

    int MAX_RESULT = 100;

    String getLibelle();
    Set<String> getExtensions();
    void setExtensions(Set<String> extensions);
    float getCoefficient();
    void setCoefficient(float coefficient);

    int getTotalProgressUnits(ComparisonLines comparisons);
    void compare(ComparisonLines comparisons, Progress progress) throws CancellationException;
}
