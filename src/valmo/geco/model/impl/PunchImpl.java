/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model.impl;

import java.util.Date;

import valmo.geco.model.Punch;

public class PunchImpl implements Punch {
	
	private int code;
	private Date time;

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