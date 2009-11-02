/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import java.util.ArrayList;
import java.util.Date;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.common.ResultData;
import org.martin.sireader.server.IPunchObject;
import org.martin.sireader.server.IResultData;
import org.martin.sireader.server.PortMessage;
import org.martin.sireader.server.SIPortHandler;
import org.martin.sireader.server.SIReaderListener;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Oct 8, 2009
 *
 */
public class SIReaderHandler extends Control implements SIReaderListener<PunchObject,PunchRecordData> {

	private Geco geco;
	
	private Announcer announcer;
	
	private SIPortHandler portHandler;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer
	 */
	public SIReaderHandler(Factory factory, Stage stage, Geco geco, Announcer announcer) {
		super(factory, stage, announcer);
		this.geco = geco;
		this.announcer = announcer;
	}

	private void configure() {
		portHandler = new SIPortHandler(new ResultData());
		portHandler.addListener(this);
		portHandler.setPortName("/dev/tty.SLAB_USBtoUART");		// TODO: take parameter from stage properties
		portHandler.setDebugDir(".");
	}

	public void start() {
		configure();
		if (!portHandler.isAlive())
			portHandler.start();
		PortMessage m = new PortMessage(SIPortHandler.START);
		portHandler.sendMessage(m);
	}
	
	public void stop() {
		if( portHandler==null )
			return;
		try {
			portHandler.interrupt();
			portHandler.join();
		} catch (InterruptedException e) {
			// TODO log
			e.printStackTrace();
		}
	}

	@Override
	public void newCardRead(IResultData<PunchObject,PunchRecordData> card) {
		Runner runner = registry().findRunnerByChip(card.getSiIdent());
		if( runner!=null ) {
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			if( runnerData.hasResult() ) {
				// create John Doe
				// launch merger
				// autoselect the target
				geco.openMergeDialog("Existing Data for Runner", "Override data and result for Runner?");
			} else {
				handleData(runnerData, card);	
			}
		} else {
			// TODO: create John Doe
			// launch merger
			geco.openMergeDialog("Unknown Chip", "Record data or merge into existing runner?");
			announcer.announceCardRead("Dangling John Doe!");
		}
		
	}
	
	/**
	 * @param runnerData 
	 * @param card
	 */
	private void handleData(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		updateRaceDataWith(runnerData, card);
		geco.checker().check(runnerData);
		announcer.announceCardRead(runnerData.getRunner().getChipnumber());	
	}

	/**
	 * @param runnerData
	 * @param card
	 */
	private void updateRaceDataWith(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		// TODO handle missinf finish time in the checker??? -> MP/DNF?
		runnerData.setErasetime(new Date(card.getClearTime()));
		runnerData.setControltime(new Date(card.getCheckTime()));		
		runnerData.setStarttime(new Date(card.getStartTime()));
//		data.setStarttime(TimeManager.safeParse(record[3]));
		runnerData.setFinishtime(new Date(card.getFinishTime()));
		handlePunches(runnerData, card.getPunches());
	}


	/**
	 * @param runnerData
	 * @param punches
	 */
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

	@Override
	public void portStatusChanged(String status) {
		
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
