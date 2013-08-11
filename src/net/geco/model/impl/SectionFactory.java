/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Aug 7, 2013
 *
 */
public class SectionFactory extends POFactory {

	public TraceData createTraceData() {
		return new SectionTraceDataImpl();
	}
	
}
