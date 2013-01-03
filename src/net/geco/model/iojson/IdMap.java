/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Denier
 * @since Jan 3, 2013
 *
 */
public class IdMap {

	private int newId;

	private Map<Object, Integer> idMap;

	public IdMap() {
		newId = 0;
		idMap = new HashMap<Object, Integer>();
	}
	
	public int idFor(Object object) {
		if( ! idMap.containsKey(object) ){
			idMap.put(object, ++newId);
		}
		return idMap.get(object);
	}

	public int maxId() {
		return newId;
	}

}
