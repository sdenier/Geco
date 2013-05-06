/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results.context;

import java.util.HashMap;

/**
 * @author Simon Denier
 * @since May 6, 2013
 *
 */
public class GenericContext extends HashMap<String, Object> {

	public ContextList createContextList(String key) {
		return createContextList(key, 10);
	}

	public ContextList createContextList(String key, int initialCapacity) {
		put(key, new ContextList(initialCapacity));
		return (ContextList) get(key);
	}

}
