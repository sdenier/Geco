/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.common.ResultData;
import org.martin.sireader.server.IPunchObject;
import org.martin.sireader.server.IResultData;
import org.martin.sireader.server.PortMessage;
import org.martin.sireader.server.SIPortHandler;
import org.martin.sireader.server.SIReaderListener;

import valmo.geco.Geco;
import valmo.geco.core.Announcer;
import valmo.geco.core.GecoRequestHandler;
import valmo.geco.core.TimeManager;
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
public class SIReaderHandler extends Control
	implements Announcer.StageListener, SIReaderListener<PunchObject,PunchRecordData> {

	private GecoRequestHandler requestHandler;
	
	private SIPortHandler portHandler;

	private String portName;
	
	private long zeroTime = 32400000; // 9:00
	
	private int nbTry;
	
	private boolean starting;

	private boolean useZeroHourAsDefaultStarttime;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer
	 */
	public SIReaderHandler(GecoControl geco, GecoRequestHandler requestHandler) {
		super(SIReaderHandler.class, geco);
		this.requestHandler = requestHandler;
		changePortName();
		changeZeroTime();
		changeZeroTimeDefaultStart();
		geco.announcer().registerStageListener(this);
	}
	
	public void setRequestHandler(GecoRequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public static String portNameProperty() {
		return "SIPortname"; //$NON-NLS-1$
	}
	public static String zerotimeProperty() {
		return "SIZeroTime"; //$NON-NLS-1$
	}
	public static String zerotimeDefaultStart() {
		return "SIZeroTimeDefaultStart"; //$NON-NLS-1$
	}
	
	private void changePortName() {
		String port = stage().getProperties().getProperty(portNameProperty());
		if( port!=null ) {
			setPortName(port);
		} else {
			setPortName(detectSIPort());
		}
	}
	
	private void changeZeroTime() {
		try {
			setNewZeroTime( Long.parseLong(stage().getProperties().getProperty(zerotimeProperty())) );			
		} catch (NumberFormatException e) {
			setNewZeroTime(32400000);
		}
	}
	
	private void changeZeroTimeDefaultStart() {
		useZeroHourAsDefaultStarttime =
				Boolean.parseBoolean( stage().getProperties().getProperty(zerotimeDefaultStart()) );
	}
	
	public void setNewZeroTime(long newZerotime) {
		setZeroTime( newZerotime );			
		if( portHandler!=null )
			portHandler.setCourseZeroTime(getZeroTime());
	}
	
	public Vector<String> listPorts() {
		@SuppressWarnings("rawtypes")
		Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		Vector<String> serialPorts = new Vector<String>();
		while( portIdentifiers.hasMoreElements() ){
			CommPortIdentifier port = (CommPortIdentifier) portIdentifiers.nextElement();
			if( port.getPortType()==CommPortIdentifier.PORT_SERIAL ){
				serialPorts.add(port.getName());
			}
		}
		return serialPorts;
	}
	
	public String detectSIPort() {
		String match;
		if( Geco.platformIsMacOs() ){
			match = "/dev/tty.SLAB_USBtoUART"; // TODO: Linux ? 
		} else {
			match = "SPORTident";
		}
		Vector<String> ports = listPorts();
		for (String portName : ports) {
			if( portName.contains(match) ){
				return portName;
			}
		}
		return ports.firstElement();
	}
	
	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public long getZeroTime() {
		return zeroTime;
	}

	public void setZeroTime(long zeroTime) {
		this.zeroTime = zeroTime;
	}
	
	public boolean useZeroHourAsDefaultStarttime() {
		return useZeroHourAsDefaultStarttime;
	}
	
	public void setZeroHourAsDefaultStartime(boolean flag) {
		useZeroHourAsDefaultStarttime = flag;
	}

	private void configure() {
		portHandler = new SIPortHandler(new ResultData());
		portHandler.addListener(this);
		portHandler.setPortName(getPortName());
		portHandler.setDebugDir(stage().getBaseDir());
		portHandler.setCourseZeroTime(getZeroTime());
	}

	public void start() {
		configure();
		nbTry = 0;
		starting = true;
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
			if( runnerData.hasData() ) {
				geco().log("READING AGAIN " + card.getSiIdent()); //$NON-NLS-1$
				String returnedCard = requestHandler.requestMergeExistingRunner(handleNewData(card), runner);
				if( returnedCard!=null ) {
					geco().announcer().announceCardReadAgain(returnedCard);
				}
			} else {
				handleData(runnerData, card);	
			}
		} else {
			geco().log("READING UNKNOWN " + card.getSiIdent()); //$NON-NLS-1$
			String returnedCard = requestHandler.requestMergeUnknownRunner(handleNewData(card), card.getSiIdent());
			if( returnedCard!=null ) {
				geco().announcer().announceUnknownCardRead(returnedCard);
			}
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
		geco().checker().check(runnerData);
		geco().log("READING " + runnerData.infoString()); //$NON-NLS-1$
		if( runnerData.getResult().is(Status.MP) ) {
			geco().announcer().dataInfo(runnerData.getResult().formatMpTrace() + " (" + runnerData.getResult().getNbMPs() + " MP)"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		geco().announcer().announceCardRead(runnerData.getRunner().getChipnumber());
		geco().announcer().announceStatusChange(runnerData, oldStatus);
	}

	/**
	 * @param runnerData
	 * @param card
	 */
	private void updateRaceDataWith(RunnerRaceData runnerData, IResultData<PunchObject,PunchRecordData> card) {
		runnerData.stampReadtime();
		runnerData.setErasetime(safeTime(card.getClearTime()));
		runnerData.setControltime(safeTime(card.getCheckTime()));		
		handleStarttime(runnerData, card);
		handleFinishtime(runnerData, card);
		handlePunches(runnerData, card.getPunches());
	}
	private Date safeTime(long siTime) {
		if( siTime>PunchObject.INVALID ) {
			return new Date(siTime);
		} else {
			return TimeManager.NO_TIME;
		}
	}
	/*
	 * Priority: start time on ecard > pre-registered start time > zero hour if enabled or NO_TIME
	 */
	private void handleStarttime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
		Date startTime = safeTime(card.getStartTime());
		if( startTime.equals(TimeManager.NO_TIME) ) { // no start time on card
			if( runnerData.getStarttime().equals(TimeManager.NO_TIME) ) { // no pre-registered start time
				if( useZeroHourAsDefaultStarttime() ) {
					// In practice, can be used as start time for a unique mass start
					startTime = new Date(getZeroTime()); // TODO: use today midnight 
				} // else keep NO_TIME as start time
			} else {
				startTime = runnerData.getStarttime(); // keep pre-registered start time
			}
		}
		runnerData.setStarttime(startTime);
		if( startTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING start time for " + card.getSiIdent()); //$NON-NLS-1$
		}
	}
	private void handleFinishtime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
		Date finishTime = safeTime(card.getFinishTime());
		runnerData.setFinishtime(finishTime);
		if( finishTime.equals(TimeManager.NO_TIME) ) {
			geco().log("MISSING finish time for " + card.getSiIdent()); //$NON-NLS-1$
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
		if( status.equals("     Open      ") && starting ){ //$NON-NLS-1$
			geco().announcer().announceStationStatus("Ready"); //$NON-NLS-1$
			starting = false;
		}
		if( status.equals("     Connecting") ){ //$NON-NLS-1$
			nbTry++;
		}
		if( nbTry>=2 ) { // catch any tentative to re-connect after a deconnexion
			portHandler.interrupt(); // one last try, after interruption
			if( starting ) { // wrong port
				geco().announcer().announceStationStatus("NotFound"); //$NON-NLS-1$
			} else { // station was disconnected?
				geco().announcer().announceStationStatus("Failed"); //$NON-NLS-1$
			}
		}
	}
	
	
	@Override
	public void changed(Stage previous, Stage next) {
//		stop();
		changePortName();
		changeZeroTime();
		changeZeroTimeDefaultStart();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(portNameProperty(), getPortName());
		properties.setProperty(zerotimeProperty(), Long.toString(getZeroTime()));
		properties.setProperty(zerotimeDefaultStart(), Boolean.toString(useZeroHourAsDefaultStarttime()));
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
