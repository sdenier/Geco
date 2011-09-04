/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeChecker extends PenaltyChecker {

	private int startCode2;

	public CompositeChecker(Factory factory) {
		super(factory, null);
		tracer = new CompositeTracer(factory);
	}

	public CompositeChecker(GecoControl gecoControl) {
		super(gecoControl, null);
		tracer = new CompositeTracer(gecoControl.factory());
	}
	
	@Override
	public Status computeStatus(RunnerRaceData data) {
		int jointPunchIndex = findJointPunch(startCode2, data.getPunches());
		tracer().setJointPunchIndex( jointPunchIndex );
		Status status = super.computeStatus(data);
		if( jointPunchIndex==-1 ){
			return Status.MP;
		}
		return status;
	}
	
	protected int findJointPunch(int startCode2, Punch[] punches) {
		for (int i = 0; i < punches.length; i++) {
			if( punches[i].getCode()==startCode2 ){
				return i;
			}
		}
		return -1;
	}

	protected CompositeTracer tracer() {
		return (CompositeTracer) tracer;
	}

	public void startWith(Tracer tracer) {
		tracer().startWith(tracer);
	}

	public void joinRight(int startCode, Tracer tracer) {
		this.startCode2 = startCode;
		tracer().joinRight(startCode, tracer);
	}
	
	

}
