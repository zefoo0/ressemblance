package org.zbaudoin.ressemblance.model;

import java.io.*;
import java.util.*;

public class ParamRessemblance implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	private List<String> excludes;
	private List<String> separators;
	private List<File> files;
	
	public ParamRessemblance() {
		excludes = new ArrayList<>();
		separators = new ArrayList<>();
		files = new ArrayList<>();

		defaultSeparator();
		defaultExcludes();
	}
	
	private void defaultSeparator() {
		separators.add("[.]");
		separators.add("[_]");
		separators.add("[-]");
		separators.add("[\\s]");
		separators.add("[,]");
	}
	
	private void defaultExcludes() {
		excludes.add("AVI");
		excludes.add("MOV");
		excludes.add("FLV");
		excludes.add("WMV");
		excludes.add("MP4");
		excludes.add("MPG");
	}

	public List<String> getExcludes() {
		return excludes;
	}
	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}
	public List<String> getSeparators() {
		return separators;
	}
	public void setSeparators(List<String> separators) {
		this.separators = separators;
	}
	public List<File> getFiles() {
		return files;
	}
	public void setFiles(List<File> files) {
		this.files = files;
	}

	@Override
	public ParamRessemblance clone() {
		try {
			return (ParamRessemblance)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return "ParamRessemblance [excludes=" + excludes + ", separators="
				+ separators + ", files=" + files + "]";
	}
}
