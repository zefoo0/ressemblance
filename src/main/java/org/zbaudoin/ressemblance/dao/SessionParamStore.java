package org.zbaudoin.ressemblance.dao;

import org.zbaudoin.ressemblance.dao.ParamDAO.RessourceParamException;
import org.zbaudoin.ressemblance.model.Param;

import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class SessionParamStore {
	private static final String ACTIVE_KEY = "session.param.active";
	private static final int CHUNK_SIZE = 7000;
	private static final String CHUNK_COUNT_SUFFIX = ".count";

	private final Preferences preferences;

	public SessionParamStore(Preferences preferences) {
		this.preferences = preferences;
	}

	public Param load() throws RessourceParamException {
		try {
			String generation = preferences.get(ACTIVE_KEY, null);
			if (generation == null) {
				return null;
			}

			int chunkCount = preferences.getInt(generation + CHUNK_COUNT_SUFFIX, -1);
			if (chunkCount < 1) {
				throw new IllegalStateException("Invalid session configuration");
			}

			StringBuilder json = new StringBuilder();
			for (int i = 0; i < chunkCount; i++) {
				String chunk = preferences.get(generation + "." + i, null);
				if (chunk == null) {
					throw new IllegalStateException("Incomplete session configuration");
				}
				json.append(chunk);
			}
			return ParamDAO.fromJson(json.toString());
		} catch (IllegalStateException | IllegalArgumentException e) {
			throw new RessourceParamException(e);
		}
	}

	public void save(Param param) throws RessourceParamException {
		String json = ParamDAO.toJson(param);
		String generation = "session.param." + UUID.randomUUID();
		try {
			int start = 0;
			int chunkCount = 0;
			while (start < json.length()) {
				int end = Math.min(start + CHUNK_SIZE, json.length());
				if (end < json.length() && Character.isHighSurrogate(json.charAt(end - 1))) {
					end--;
				}
				preferences.put(generation + "." + chunkCount, json.substring(start, end));
				chunkCount++;
				start = end;
			}
			preferences.putInt(generation + CHUNK_COUNT_SUFFIX, chunkCount);
			String previousGeneration = preferences.get(ACTIVE_KEY, null);
			preferences.put(ACTIVE_KEY, generation);
			preferences.flush();
			removeGeneration(previousGeneration);
		} catch (BackingStoreException | IllegalStateException e) {
			throw new RessourceParamException(e);
		}
	}

	public void clear() throws RessourceParamException {
		try {
			String generation = preferences.get(ACTIVE_KEY, null);
			preferences.remove(ACTIVE_KEY);
			preferences.flush();
			removeGeneration(generation);
		} catch (BackingStoreException | IllegalStateException e) {
			throw new RessourceParamException(e);
		}
	}

	private void removeGeneration(String generation) throws BackingStoreException {
		if (generation == null) {
			return;
		}
		int chunkCount = preferences.getInt(generation + CHUNK_COUNT_SUFFIX, 0);
		for (int i = 0; i < chunkCount; i++) {
			preferences.remove(generation + "." + i);
		}
		preferences.remove(generation + CHUNK_COUNT_SUFFIX);
		preferences.flush();
	}
}
