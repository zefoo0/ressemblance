package org.zbaudoin.ressemblance.model;

import java.io.*;

public class ParamMonitoring implements Serializable {
	private static final long serialVersionUID = 1L;

	private String dir;
	private int offset;
	
	public ParamMonitoring(String dir, int offset) {
		this.dir = dir;
		this.offset = offset;
	}
	
	public String getDir() {
		return dir;
	}
	
	public void setDir(String dir) {
		this.dir = dir;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public String toString() {
		return "ParamMonitoring [dir=" + dir + ", offset=" + offset + "]";
	}
}
