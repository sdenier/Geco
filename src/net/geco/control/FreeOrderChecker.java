/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 5, 2011
 *
 */
public class FreeOrderChecker extends PunchChecker {

	private FreeOrderTracer tracer;
	
	public FreeOrderChecker(Factory factory) {
		super(factory);
		tracer = new FreeOrderTracer(factory);
	}

	@Override
	protected Status computeStatus(RunnerRaceData data) {
		tracer.computeTrace(data.getCourse().getCodes(), data.getPunches());
		int nbMPs = tracer.getNbMPs();
		data.getResult().setNbMPs(nbMPs);
		data.getResult().setTrace(tracer.getTrace());
		if( nbMPs > 0 ) {
			return Status.MP;
		} else {
			return Status.OK;
		}
	}

}
