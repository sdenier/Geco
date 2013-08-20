/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Date;
import java.util.TimeZone;

import net.geco.basics.TimeManager;
import net.geco.model.ECardData;
import net.geco.model.Punch;

/**
 * @author  Simon Denier
 * @since  Jul 20, 2013
 */
public class ECardDataImpl implements ECardData {
	
	private Date starttime = TimeManager.NO_TIME;

	private Date finishtime = TimeManager.NO_TIME;

	private Date controltime = TimeManager.NO_TIME;

	private Date readtime = TimeManager.NO_TIME;

	private Punch[] punches = new PunchImpl[0];

	public ECardData clone() {
		try {
			ECardData clone = (ECardData) super.clone();
			clone.setStartTime((Date) getStartTime().clone());
			clone.setFinishTime((Date) getFinishTime().clone());
			clone.setCheckTime((Date) getCheckTime().clone());
			clone.setReadTime((Date) getReadTime().clone());
			Punch[] punches = new Punch[getPunches().length];
			for (int i = 0; i < getPunches().length; i++) {
				punches[i] = getPunches()[i].clone();
			}
			clone.setPunches(punches);
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Date getStartTime() {
		return starttime;
	}

	public void setStartTime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getFinishTime() {
		return finishtime;
	}

	public void setFinishTime(Date finishtime) {
		this.finishtime = finishtime;
	}

	public Date getCheckTime() {
		return controltime;
	}

	public void setCheckTime(Date controltime) {
		this.controltime = controltime;
	}

	public Date getReadTime() {
		return readtime;
	}

	public void setReadTime(Date readtime) {
		this.readtime = readtime;
	}

	public Date stampReadTime() {
		// Use TimeZone to set the time with the right offset
		long stamp = System.currentTimeMillis();
		setReadTime(new Date(stamp + TimeZone.getDefault().getOffset(stamp)));
		return getReadTime();
	}

	public Punch[] getPunches() {
		return punches;
	}

	public void setPunches(Punch[] punches) {
		this.punches = punches;
	}

}