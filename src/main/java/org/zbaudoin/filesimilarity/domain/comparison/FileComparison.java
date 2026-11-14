package org.zbaudoin.filesimilarity.domain.comparison;



import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FileComparison implements Comparable<FileComparison> {

	private final File file1;
	private final File file2;
	private final List<ComparisonError> erreurs;

	private ComparisonResult result;

	public FileComparison(File file1, File file2) {
		this.file1 = file1;
		this.file2 = file2;
		this.erreurs = new ArrayList<>();
	}

	public void setResult(ComparisonResult result) {
		this.result = result;
	}

	@Override
	public int compareTo(FileComparison o) {
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

	public ComparisonResult getResult() {
		return result;
	}

	public List<ComparisonError> getErreurs() {
		return erreurs;
	}

	public void addErreur(ComparisonError error) {
		 erreurs.add(error);
	}

	public boolean canImproveResult() {
		return result == null || result.getSimilarite() < ComparisonStrategy.MAX_RESULT;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", FileComparison.class.getSimpleName() + "[", "]")
				.add("file1=" + file1)
				.add("file2=" + file2)
				.add("result=" + result)
				.add("erreurs='" + erreurs + "'")
				.toString();
	}
}