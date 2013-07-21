/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import net.geco.model.Punch;
import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Aug 7, 2011
 *
 */
public interface Tracer {

	public void computeTrace(int[] codes, Punch[] punches);

	public int getNbMPs();

	public Trace[] getTrace();

	public String getTraceAsString();

}
