package org.zbaudoin.ressemblance.ui;

import java.text.DecimalFormat;

class FileSizeFormater {

	    private static final long[] CST_SIZE = {1024, 1024 * 1024, 1024 * 1024 * 1024};
	    private static final String[] CST_UNITS = {"Ko", "Mo", "Go"};

	    public static String format(final long value) {
	        String result = null;
	        long size;
	        for (int i = 0; i < 3; i++) {
	            size=value / CST_SIZE[i];
	            if (size <= 1024) {
	                result = mergeUnit(size, CST_UNITS[i]);
	                break;
	            }
	        }
	        return result;
	    }

	    public static String mergeUnit(long size, String unit) {
	        return new DecimalFormat("#,##0.#").format(size) + " " + unit;
	    }
	}