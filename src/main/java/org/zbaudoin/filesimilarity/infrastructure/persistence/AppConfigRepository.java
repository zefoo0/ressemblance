package org.zbaudoin.filesimilarity.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.zbaudoin.filesimilarity.domain.comparison.ComparisonStrategy;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.FileContentComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.FileNameComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.ImageContentComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.VideoContentComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.config.DefaultAppConfigFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
public class AppConfigRepository {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.addMixIn(ComparisonStrategy.class, ComparisonStrategyJsonMixin.class);

	private final File file;
	
	public AppConfigRepository(File file) {
		this.file = file;
	}
	
	public void save(AppConfig param) throws RessourceParamException {
		try {
			MAPPER.writeValue(file, param);
		} catch (IOException e) {
			throw new RessourceParamException(e);
		}
	}

	public static String toJson(AppConfig param) throws RessourceParamException {
		try {
			return MAPPER.writeValueAsString(param);
		} catch (IOException e) {
			throw new RessourceParamException(e);
		}
	}

	public static AppConfig fromJson(String json) throws RessourceParamException {
		try {
			return MAPPER.readerForUpdating(DefaultAppConfigFactory.create()).readValue(json);
		} catch (IOException e) {
			throw new RessourceParamException(e);
		}
	}
	
	public AppConfig load() throws RessourceParamException {
		try (BufferedInputStream input = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
			return MAPPER.readerForUpdating(DefaultAppConfigFactory.create()).readValue(input);
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

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
	@JsonSubTypes({
			@JsonSubTypes.Type(value = FileContentComparisonStrategy.class, name = "file-content-comparator"),
			@JsonSubTypes.Type(value = ImageContentComparisonStrategy.class, name = "image-content-comparator"),
			@JsonSubTypes.Type(value = VideoContentComparisonStrategy.class, name = "video-content-comparator"),
			@JsonSubTypes.Type(value = FileNameComparisonStrategy.class, name = "file-name-comparator")
	})
	private abstract static class ComparisonStrategyJsonMixin {
	}
}
