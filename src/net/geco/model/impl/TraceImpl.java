/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Date;

import net.geco.model.Punch;
import net.geco.model.Trace;



public class TraceImpl implements Trace {

	private String code;
	private Date time;
	private boolean neutralized;
	
	public TraceImpl(Punch punch) {
		this(Integer.toString(punch.getCode()), punch.getTime());
	}
	
	public TraceImpl(String code, Date time) {
		this.neutralized = false;
		this.code = code;
		this.time = time;
	}
	
	public TraceImpl clone() {
		try {
			TraceImpl clone = (TraceImpl) super.clone();
			clone.time = (Date) time.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCode() {
		return code;
	}
	public String getBasicCode() {
		if( isSubst() ) {
			return code.substring(1, code.indexOf('+'));
		} else {
		if( isMP() || isAdded() ) {
			return code.substring(1);
		} else
			return code;
		}
	}
	public String getAddedCode() {
		if( isAdded() ) {
			return code.substring(1);
		} else
		if( isSubst() ) {
			return code.substring(code.indexOf('+') + 1);
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	public Date getTime() {
		return time;
	}
	
	public String toString() {
		return getCode();
	}

	@Override
	public boolean isOK() {
		return !( isMP() || isAdded() );
	}
	@Override
	public boolean isMP() {
		return code.startsWith("-"); //$NON-NLS-1$
	}
	@Override
	public boolean isAdded() {
		return code.startsWith("+"); //$NON-NLS-1$
	}
	@Override
	public boolean isSubst() {
		// Notice that isSub => isMP
		return code.startsWith("-") && code.contains("+"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean isTruePunch() {
		return ! isMP() || isSubst();
	}

	@Override
	public boolean isNeutralized() {
		return neutralized;
	}

	@Override
	public void setNeutralized(boolean flag) {
		this.neutralized = flag;
	}
}