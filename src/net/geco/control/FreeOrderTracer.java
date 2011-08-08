/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.Date;
import java.util.LinkedList;

import net.geco.basics.Util;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Aug 8, 2011
 *
 */
public class FreeOrderTracer extends BasicControl implements Tracer {

	private int nbMPs;
	private Trace[] trace;

	public FreeOrderTracer(Factory factory) {
		super(factory);
	}

	@Override
	public void computeTrace(int[] codes, Punch[] punches) {
		nbMPs = 0;
		LinkedList<Integer> codez = new LinkedList<Integer>();
		for (int code : codes) {
			codez.add(code);
		}
		LinkedList<Trace> trace = new LinkedList<Trace>();
		for (Punch punch : punches) {
			if( codez.remove( Integer.valueOf(punch.getCode()) )){
				trace.add(factory().createTrace(punch));
			} else {
				trace.add(factory().createTrace("+" + punch.getCode(), punch.getTime()));
			}
		}
		for (Integer missingCode : codez) {
			nbMPs++;
			trace.add(factory().createTrace("-" + missingCode, new Date(0)));
		}
		this.trace = trace.toArray(new Trace[0]);
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
