/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.basics.TimeManager;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class PunchChecker extends BasicControl {
	
	
	public PunchChecker(Factory factory) {
		super(factory);
	}

	public void check(RunnerRaceData data) {
		data.setResult(factory().createRunnerResult());

		Status status = computeStatus(data);
		long racetime = computeOfficialRaceTime(data);
		if( racetime==TimeManager.NO_TIME_l ) {
			status = Status.MP;
		}
		data.getResult().setStatus(status);
		data.getResult().setRacetime(racetime);
	}

	public long computeOfficialRaceTime(RunnerRaceData data) {
		return data.realRaceTime();
	}

	public void resetRaceTime(RunnerRaceData data) {
		if( data.getResult()==null ) { // possible dead branch
			check(data);
		} else {
			data.getResult().setRacetime(computeOfficialRaceTime(data));
		}
	}

	protected Status computeStatus(RunnerRaceData data) {
		return checkCodes(data.getCourse().getCodes(), data.getPunches());
	}

	/**
	 * Naive and simple algorithm, reliably judge ok/mp race but can not provide a right account of mps.
	 * For legacy.
	 */
	private Status checkCodes(int[] codes, Punch[] punches) {
		int current = 0;
		for (int code : codes) {
			int index = indexOf(code, punches, current);
			if( index==-1 ) {
				return Status.MP;
			}
			current = index + 1;
		}
		return Status.OK;
	}

	private int indexOf(int code, Punch[] punches, int current) {
		for (int i = current; i < punches.length; i++) {
			if( code == punches[i].getCode() ) {
				return i;
			}
		}
		return -1;
	}

}
