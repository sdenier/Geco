/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeChecker2 extends PenaltyChecker {

	public CompositeChecker2(Factory factory) {
		super(factory, null);
		tracer = new CompositeTracer2(factory);
	}

	public CompositeChecker2(GecoControl gecoControl) {
		super(gecoControl, null);
		tracer = new CompositeTracer2(gecoControl.factory());
	}

	protected CompositeTracer2 tracer() {
		return (CompositeTracer2) tracer;
	}

	public void startWith(Tracer tracer) {
		tracer().startWith(tracer);
	}

	public void joinRight(int startCode, Tracer tracer) {
		tracer().joinRight(startCode, tracer);
	}
	
	

}
