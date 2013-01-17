/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

/**
 * @author Simon Denier
 * @since Jan 3, 2013
 *
 */
public class RefMap {

	private Object[] refs;

	public RefMap(int capacity) {
		refs = new Object[capacity];
	}

	public void put(int i, Object object) {
		refs[i] = object;
	}

	public Object get(int i) {
		return refs[i];
	}

}
