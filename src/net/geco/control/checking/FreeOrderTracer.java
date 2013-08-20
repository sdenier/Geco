/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.ArrayList;
import java.util.LinkedList;

import net.geco.basics.TimeManager;
import net.geco.control.BasicControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Aug 8, 2011
 *
 */
public class FreeOrderTracer extends BasicControl implements Tracer {

	public FreeOrderTracer(Factory factory) {
		super(factory);
	}

	@Override
	public TraceData computeTrace(int[] codes, Punch[] punches) {
		int nbMPs = 0;
		ArrayList<Integer> codez = new ArrayList<Integer>(codes.length);
		for (int code : codes) {
			codez.add(code);
		}
		LinkedList<Trace> trace = new LinkedList<Trace>();
		for (Punch punch : punches) {
			if( codez.remove( Integer.valueOf(punch.getCode()) )){
				trace.add(factory().createTrace(punch));
			} else {
				trace.add(factory().createTrace("+" + punch.getCode(), punch.getTime())); //$NON-NLS-1$
			}
		}
		for (Integer missingCode : codez) {
			nbMPs++;
			trace.add(factory().createTrace("-" + missingCode, TimeManager.NO_TIME)); //$NON-NLS-1$
		}
		TraceData traceData = factory().createTraceData();
		traceData.setNbMPs(nbMPs);
		traceData.setTrace(trace.toArray(new Trace[0]));
		return traceData;
	}

}
