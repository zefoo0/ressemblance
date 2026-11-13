package org.zbaudoin.ressemblance.model;


import java.util.StringJoiner;

public class ComparisonLineError {

	private final Comparator comparator;
	private final Exception exception;

	public ComparisonLineError(Comparator comparator, Exception exception) {
		this.comparator = comparator;
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public Comparator getComparator() {
		return comparator;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ComparisonLineError.class.getSimpleName() + "[", "]")
				.add("comparator='" + comparator.getLibelle() + "'")
				.add("exception=" + exception.getMessage())
				.toString();
	}
}