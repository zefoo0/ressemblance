package org.zbaudoin.ressemblance.dao;

import java.io.*;

import org.zbaudoin.ressemblance.model.*;
import org.zbaudoin.util.io.*;


public class ParamDAO {

	private final File file;
	
	public ParamDAO(File file) {
		this.file = file;
	}
	
	public void save(ParamRessemblance param) throws RessourceParamException {
		try {
			FileUtil.writeObject(file, param);
		} catch (IOException e) {
			throw new RessourceParamException(e);
		}
	}
	
	public ParamRessemblance load() throws RessourceParamException {
		try {
			return (ParamRessemblance)FileUtil.readObject(file);
		} catch (ClassNotFoundException | IOException e) {
			throw new RessourceParamException(e);
		}
    }
	
	public static class RessourceParamException extends Exception {
		private static final long serialVersionUID = 1L;

		public RessourceParamException(Throwable t) {
			super(t);
		}
	}
}
