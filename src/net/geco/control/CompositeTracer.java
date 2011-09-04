/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.Arrays;

import net.geco.basics.Util;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeTracer extends AbstractTracer {

	private Tracer tracer1;
	private Tracer tracer2;
	private int jointStart2;
	private int jointPunchIndex;

	public CompositeTracer(Factory factory) {
		super(factory);
	}

	@Override
	public void computeTrace(int[] codes, Punch[] punches) {
		int courseIndex = Util.firstIndexOf(jointStart2, codes);
		int[] secondCourse = Arrays.copyOfRange(codes, courseIndex, codes.length);

		if( jointPunchIndex==-1 ){
			tracer2.computeTrace(secondCourse, punches);
			this.nbMPs = tracer2.getNbMPs();
			this.trace = tracer2.getTrace();
			return;
		}
		
		int[] firstCourse = Arrays.copyOfRange(codes, 0, courseIndex);
		Punch[] firstRace = Arrays.copyOfRange(punches, 0, jointPunchIndex);
		Punch[] secondRace = Arrays.copyOfRange(punches, jointPunchIndex, punches.length);
		tracer1.computeTrace(firstCourse, firstRace);
		tracer2.computeTrace(secondCourse, secondRace);
		this.nbMPs = tracer1.getNbMPs() + tracer2.getNbMPs();
		this.trace = mergeTrace(tracer1.getTrace(), tracer2.getTrace());
	}

	protected Trace[] mergeTrace(Trace[] trace1, Trace[] trace2) {
		Trace[] trace = new Trace[trace1.length + trace2.length];
		System.arraycopy(trace1, 0, trace, 0, trace1.length);
		System.arraycopy(trace2, 0, trace, trace1.length, trace2.length);
		return trace;
	}

	public void startWith(Tracer tracer) {
		tracer1 = tracer;
	}

	public void joinRight(int startCode, Tracer tracer) {
		jointStart2 = startCode;
		tracer2 = tracer;
	}

	public void setJointPunchIndex(int jointPunchIndex) {
		this.jointPunchIndex = jointPunchIndex;
	}

}
