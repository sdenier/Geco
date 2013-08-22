/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.Vector;

import net.geco.basics.TimeManager;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;


/**
 * @author Simon Denier
 * @since Aug 22, 2010
 *
 */
public class PoolMerger extends Control {

	/**
	 * @param gecoControl
	 */
	public PoolMerger(GecoControl gecoControl) {
		super(gecoControl);
	}

	/**
	 * @param poolStages
	 */
	public void merge(Vector<Stage> poolStages) {
		for (RunnerRaceData runnerData : registry().getRunnersData()) {
			mergeRunnerData(runnerData, poolStages);
		}
	}

	private void mergeRunnerData(RunnerRaceData runnerData, Vector<Stage> poolStages) {
		long mergedTime = 0;
		Status mergedStatus = Status.OK;
		for (Stage stage : poolStages) {
			RunnerRaceData poolData = stage.registry().findRunnerData(runnerData.getRunner().getEcard());
			mergedTime = mergeTime(mergedTime, poolData.getResult().getResultTime());
			mergedStatus = mergeStatus(mergedStatus, poolData.getResult().getStatus());
		}
		runnerData.getResult().setResultTime(mergedTime);
		runnerData.getResult().setStatus(mergedStatus);
	}

	private long mergeTime(long mergedTime, long racetime) {
		if( racetime==TimeManager.NO_TIME_l ) {
			return TimeManager.NO_TIME_l;
		} else {
			return mergedTime + racetime;
		}
	}

	private Status mergeStatus(Status mergedStatus, Status poolStatus) {
		if( mergedStatus.equals(Status.OK) ) {
			return poolStatus;
		} else {
			return mergedStatus;
		}
	}

}
