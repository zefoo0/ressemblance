package org.zbaudoin.filesimilarity.domain.comparison;



import java.util.StringJoiner;

public class ComparisonResult {

	private final int similarite;
	private final ComparisonStrategy comparator;

	public ComparisonResult(int similarite, ComparisonStrategy comparator) {
		this.similarite = similarite;
		this.comparator = comparator;
	}

	public int getSimilarite() {
		return similarite;
	}

	public ComparisonStrategy getComparator() {
		return comparator;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ComparisonResult.class.getSimpleName() + "[", "]")
				.add("similarite=" + similarite)
				.add("comparator='" + comparator.getLibelle() + "'")
				.toString();
	}
}