/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

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
import valmo.geco.core.TimeManager;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Oct 8, 2009
 *
 */
public class SIReaderHandler extends Control implements SIReaderListener<PunchObject,PunchRecordData> {

	private Geco geco;
	
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
		// do not do any announcement here since the case is handled in the Merge dialog after that and depends on user decision
		return newData;
	}
	
	/**
	 * @param runnerData 
	 * @param card
	 */
	private void handleData(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		updateRaceDataWith(runnerData, card);
		Status oldStatus = runnerData.getResult().getStatus();
		geco.checker().check(runnerData);
		geco.announcer().announceCardRead(runnerData.getRunner().getChipnumber());
		geco.announcer().announceStatusChange(runnerData, oldStatus);
	}

	/**
	 * @param runnerData
	 * @param card
	 */
	private void updateRaceDataWith(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		runnerData.setErasetime(safeTime(card.getClearTime()));
		runnerData.setControltime(safeTime(card.getCheckTime()));		
		runnerData.setStarttime(safeTime(card.getStartTime()));
		runnerData.setFinishtime(safeTime(card.getFinishTime()));
		handlePunches(runnerData, card.getPunches());
	}
	
	private Date safeTime(long siTime) {
		if( siTime>PunchObject.INVALID ) {
			return new Date(siTime);
		} else {
			return TimeManager.NO_TIME;
		}
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
//		stop();
		super.changed(previous, next);
		setNewPortName();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(portNameProperty(), getPortName());
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
