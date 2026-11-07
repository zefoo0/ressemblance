package org.zbaudoin.ressemblance.model;


public class ComparisonLine implements Comparable<ComparisonLine> {

	private final ZFile e1;
	private final ZFile e2;
	private final int level;
	
	public ComparisonLine(ZFile e1, ZFile e2, int level) {
		this.e1 = e1;
		this.e2 = e2;
		this.level = level;
	}
	
	@Override
	public int compareTo(ComparisonLine o) {
		return o.level - level;
	}
	
	public ZFile getElement1() {
		return e1;
	}
	
	public ZFile getElement2() {
		return e2;
	}

	public int getLevel() {
		return level;
	}
	
	@Override
	public String toString() {
		return "Seem [e1=" + e1 + ", e2=" + e2 + ", level=" + level + "]";
	}
}