/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.Arrays;

import net.geco.control.BasicControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeTracer extends BasicControl implements Tracer {

	private MultiCourse multiCourse;

	public CompositeTracer(Factory factory) {
		super(factory);
	}

	@Override
	public TraceData computeTrace(int[] codes, Punch[] punches) {
		Tracer tracer1 = multiCourse.firstSection().tracer;
		int[] firstCourse = multiCourse.firstSection().codes;
		Tracer tracer2 = multiCourse.secondSection().tracer;
		int[] secondCourse = multiCourse.secondSection().codes;
		
		int jointPunchIndex = findJointPunchIndex(secondCourse, punches);
		Punch[] firstRace = Arrays.copyOfRange(punches, 0, jointPunchIndex);
		TraceData trace1 = tracer1.computeTrace(firstCourse, firstRace);
		Punch[] secondRace = Arrays.copyOfRange(punches, jointPunchIndex, punches.length);
		TraceData trace2 = tracer2.computeTrace(secondCourse, secondRace);

		TraceData traceData = factory().createTraceData();
		traceData.setNbMPs(trace1.getNbMPs() + trace2.getNbMPs());
		traceData.setTrace(mergeTrace(trace1.getTrace(), trace2.getTrace()));
		return traceData;
	}

	protected int findJointPunchIndex(int[] secondCourse, Punch[] punches) {
		Tracer tracer2 = multiCourse.secondSection().tracer;
		TraceData traceData = tracer2.computeTrace(secondCourse, punches);
		Trace[] trace2 = traceData.getTrace();
		int i = 0;
		while( i < trace2.length && !trace2[i].isOK() ){
			i++;
		}
		if( i==trace2.length ){
			return punches.length;
		}
		int indexCode = Integer.parseInt( trace2[i].getCode() );
		for (int j = 0; j < punches.length; j++) {
			if( punches[j].getCode() == indexCode ){
				return j;
			}
		}
		return -1;
	}

	protected Trace[] mergeTrace(Trace[] trace1, Trace[] trace2) {
		Trace[] trace = new Trace[trace1.length + trace2.length];
		System.arraycopy(trace1, 0, trace, 0, trace1.length);
		System.arraycopy(trace2, 0, trace, trace1.length, trace2.length);
		return trace;
	}

	public void setMultiCourse(MultiCourse multiCourse) {
		this.multiCourse = multiCourse;
	}

}
