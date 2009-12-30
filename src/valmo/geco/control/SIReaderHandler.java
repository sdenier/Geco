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

	private String portName;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer
	 */
	public SIReaderHandler(Factory factory, Stage stage, Geco geco, Announcer announcer) {
		super(factory, stage, announcer);
		this.geco = geco;
		this.announcer = announcer;
		setNewPortName();
	}

	public static String portNameProperty() {
		return "SIPortname";
	}

	public String defaultPortName() {
		return "/dev/tty.SLAB_USBtoUART";
	}

	private void setNewPortName() {
		String port = stage().getProperties().getProperty(portNameProperty());
		if( port!=null ) {
			setPortName(port);
		} else {
			setPortName(defaultPortName());
		}
	}
	
	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	private void configure() {
		portHandler = new SIPortHandler(new ResultData());
		portHandler.addListener(this);
		portHandler.setPortName(getPortName());
		portHandler.setDebugDir(stage().getBaseDir());
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
				geco.openOverrideDialog("Existing Data for Runner", handleNewData(card), runner);
			} else {
				handleData(runnerData, card);	
			}
		} else {
			geco.openMergeDialog("Unknown Chip", handleNewData(card), card.getSiIdent());
//			announcer.announceCardRead("Dangling John Doe!");
		}
		
	}
	
	private RunnerRaceData handleNewData(IResultData<PunchObject,PunchRecordData> card) {
		RunnerRaceData newData = factory().createRunnerRaceData();
		newData.setResult(factory().createRunnerResult());
		updateRaceDataWith(newData, card);
		return newData;
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
	public void changed(Stage previous, Stage next) {
		super.changed(previous, next);
		setNewPortName();
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
