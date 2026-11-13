package org.zbaudoin.ressemblance.service;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bytedeco.tesseract.PAGE_RES;
import org.zbaudoin.ressemblance.model.*;
import org.zbaudoin.ressemblance.model.Comparator;


public class Ressemblance {

	private final Param param;
	
	public Ressemblance(Param param) {
		this.param = param;
	}
	
	public List<ComparisonLine> run() {
		return run((completed, total) -> { }, () -> false);
	}

	public List<ComparisonLine> run(BiConsumer<Long, Long> progressListener, BooleanSupplier cancellationRequested) {
		Objects.requireNonNull(progressListener);
		return run((operation, completed, total) -> progressListener.accept(completed, total),
				cancellationRequested);
	}

	public List<ComparisonLine> run(Progress.Listener progressListener, BooleanSupplier cancellationRequested) {
		Objects.requireNonNull(progressListener);
		Objects.requireNonNull(cancellationRequested);

		List<ComparisonLine> result;

		System.out.println("================ DEBUT");
		long tps = System.currentTimeMillis();

		List<File> dirs = param.getFiles();
		if (!dirs.isEmpty()) {
			Set<File> files = collecterFichiers(dirs);
			List<ComparisonLine> comparisonLines = peuplerComparaisonLine(files);
			result = process(comparisonLines, progressListener, cancellationRequested);
			Collections.sort(result);
		} else {
			result = new ArrayList<>();
			progressListener.accept("", 0L, 0L);
		}
		System.out.println("================ TEMPS TOTAL = " + ((System.currentTimeMillis() - tps) / 1000) + " secondes");
		return result;
	}
	
	private Set<File> collecterFichiers(List<File> rootDirs) {
		Set<File> files = new HashSet<>();
        for (File dir : rootDirs) {
            addFiles(files, dir);
        }
		
		return files;
	}
	
	private void addFiles(Set<File> files, File dir) {
		if (!dir.isDirectory()) return;
		
		File[] files0 = dir.listFiles();
		if (files0 != null) {
			for (File file : files0) {
				if (file.isDirectory()) {
					addFiles(files, file);
				} else {
					files.add(file);
				}
			}
		}
	}
	
	private List<ComparisonLine> process(List<ComparisonLine> comparisonLines,
			Progress.Listener progressListener, BooleanSupplier cancellationRequested) {
		long totalProgress = 0;
		Map<Comparator, ComparisonLines> lignesParComparaison = new HashMap<>();
		for (Comparator comparator : param.getComparators()) {
			ExtensionValidator validator = new ExtensionValidator(comparator.getExtensions());
			ComparisonLines lines = new ComparisonLines(comparisonLines.stream()
					.filter(line -> validator.validate(line.getFile1()) && validator.validate(line.getFile2()))
					.collect(Collectors.toList()));
			lignesParComparaison.put(comparator, lines);
			totalProgress += comparator.getTotalProgressUnits(lines);
		}
		Progress progres = new Progress(totalProgress, progressListener, cancellationRequested);

		long completedProgress = 0;
		for (Comparator comparator : param.getComparators()) {
			checkCancellation(cancellationRequested);
			ComparisonLines lines = lignesParComparaison.get(comparator);
			if (lines != null) {
				progres.setCurrentOperation(comparator.getLibelle());
				comparator.compare(lines, progres);
			}
		}

		return comparisonLines;
	}

	private List<ComparisonLine> peuplerComparaisonLine(Set<File> files0) {
		List<ComparisonLine> result = new ArrayList<>();

		List<File> files = new ArrayList<>(files0);
		for (int n = 0; n < files.size(); n++) {
			File f1= files.get(n);
			for (int m = n+1; m < files.size(); m++) {
				File f2 = files.get(m);
				result.add(new ComparisonLine(f1, f2));
			}
		}

		return result;
	}

	private void checkCancellation(BooleanSupplier cancellationRequested) {
		if (cancellationRequested.getAsBoolean()) {
			throw new CancellationException("Ressemblance processing was cancelled");
		}
	}

	private static class ExtensionValidator {

		private final Set<String> extensions;

		private ExtensionValidator(Set<String> extensions) {
			Set<String> normalisedExtensions = extensions == null ? Collections.emptySet() : extensions.stream()
							.filter(e -> !e.isBlank())
							.map(e -> e.trim().toUpperCase(Locale.ROOT))
							.collect(Collectors.toSet());
			if (normalisedExtensions.isEmpty()) {
				this.extensions = null;
			} else if (normalisedExtensions.contains("*")) {
				this.extensions = Collections.emptySet();
			} else {
				this.extensions = normalisedExtensions;
			}
		}

		private boolean validate(File file) {
			return extensions != null && (extensions.isEmpty() || extensions.contains(extractExtension(file.getName())));
		}

		private String extractExtension(String fileName) {
			String extension;
			int index = fileName.lastIndexOf(".");
			if (index != -1) {
				extension = fileName.substring(index + 1, fileName.length());
			} else {
				extension = "";
			}
			return extension.toUpperCase(Locale.ROOT);
		}
	}
}
