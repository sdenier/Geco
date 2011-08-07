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
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Aug 5, 2011
 *
 */
public class FreeOrderChecker extends PunchChecker {

	private int nbMPs;
	private Trace[] trace;

	public FreeOrderChecker(Factory factory) {
		super(factory);
	}

	@Override
	protected Status computeStatus(RunnerRaceData data) {
		Status status = super.computeStatus(data);
		data.getResult().setNbMPs(nbMPs);
		data.getResult().setTrace(trace);
		return status;
	}

	@Override
	public Status checkCodes(int[] codes, Punch[] punches) {
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
		if( nbMPs == 0 ){
			return Status.OK;
		} else {
			return Status.MP;
		}
	}
	
	public int indexOf(int code, int[] codes) {
		for (int i = 0; i < codes.length; i++) {
			if( code == codes[i] ){
				return i;
			}
		}
		return -1;
	}

	public Trace[] getLastTrace() {
		return trace;
	}
	
	public String getLastTraceAsString() {
		return Util.join(trace, ",", new StringBuffer());
	}

}
