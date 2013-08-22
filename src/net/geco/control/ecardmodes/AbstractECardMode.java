/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.gecosi.dataframe.SiDataFrame;
import net.gecosi.dataframe.SiPunch;

/**
 * @author Simon Denier
 * @since Mar 14, 2012
 *
 */
public abstract class AbstractECardMode extends Control implements ECardMode {

	protected ECardHandler finishHandler, duplicateHandler, unregisteredHandler;

	public AbstractECardMode(GecoControl gecoControl) {
		super(gecoControl);
	}
	
	public AbstractECardMode(Class<? extends Control> clazz, GecoControl gecoControl) {
		super(clazz, gecoControl);
	}

	/**
	 * Entry point for the handler.
	 * 
	 * @param card
	 */
	@Override
	public void processECard(SiDataFrame card) {
		String cardId = card.getSiNumber();
		Runner runner = registry().findRunnerByEcard(cardId);
		if( runner!=null ) {
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			if( runnerData.hasData() ) {
				processDuplicate(card, runner);
			} else {
				processRegistered(card, runnerData);
			}
			// TODO: update runner before check
			if( runner.rentedEcard() ){
				geco().info(Messages.getString("SIReaderHandler.RentedEcardMessage") + cardId, true); //$NON-NLS-1$
				geco().announcer().announceRentedCard(cardId);
			}
		} else {
			processUnregistered(card);
		}
	}

	protected void processRegistered(SiDataFrame card, RunnerRaceData data) {
		handleRegistered(updateRaceDataWith(data, card));
	}
	
	protected void processDuplicate(SiDataFrame card, Runner runner) {
		handleDuplicate(createUnregisteredData(card), runner);
	}

	protected void processUnregistered(SiDataFrame card) {
		handleUnregistered(createUnregisteredData(card), card.getSiNumber());
	}

	public RunnerRaceData updateRaceDataWith(RunnerRaceData runnerData, SiDataFrame card) {
		runnerData.stampReadtime();
		runnerData.setControltime(safeTime(card.getCheckTime()));		
		processStarttime(runnerData, card);
		processFinishtime(runnerData, card);
		processPunches(runnerData, card.getPunches());
		return runnerData;
	}
	
	public RunnerRaceData createUnregisteredData(SiDataFrame card) {
		RunnerRaceData newData = factory().createRunnerRaceData();
		newData.setTraceData(factory().createTraceData());
		newData.setResult(factory().createRunnerResult());
		updateRaceDataWith(newData, card);
		return newData;
	}

	public Date safeTime(long siTime) {
		return ( siTime == SiDataFrame.NO_TIME ) ? TimeManager.NO_TIME : new Date(siTime);
	}

	public void processStarttime(RunnerRaceData runnerData, SiDataFrame card) {
		Date startTime = safeTime(card.getStartTime());
		runnerData.setStarttime(startTime); // raw time
		if( startTime.equals(TimeManager.NO_TIME) // no start time on card
				&& runnerData.getRunner()!=null ){
			// retrieve registered start time for next check to be accurate
			startTime = runnerData.getRunner().getRegisteredStarttime();
		}
		if( startTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING start time for " + card.getSiNumber()); //$NON-NLS-1$
		}
	}

	public void processFinishtime(RunnerRaceData runnerData, SiDataFrame card) {
		Date finishTime = safeTime(card.getFinishTime());
		runnerData.setFinishtime(finishTime);
		if( finishTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING finish time for " + card.getSiNumber()); //$NON-NLS-1$
		}
	}

	private void processPunches(RunnerRaceData runnerData, SiPunch[] punchArray) {
		Punch[] punches = new Punch[punchArray.length];
		for(int i=0; i< punches.length; i++) {
			punches[i] = factory().createPunch();
			punches[i].setCode(punchArray[i].code());
			punches[i].setTime(new Date(punchArray[i].timestamp()));
		}
		runnerData.setPunches(punches);
	}

}