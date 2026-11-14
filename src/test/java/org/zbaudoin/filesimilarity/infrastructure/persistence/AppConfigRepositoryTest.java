package org.zbaudoin.filesimilarity.infrastructure.persistence;

import org.zbaudoin.filesimilarity.domain.comparison.ComparisonStrategy;
import org.zbaudoin.filesimilarity.domain.config.AppConfig;
import org.zbaudoin.filesimilarity.domain.config.ResultTableState;
import org.zbaudoin.filesimilarity.infrastructure.config.DefaultAppConfigFactory;

import org.junit.Test;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppConfigRepositoryTest {

	@Test
	public void savesAndLoadsJson() throws Exception {
		Path path = Files.createTempFile("param-ressemblance", ".json");
		path.toFile().deleteOnExit();
		AppConfig expected = DefaultAppConfigFactory.create();
		expected.setFiles(List.of(new File("photos").getAbsoluteFile()));
		ResultTableState tableState = new ResultTableState();
		tableState.setColumnOrder(Arrays.asList(ResultTableState.NAME1, ResultTableState.FILE1,
				ResultTableState.FILE2, ResultTableState.SCORE, ResultTableState.NAME2,
				ResultTableState.PARENT1, ResultTableState.PARENT2, ResultTableState.SIZE1,
				ResultTableState.SIZE2, ResultTableState.RESULT_COMPARATOR,
				ResultTableState.ERROR_COMPARATORS));
		tableState.setVisibleColumns(Arrays.asList(ResultTableState.NAME1, ResultTableState.FILE1,
				ResultTableState.SCORE));
		tableState.getColumnWidths().put(ResultTableState.NAME1, 275);
		tableState.getColumnWidths().put(ResultTableState.FILE1, 320);
		tableState.setSortColumn(ResultTableState.SCORE);
		tableState.setSortAscending(false);
		expected.setResultTableState(tableState);

		AppConfigRepository dao = new AppConfigRepository(path.toFile());
		dao.save(expected);

		String json = Files.readString(path);
		AppConfig actual = dao.load();
		assertTrue(json.contains("\"type\""));
		assertEquals(comparatorTypes(expected), comparatorTypes(actual));
		assertEquals(expected.getFiles(), actual.getFiles());
		assertEquals(expected.getResultTableState(), actual.getResultTableState());
	}

	@Test
	public void loadsConfigurationWithoutComparatorsUsingDefaults() throws Exception {
		Path path = Files.createTempFile("param-ressemblance-legacy", ".json");
		path.toFile().deleteOnExit();
		Files.write(path, "{\"files\":[\"photos\"]}".getBytes(StandardCharsets.UTF_8));

		AppConfig actual = new AppConfigRepository(path.toFile()).load();

		assertEquals(List.of(new File("photos")), actual.getFiles());
		assertEquals(comparatorTypes(DefaultAppConfigFactory.create()), comparatorTypes(actual));
		assertEquals(new ResultTableState(), actual.getResultTableState());
	}

	@Test
	public void ignoresUnknownJsonPropertiesAndUsesDefaultsForMissingOnes() throws Exception {
		Path path = Files.createTempFile("param-ressemblance-future", ".json");
		path.toFile().deleteOnExit();
		Files.write(path, "{\"newProperty\":true}".getBytes(StandardCharsets.UTF_8));

		AppConfig actual = new AppConfigRepository(path.toFile()).load();

		assertEquals(comparatorTypes(DefaultAppConfigFactory.create()), comparatorTypes(actual));
		assertTrue(actual.getFiles().isEmpty());
		assertEquals(new ResultTableState(), actual.getResultTableState());
	}

	private List<Class<?>> comparatorTypes(AppConfig param) {
		return param.getComparators().stream()
				.map(ComparisonStrategy::getClass)
				.collect(Collectors.toList());
	}
}
