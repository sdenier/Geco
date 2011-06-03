/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Denier
 * @since Jun 3, 2011
 *
 */
public class GroupRegistry<T extends Group> {

	private Map<String, T> groups;

	public GroupRegistry() {
		groups = new HashMap<String, T>();
	}
	
	public void add(T group) {
		groups.put(group.getName(), group);
	}

	public Collection<T> getGroups() {
		return groups.values();
	}

	public T find(String name) {
		return groups.get(name);
	}

	public void remove(T group) {
		groups.remove(group.getName());
	}
	
	public T any() {
		return groups.values().iterator().next();
	}

	public void updateName(T group, String newName) {
		groups.remove(group.getName());
		group.setName(newName);
		add(group);
	}

}
