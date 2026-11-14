package org.zbaudoin.ressemblance.dao;

import org.junit.Test;
import org.zbaudoin.ressemblance.model.Param;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SessionParamStoreTest {

	@Test
	public void storesLargeConfigurationsAndClearsThem() throws Exception {
		Preferences preferences = Preferences.userRoot().node("org/zbaudoin/ressemblance/test/" + UUID.randomUUID());
		try {
			SessionParamStore store = new SessionParamStore(preferences);
			Param expected = new Param();
			List<File> files = new ArrayList<>();
			for (int i = 0; i < 400; i++) {
				files.add(new File("/configuration/directory/" + i + "/with/a/long/path"));
			}
			expected.setFiles(files);

			store.save(expected);

			assertEquals(expected.getFiles(), store.load().getFiles());
			store.clear();
			assertNull(store.load());
		} finally {
			preferences.removeNode();
		}
	}
}
