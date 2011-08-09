/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.basics.Util;
import net.geco.model.Factory;
import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Aug 9, 2011
 *
 */
public abstract class AbstractTracer extends BasicControl implements Tracer {

	protected int nbMPs;
	protected Trace[] trace;

	public AbstractTracer(Factory factory) {
		super(factory);
	}

	@Override
	public int getNbMPs() {
		return nbMPs;
	}

	@Override
	public Trace[] getTrace() {
		return trace;
	}

	@Override
	public String getTraceAsString() {
		return Util.join(getTrace(), ",", new StringBuilder());
	}

}