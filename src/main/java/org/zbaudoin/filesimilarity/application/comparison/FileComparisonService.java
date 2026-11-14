package org.zbaudoin.filesimilarity.application.comparison;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonBatch;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonStrategy;
import org.zbaudoin.filesimilarity.domain.comparison.FileComparison;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class FileComparisonService {

	private final AppConfig param;
	
	public FileComparisonService(AppConfig param) {
		this.param = param;
	}
	
	public List<FileComparison> run(Progress.Listener progressListener, BooleanSupplier cancellationRequested) {
		Objects.requireNonNull(progressListener);
		Objects.requireNonNull(cancellationRequested);

		List<FileComparison> result;

		System.out.println("================ DEBUT");
		long tps = System.currentTimeMillis();

		List<File> dirs = param.getFiles();
		if (!dirs.isEmpty()) {
			Set<File> files = collecterFichiers(dirs);
			List<FileComparison> comparisonLines = peuplerComparaisonLine(files);
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
	
	private List<FileComparison> process(List<FileComparison> comparisonLines,
			Progress.Listener progressListener, BooleanSupplier cancellationRequested) {
		long totalProgress = 0;
		Map<ComparisonStrategy, ComparisonBatch> lignesParComparaison = new HashMap<>();
		for (ComparisonStrategy comparator : param.getComparators()) {
			ExtensionValidator validator = new ExtensionValidator(comparator.getExtensions());
			ComparisonBatch lines = new ComparisonBatch(comparisonLines.stream()
					.filter(line -> validator.validate(line.getFile1()) && validator.validate(line.getFile2()))
					.collect(Collectors.toList()));
			lignesParComparaison.put(comparator, lines);
			totalProgress += comparator.getTotalProgressUnits(lines);
		}
		Progress progres = new Progress(totalProgress, progressListener, cancellationRequested);

		for (ComparisonStrategy comparator : param.getComparators()) {
			checkCancellation(cancellationRequested);
			ComparisonBatch lines = lignesParComparaison.get(comparator);
			if (lines != null) {
				progres.setCurrentOperation(comparator.getLibelle());
				comparator.compare(lines, progres);
			}
		}

		return comparisonLines;
	}

	private List<FileComparison> peuplerComparaisonLine(Set<File> files0) {
		List<FileComparison> result = new ArrayList<>();

		List<File> files = new ArrayList<>(files0);
		for (int n = 0; n < files.size(); n++) {
			File f1= files.get(n);
			for (int m = n+1; m < files.size(); m++) {
				File f2 = files.get(m);
				result.add(new FileComparison(f1, f2));
			}
		}

		return result;
	}

	private void checkCancellation(BooleanSupplier cancellationRequested) {
		if (cancellationRequested.getAsBoolean()) {
			throw new CancellationException("FileComparisonService processing was cancelled");
		}
	}

	/** Null or empty extensions match no files; "*" explicitly matches every extension. */
	private static class ExtensionValidator {

		private final Set<String> extensions;
		private final boolean acceptsAllExtensions;

		private ExtensionValidator(Set<String> extensions) {
			Set<String> normalizedExtensions = extensions == null ? Collections.emptySet() : extensions.stream()
							.filter(e -> !e.isBlank())
							.map(e -> e.trim().toUpperCase(Locale.ROOT))
							.collect(Collectors.toSet());
			acceptsAllExtensions = normalizedExtensions.contains("*");
			this.extensions = acceptsAllExtensions ? Collections.emptySet() : normalizedExtensions;
		}

		private boolean validate(File file) {
			return acceptsAllExtensions || extensions.contains(extractExtension(file.getName()));
		}

		private String extractExtension(String fileName) {
			String extension;
			int index = fileName.lastIndexOf(".");
			if (index != -1) {
				extension = fileName.substring(index + 1);
			} else {
				extension = "";
			}
			return extension.toUpperCase(Locale.ROOT);
		}
	}
}
