/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.ArrayList;
import java.util.Date;

import net.geco.basics.GecoRequestHandler;
import net.geco.basics.TimeManager;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.server.IPunchObject;
import org.martin.sireader.server.IResultData;

/**
 * @author Simon Denier
 * @since Mar 11, 2012
 *
 */
public class ECardHandler extends Control {

	private GecoRequestHandler requestHandler;
	
	public ECardHandler(GecoControl geco) {
		super(ECardHandler.class, geco);
	}
	
	public void setRequestHandler(GecoRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	/**
	 * Entry point for the handler.
	 * 
	 * @param card
	 */
	public void handleECard(IResultData<PunchObject,PunchRecordData> card) {
		String cardId = card.getSiIdent();
		Runner runner = registry().findRunnerByEcard(cardId);
		if( runner!=null ) {
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			if( runnerData.hasData() ) {
				handleDuplicate(card, cardId, runner);
			} else {
				handleFinished(runnerData, card);
			}
			// TODO: update runner before check
			if( runner.rentedEcard() ){
				geco().info(Messages.getString("SIReaderHandler.RentedEcardMessage") + cardId, true); //$NON-NLS-1$
				geco().announcer().announceRentedCard(cardId);
			}
		} else {
			handleUnknown(card, cardId);
		}
	}
	
	public void handleFinished(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		updateRaceDataWith(runnerData, card);
		Status oldStatus = runnerData.getResult().getStatus();
		geco().checker().check(runnerData);
		geco().log("READING " + runnerData.infoString()); //$NON-NLS-1$
		if( runnerData.getResult().getNbMPs() > 0 ) {
			geco().announcer().dataInfo(
					runnerData.getResult().formatMpTrace()
					+ " (" + runnerData.getResult().getNbMPs() + " MP)"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		geco().announcer().announceCardRead(runnerData.getRunner().getEcard());
		geco().announcer().announceStatusChange(runnerData, oldStatus);
	}

	public void handleDuplicate(IResultData<PunchObject, PunchRecordData> card, String cardId, Runner runner) {
		geco().log("READING AGAIN " + cardId); //$NON-NLS-1$
		String returnedCard = requestHandler.requestMergeExistingRunner(createUnregisteredData(card), runner);
		if( returnedCard!=null ) {
			geco().announcer().announceCardReadAgain(returnedCard);
		}
	}

	public void handleUnknown(IResultData<PunchObject, PunchRecordData> card, String cardId) {
		geco().log("READING UNKNOWN " + cardId); //$NON-NLS-1$
		String returnedCard = requestHandler.requestMergeUnknownRunner(createUnregisteredData(card), cardId);
		if( returnedCard!=null ) {
			geco().announcer().announceUnknownCardRead(returnedCard);
		}
	}
	
	private RunnerRaceData createUnregisteredData(IResultData<PunchObject,PunchRecordData> card) {
		RunnerRaceData newData = factory().createRunnerRaceData();
		newData.setResult(factory().createRunnerResult());
		updateRaceDataWith(newData, card);
		// do not do any announcement here
		// since the case is handled in the Merge dialog after that and depends on user decision
		return newData;
	}

	public void updateRaceDataWith(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		runnerData.stampReadtime();
		runnerData.setErasetime(safeTime(card.getClearTime()));
		runnerData.setControltime(safeTime(card.getCheckTime()));		
		handleStarttime(runnerData, card);
		handleFinishtime(runnerData, card);
		handlePunches(runnerData, card.getPunches());
	}
	
	public Date safeTime(long siTime) {
		if( siTime>PunchObject.INVALID ) {
			return new Date(siTime);
		} else {
			return TimeManager.NO_TIME;
		}
	}
	
	public void handleStarttime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
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

	public void handleFinishtime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
		Date finishTime = safeTime(card.getFinishTime());
		runnerData.setFinishtime(finishTime);
		if( finishTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING finish time for " + card.getSiIdent()); //$NON-NLS-1$
		}
	}

	private void handlePunches(RunnerRaceData runnerData, ArrayList<PunchObject> punchArray) {
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
