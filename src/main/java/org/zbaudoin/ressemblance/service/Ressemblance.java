package org.zbaudoin.ressemblance.service;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;

import org.zbaudoin.ressemblance.model.*;



public class Ressemblance {

	private final ParamRessemblance param;
	
	public Ressemblance(ParamRessemblance param) {
		this.param = param;
	}
	
	public List<Seem> run() {
		
		List<Seem> seems = new ArrayList<>();
		
		MessageDigest digest = initDigest();
		ComparatorLevel<ZFile> comparator = initComparator(digest);
		
		List<File> dirs = param.getFiles();
		
		if (!dirs.isEmpty()) {
			Set<ZFile> files = getFiles(dirs);
			seems = process(files, comparator);
			
			Collections.sort(seems);
		}
		
		return seems;
	}
	
	private Set<ZFile> getFiles(List<File> rootDirs) {
		
		Set<ZFile> files = new HashSet<>();

        for (File dir : rootDirs) {
            addFiles(files, dir);
        }
		
		return files;
	}
	
	private void addFiles(Set<ZFile> zfiles, File dir) {
		if (!dir.isDirectory()) return;
		
		File[] files0 = dir.listFiles();
		if (files0 != null) {
			for (File file : files0) {
				if (file.isDirectory()) {
					addFiles(zfiles, file);
				} else {
					zfiles.add(new ZFile(file));
				}
			}
		}
	}
	
	private ComparatorLevel<ZFile> initComparator(final MessageDigest digest) {
		return (f1, f2) -> {

            if (f1.length()==f2.length()) {

                try {
                    if (f1.getImprint()==null) f1.setImprint(computeImprint(f1, digest));
                    if (f2.getImprint()==null) f2.setImprint(computeImprint(f2, digest));
                    if (f1.getImprint().equals(f2.getImprint())) return 9999;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (f1.getWords()==null) f1.setWords(extractWords(f1));
            if (f2.getWords()==null) f2.setWords(extractWords(f2));

            int cpt = 0;
            for (String word1 : f1.getWords()) {
                if (f2.getWords().contains(word1)) cpt = cpt + 10;
            }

            cpt = cpt + computeLenghtLevel(f1, f2);

            return cpt;
        };
	}
	
	private int computeLenghtLevel(ZFile f1, ZFile f2) {
		double decreasePercent = (double)Math.min(f1.length(), f2.length()) / (double)Math.max(f1.length(), f2.length());
		return f(100 - (int)(decreasePercent * 100));
	}
	
	/**
	 * @return y = 1/400 * (60 - x)²
	 */
	private int f(int x) {
		int x0 = Math.min(x, 60);
		return (60 - x0) * (60 - x0) / 400; 
	}
	
	private List<Seem> process(Set<ZFile> files0, ComparatorLevel<ZFile> comparator) {
		
		List<Seem> seems = new ArrayList<>();
		
		List<ZFile> files = new ArrayList<>(files0);
		
		for (int n = 0; n < files.size(); n++) {
			ZFile f1= files.get(n);
			for (int m = n+1; m < files.size(); m++) {
				ZFile f2 = files.get(m);
				int level = comparator.compare(f1, f2);
				if (level > 0) seems.add(new Seem(f1, f2, level));
			}
		}
		
		return seems;
	}
	
    private MessageDigest initDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
        	return null;
        }
    }

	private List<String> extractWords(File file) {
		
		List<String> resWords = new ArrayList<>();
		
		StringBuilder regex = new StringBuilder("[");
		for (String sep : param.getSeparators()) {
			regex.append(sep);
		}
		regex.append("]");
		
		String[] words = file.getName().split(regex.toString());
        for (String s : words) {
            String word = s.toUpperCase();
            if (!word.isEmpty() && !param.getExcludes().contains(word)) {
                resWords.add(word);
            }
        }
		
		Collections.sort(resWords);
		
		return resWords;
	}
	    	
    private static String computeImprint(File file, MessageDigest digest) throws IOException {
        digest.update(getByte(file));
        byte[] b1 = digest.digest();
        return toHexString(b1);
    }
         
    private static byte[] getByte(File f) throws IOException {
        
        byte[] tableau = new byte[(int)f.length()];
        
        try (FileInputStream in = new FileInputStream(f)) {
        	in.read(tableau);	
        }

        return tableau;
    }

    /*
     * Converts a byte array to hex string
     */
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();

        int len = block.length;

        for (byte b : block) {
            byte2hex(b, buf);
        }
        return buf.toString();
    }

    /*
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
	
	public static class ZFile extends File {
		
		private static final long serialVersionUID = 1L;
		
		private String imprint;
		private List<String> words;
		
		private ZFile(File f) {
			super(f.getAbsolutePath());
		}
		
		public void setWords(List<String> words) {
			this.words = words;
		}
		
		public List<String> getWords() {
			return words;
		}

		public void setImprint(String imprint) {
			this.imprint = imprint;
		}
		
		public String getImprint() {
			return imprint;
		}						
	}
	
	public static class FileSizeFormater {

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
	
	public interface ComparatorLevel<T> {
		int compare(T o1, T o2);
	}
}
