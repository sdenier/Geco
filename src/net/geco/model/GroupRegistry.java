/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Denier
 * @since Jun 3, 2011
 *
 */
public class GroupRegistry<T extends Group> {

	private Map<String, T> groups;
	private List<String> sortedNames;

	public GroupRegistry() {
		groups = new HashMap<String, T>();
	}
	
	public synchronized void add(T group) {
		groups.put(group.getName(), group);
		sortedNames = null;
	}

	public synchronized Collection<T> getGroups() {
		return groups.values();
	}

	public synchronized T find(String name) {
		return groups.get(name);
	}

	public synchronized void remove(T group) {
		groups.remove(group.getName());
		sortedNames = null;
	}
	
	public synchronized T any() {
		return groups.values().iterator().next();
	}

	public synchronized void updateName(T group, String newName) {
		groups.remove(group.getName());
		group.setName(newName);
		add(group);
	}

	public synchronized List<String> getNames() {
		return new LinkedList<String>(groups.keySet());
	}

	public List<T> getSortedGroups() {
		LinkedList<T> groups = new LinkedList<T>(getGroups());
		Collections.sort(groups, new Comparator<T>() {
			@Override
			public int compare(T c1, T c2) {
				return c1.getName().compareTo(c2.getName());
			}});
		return groups;
	}

	public synchronized List<String> getSortedNames() {
		if( sortedNames==null ){
			sortedNames = getNames();
			Collections.sort(sortedNames);
		}
		return sortedNames;			
	}

}
