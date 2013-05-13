/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results.context;

import java.util.ArrayList;

/**
 * @author Simon Denier
 * @since May 6, 2013
 *
 */
public class ContextList extends ArrayList<GenericContext> {

	public ContextList(int initialCapacity) {
		super(initialCapacity);
	}
	
	public <T extends GenericContext> T addContext(T context) {
		add(context);
		return context;
	}

}
