package org.zbaudoin.ressemblance.model;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ComparisonLine implements Comparable<ComparisonLine> {

	private final File file1;
	private final File file2;
	private final List<ComparisonLineError> erreurs;

	private ComparisonLineResult result;

	public ComparisonLine(File file1, File file2) {
		this.file1 = file1;
		this.file2 = file2;
		this.erreurs = new ArrayList<>();
	}

	public void setResult(ComparisonLineResult result) {
		this.result = result;
	}

	@Override
	public int compareTo(ComparisonLine o) {
		if (o.getResult() == null && getResult() == null) {
			return 0;
		} else if (o.getResult() == null) {
			return 1;
		} else if (getResult() == null) {
			return -1;
		}
		return Integer.compare(o.getResult().getSimilarite(), getResult().getSimilarite());
	}

	public File getFile1() {
		return file1;
	}
	
	public File getFile2() {
		return file2;
	}

	public ComparisonLineResult getResult() {
		return result;
	}

	public List<ComparisonLineError> getErreurs() {
		return erreurs;
	}

	public void addErreur(ComparisonLineError error) {
		 erreurs.add(error);
	}

	public boolean isResultMax() {
		return result==null || result.getSimilarite() < Comparator.MAX_RESULT;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ComparisonLine.class.getSimpleName() + "[", "]")
				.add("file1=" + file1)
				.add("file2=" + file2)
				.add("result=" + result)
				.add("erreurs='" + erreurs + "'")
				.toString();
	}
}