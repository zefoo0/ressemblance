package org.zbaudoin.ressemblance.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.zbaudoin.ressemblance.model.Param;


public class ParamDAO {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	private final File file;
	
	public ParamDAO(File file) {
		this.file = file;
	}
	
	public void save(Param param) throws RessourceParamException {
		try {
			MAPPER.writeValue(file, param);
		} catch (IOException e) {
			throw new RessourceParamException(e);
		}
	}
	
	public Param load() throws RessourceParamException {
		try (BufferedInputStream input = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
			return MAPPER.readValue(input, Param.class);
		} catch (IOException e) {
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
