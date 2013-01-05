/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.Reader;

import net.geco.model.iojson.PersistentStore.K;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Simon Denier
 * @since Jan 5, 2013
 *
 */
public class JSONStore {

	private JSONObject jsonRoot;

	private RefMap refMap;

	public JSONStore(Reader reader) throws JSONException {
		jsonRoot = new JSONObject(new JSONTokener(reader));
		initRefMap(jsonRoot.getInt(K.MAXID) + 1);
	}

	/*
	 * For testing purpose
	 */
	public JSONStore(int capacity) throws JSONException {
		initRefMap(capacity);
	}

	/*
	 * RefMap capacity: [1..MAXID] + 0 index reserved for null
	 */
	private void initRefMap(int capacity) throws JSONException {
		refMap = new RefMap(capacity);
	}
	
	public JSONArray getJSONArray(String key) throws JSONException {
		return jsonRoot.getJSONArray(key);
	}

	public <T> T register(T object, int id) {
		refMap.put(id, object);
		return object;
	}

	@SuppressWarnings("unchecked")
	public <T> T retrieve(int id, Class<T> clazz) {
		return (T) refMap.get(id);
	}

}
