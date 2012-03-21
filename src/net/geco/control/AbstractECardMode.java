/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.ArrayList;
import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.server.IPunchObject;
import org.martin.sireader.server.IResultData;

/**
 * @author Simon Denier
 * @since Mar 14, 2012
 *
 */
public abstract class AbstractECardMode extends Control implements ECardMode {

	public AbstractECardMode(Class<? extends Control> clazz, GecoControl gecoControl) {
		super(clazz, gecoControl);
	}

	/**
	 * Entry point for the handler.
	 * 
	 * @param card
	 */
	@Override
	public void processECard(IResultData<PunchObject,PunchRecordData> card) {
		String cardId = card.getSiIdent();
		Runner runner = registry().findRunnerByEcard(cardId);
		if( runner!=null ) {
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			if( runnerData.hasData() ) {
				processDuplicate(card, runner);
			} else {
				processFinished(card, runnerData);
			}
			// TODO: update runner before check
			if( runner.rentedEcard() ){
				geco().info(Messages.getString("SIReaderHandler.RentedEcardMessage") + cardId, true); //$NON-NLS-1$
				geco().announcer().announceRentedCard(cardId);
			}
		} else {
			processUnknown(card);
		}
	}

	protected void processFinished(IResultData<PunchObject,PunchRecordData> card, RunnerRaceData data) {
		handleFinished(updateRaceDataWith(data, card));
	}
	
	protected void processDuplicate(IResultData<PunchObject, PunchRecordData> card, Runner runner) {
		handleDuplicate(createUnregisteredData(card), card.getSiIdent(), runner);
	}

	protected void processUnknown(IResultData<PunchObject, PunchRecordData> card) {
		handleUnknown(createUnregisteredData(card), card.getSiIdent());
	}

	public RunnerRaceData updateRaceDataWith(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		runnerData.stampReadtime();
		runnerData.setErasetime(safeTime(card.getClearTime()));
		runnerData.setControltime(safeTime(card.getCheckTime()));		
		processStarttime(runnerData, card);
		processFinishtime(runnerData, card);
		processPunches(runnerData, card.getPunches());
		return runnerData;
	}
	
	public RunnerRaceData createUnregisteredData(IResultData<PunchObject,PunchRecordData> card) {
		RunnerRaceData newData = factory().createRunnerRaceData();
		newData.setResult(factory().createRunnerResult());
		updateRaceDataWith(newData, card);
		return newData;
	}

	public Date safeTime(long siTime) {
		if( siTime>PunchObject.INVALID ) {
			return new Date(siTime);
		} else {
			return TimeManager.NO_TIME;
		}
	}

	public void processStarttime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
		Date startTime = safeTime(card.getStartTime());
		runnerData.setStarttime(startTime); // raw time
		if( startTime.equals(TimeManager.NO_TIME) // no start time on card
				&& runnerData.getRunner()!=null ){
			// retrieve registered start time for next check to be accurate
			startTime = runnerData.getRunner().getRegisteredStarttime();
		}
		if( startTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING start time for " + card.getSiIdent()); //$NON-NLS-1$
		}
	}

	public void processFinishtime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
		Date finishTime = safeTime(card.getFinishTime());
		runnerData.setFinishtime(finishTime);
		if( finishTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING finish time for " + card.getSiIdent()); //$NON-NLS-1$
		}
	}

	private void processPunches(RunnerRaceData runnerData, ArrayList<PunchObject> punchArray) {
		Punch[] punches = new Punch[punchArray.size()];
		for(int i=0; i< punches.length; i++) {
			IPunchObject punchObject = punchArray.get(i);
			punches[i] = factory().createPunch();
			punches[i].setCode(punchObject.getCode());
			punches[i].setTime(new Date(punchObject.getTime()));
		}
		runnerData.setPunches(punches);
	}

}