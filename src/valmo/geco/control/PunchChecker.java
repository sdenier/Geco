/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
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
		int[] codes = data.getCourse().getCodes();
		Punch[] punches = data.getPunches();
		Status status = checkCodes(codes, punches);
		long racetime = computeRaceTime(data);
		if( racetime==TimeManager.NO_TIME.getTime() ) {
			status = Status.MP;
		}
		RunnerResult result = factory().createRunnerResult();
		result.setStatus(status);
		result.setRacetime(racetime);
		data.setResult(result);
	}

	public void resetRaceTime(RunnerRaceData data) {
		if( data.getResult()==null ) { // possible dead branch, but too complex to assert
			check(data);
		} else {
			data.getResult().setRacetime(computeRaceTime(data));
		}
	}
	
	/**
	 * @param data
	 * @return
	 */
	public long computeRaceTime(RunnerRaceData data) {
		if( data.getFinishtime().equals(TimeManager.NO_TIME) ) {
			return TimeManager.NO_TIME.getTime();
		}
		if( data.getStarttime().equals(TimeManager.NO_TIME) ) {
			return TimeManager.NO_TIME.getTime();
		}
		return data.getFinishtime().getTime() - data.getStarttime().getTime();
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
