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
public class CompositeTracer2 extends AbstractTracer {

	private Tracer tracer1;
	private Tracer tracer2;
	private int jointStart2;

	public CompositeTracer2(Factory factory) {
		super(factory);
	}

	@Override
	public void computeTrace(int[] codes, Punch[] punches) {
		int courseIndex = Util.firstIndexOf(jointStart2, codes);
		int[] firstCourse = Arrays.copyOfRange(codes, 0, courseIndex);
		int[] secondCourse = Arrays.copyOfRange(codes, courseIndex, codes.length);

		int jointPunchIndex = findJointPunchIndex(secondCourse, punches);
		Punch[] firstRace = Arrays.copyOfRange(punches, 0, jointPunchIndex);
		tracer1.computeTrace(firstCourse, firstRace);
		Punch[] secondRace = Arrays.copyOfRange(punches, jointPunchIndex, punches.length);
		tracer2.computeTrace(secondCourse, secondRace);

		this.nbMPs = tracer1.getNbMPs() + tracer2.getNbMPs();
		this.trace = mergeTrace(tracer1.getTrace(), tracer2.getTrace());
	}

	protected int findJointPunchIndex(int[] secondCourse, Punch[] punches) {
		tracer2.computeTrace(secondCourse, punches);
		Trace[] trace2 = tracer2.getTrace();
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

	public void startWith(Tracer tracer) {
		tracer1 = tracer;
	}

	public void joinRight(int startCode, Tracer tracer) {
		jointStart2 = startCode;
		tracer2 = tracer;
	}

}
