/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import net.geco.control.BasicControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Trace;
import net.geco.model.TraceData;


/**
 * @author Simon Denier
 * @since Aug 8, 2013
 *
 */
public class GreedyLooseTracer extends BasicControl implements Tracer {

	public GreedyLooseTracer(Factory factory) {
		super(factory);
	}

	@Override
	public TraceData computeTrace(int[] codes, Punch[] punches) {
		Trace[] trace = new Trace[punches.length];
		for (int i = 0; i < punches.length; i++) {
			Punch punch = punches[i];
			trace[i] = contains(codes, punch.getCode()) ?
						factory().createTrace(punch) :
						factory().createTrace("+" + punch.getCode(), punch.getTime());
		}
		TraceData traceData = factory().createTraceData();
		traceData.setTrace(trace);
		return traceData;
	}

	private boolean contains(int[] codes, int code) {
		for (int i = 0; i < codes.length; i++) {
			if( code == codes[i] ){
				return true;
			}
		}
		return false;
	}
	
}
