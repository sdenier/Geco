/**
 * Copyright (c) 2014 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.model.CourseSet;

/**
 * @author Simon Denier
 * @since Jan 8, 2014
 *
 */
public class CourseSetImpl implements CourseSet {

	private String name;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
