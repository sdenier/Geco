/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import java.util.Date;

import valmo.geco.model.Punch;

public class PunchImpl implements Punch {
	
	private int code;
	private Date time;
	
	public Punch clone() {
		try {
			Punch clone = (Punch) super.clone();
			clone.setTime((Date) getTime().clone());
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}

	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
}