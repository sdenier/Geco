/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class PunchChecker extends Control {
	
	
	public PunchChecker(Factory factory) {
		super(factory);
	}

	public void check(RunnerRaceData data) {
		data.setResult(factory().createRunnerResult());

		Status status = computeStatus(data);
		long racetime = computeOfficialRaceTime(data);
		if( racetime==TimeManager.NO_TIME.getTime() ) {
			status = Status.MP;
		}
		data.getResult().setStatus(status);
		data.getResult().setRacetime(racetime);
	}

	public long computeOfficialRaceTime(RunnerRaceData data) {
		return computeRealRaceTime(data);
	}

	public final long computeRealRaceTime(RunnerRaceData data) {
		if( data.getFinishtime().equals(TimeManager.NO_TIME) ) {
			return TimeManager.NO_TIME.getTime();
		}
		if( data.getStarttime().equals(TimeManager.NO_TIME) ) {
			return TimeManager.NO_TIME.getTime();
		}
		return data.getFinishtime().getTime() - data.getStarttime().getTime();
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
	 * 
	 * @param codes
	 * @param punches
	 * @return
	 */
	public Status checkCodes(int[] codes, Punch[] punches) {
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

	/**
	 * @param code
	 * @param punches
	 * @param current 
	 * @return index of code in punches, or -1 if not found.
	 */
	public int indexOf(int code, Punch[] punches, int current) {
		for (int i = current; i < punches.length; i++) {
			if( code == punches[i].getCode() ) {
				return i;
			}
		}
		return -1;
	}

}
