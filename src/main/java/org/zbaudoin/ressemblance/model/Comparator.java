package org.zbaudoin.ressemblance.model;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public static final int MAX_RESULT = 100;
    public static final int MIN_RESULT = 0;

    String getLibelle();
    Set<String> getExtensions();
    void setExtensions(Set<String> extensions);
    float getCoefficient();
    void setCoefficient(float coefficient);

    int getTotalProgressUnits(ComparisonLines comparisons);
    void compare(ComparisonLines comparisons, Progress progress) throws CancellationException;
}
