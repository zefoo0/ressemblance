package org.zbaudoin.filesimilarity.domain.comparison;



import java.util.StringJoiner;

public class ComparisonError {

	private final ComparisonStrategy comparator;
	private final Exception exception;

	public ComparisonError(ComparisonStrategy comparator, Exception exception) {
		this.comparator = comparator;
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public ComparisonStrategy getComparator() {
		return comparator;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", ComparisonError.class.getSimpleName() + "[", "]")
				.add("comparator='" + comparator.getLibelle() + "'")
				.add("exception=" + exception.getMessage())
				.toString();
	}
}