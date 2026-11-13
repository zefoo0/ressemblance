package org.zbaudoin.ressemblance.model;

import org.zbaudoin.ressemblance.service.comparator.ContenuVideoComparator;
import org.zbaudoin.ressemblance.service.comparator.FileComparator;
import org.zbaudoin.ressemblance.service.comparator.FileNameComparator;

import java.io.*;
import java.util.*;

public class Param implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private List<Comparator> comparators;
	private List<File> files;
	private ResultTableState resultTableState = new ResultTableState();
	
	public Param() {
		comparators = Arrays.asList(new FileComparator(),
				new ContenuVideoComparator(),
				new FileNameComparator());
		files = new ArrayList<>();
	}

	public List<Comparator> getComparators() {
		return comparators;
	}

	public void setComparators(List<Comparator> comparators) {
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
	public Param clone() {
		try {
			return (Param)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", Param.class.getSimpleName() + "[", "]")
				.add("comparators=" + comparators)
				.add("files=" + files)
				.add("resultTableState=" + resultTableState)
				.toString();
	}
}
