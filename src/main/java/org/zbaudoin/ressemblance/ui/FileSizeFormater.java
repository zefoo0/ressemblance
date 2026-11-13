package org.zbaudoin.ressemblance.ui;

import java.text.DecimalFormat;

class FileSizeFormater {

	private static final String[] UNITS = {"o", "Ko", "Mo", "Go", "To", "Po", "Eo"};

	public static String format(final long value) {
		if (value < 1024) {
			return value + " o";
		}
		double size = value;
		int unit = 0;
		while (size >= 1024 && unit < UNITS.length - 1) {
			size /= 1024;
			unit++;
		}
		return new DecimalFormat("#,##0.#").format(size) + " " + UNITS[unit];
	}
}