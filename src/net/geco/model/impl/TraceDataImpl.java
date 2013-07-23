/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.ArrayList;

import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Jul 20, 2013
 *
 */
public class TraceDataImpl implements TraceData {

	private long runningTime;
	
	private int nbMPs = 0;
	
	private Trace[] trace = new Trace[0];

	public TraceData clone() {
		try {
			TraceData clone = (TraceData) super.clone();
			Trace[] trace = new Trace[getTrace().length];
			for (int i = 0; i < getTrace().length; i++) {
				trace[i] = getTrace()[i].clone();
			}
			clone.setTrace(trace);
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	public String formatRunningTime() {
		return TimeManager.time(runningTime);
	}

	public int getNbMPs() {
		return nbMPs;
	}

	public void setNbMPs(int nbMPs) {
		this.nbMPs = nbMPs;
	}

	public Trace[] getTrace() {
		return trace;
	}

	public void setTrace(Trace[] trace) {
		this.trace = trace;
	}

	public Trace[] getMpTrace() {
		ArrayList<Trace> mpTrace = new ArrayList<Trace>();
		for (Trace t : trace) {
			if( t.isMP() ){	mpTrace.add(t); }
		}
		return mpTrace.toArray(new Trace[0]);
	}

	public Trace[] getClearTrace() {
		ArrayList<Trace> clearTrace = new ArrayList<Trace>(trace.length);
		for (Trace t : trace) {
			if( ! t.isAdded() ) { clearTrace.add(t); }
		}
		return clearTrace.toArray(new Trace[0]);
	}

	public Trace[] retrieveLeg(String legStart, String legEnd) {
		Trace[] clearTrace = getClearTrace();
		for (int i = 0; i < clearTrace.length-1; i++) {
			if( clearTrace[i].getCode().equals(legStart) && clearTrace[i+1].getCode().equals(legEnd) ){
				return new Trace[]{ clearTrace[i], clearTrace[i+1] };
			}
		}
		return null;
	}
	
	public Trace[] getPunchTrace() {
		ArrayList<Trace> punchTrace = new ArrayList<Trace>(trace.length);
		for (Trace t : trace) {
			if( ! t.isMP() || t.isSubst() ) { punchTrace.add(t); }
		}
		return punchTrace.toArray(new Trace[0]);
	}

	protected String formatTrace(Trace[] trace) {
		if( trace.length > 0 )
			return Util.join(trace, ",", new StringBuilder()); //$NON-NLS-1$
		else
			return ""; //$NON-NLS-1$
	}

	public String formatTrace() {
		return formatTrace(trace);
	}

	public String formatMpTrace() {
		return formatTrace(getMpTrace());
	}

	public String formatClearTrace() {
		return formatTrace(getClearTrace());
	}

	public String formatPunchTrace() {
		return formatTrace(getPunchTrace());
	}

}
