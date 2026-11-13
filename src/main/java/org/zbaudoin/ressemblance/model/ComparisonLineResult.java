package org.zbaudoin.ressemblance.model;


import java.util.StringJoiner;

public class ComparisonLineResult {

	private final int similarite;
	private final Comparator comparator;

	public ComparisonLineResult(int similarite, Comparator comparator) {
		this.similarite = similarite;
		this.comparator = comparator;
	}

	public int getSimilarite() {
		return similarite;
	}

	public Comparator getComparator() {
		return comparator;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ComparisonLineResult.class.getSimpleName() + "[", "]")
				.add("similarite=" + similarite)
				.add("comparator='" + comparator.getLibelle() + "'")
				.toString();
	}
}