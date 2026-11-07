package org.zbaudoin.ressemblance.model;


public class Seem implements Comparable<Seem> {

	private final Object e1;
	private final Object e2;
	private final int level;
	
	public Seem(Object e1, Object e2, int level) {
		this.e1 = e1;
		this.e2 = e2;
		this.level = level;
	}
	
	@Override
	public int compareTo(Seem o) {
		return o.level - level;
	}
	
	public Object getElement1() {
		return e1;
	}
	
	public Object getElement2() {
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