package org.zbaudoin.ressemblance.model;

import java.io.File;
import java.util.List;

public class ZFile extends File {

    private static final long serialVersionUID = 1L;

    private String imprint;
    private List<String> words;

    public ZFile(File f) {
        super(f.getAbsolutePath());
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public String getImprint() {
        return imprint;
    }

    public void setImprint(String imprint) {
        this.imprint = imprint;
    }
}
