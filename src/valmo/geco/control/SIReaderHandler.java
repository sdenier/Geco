/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.common.ResultData;
import org.martin.sireader.server.IPunchObject;
import org.martin.sireader.server.IResultData;
import org.martin.sireader.server.PortMessage;
import org.martin.sireader.server.SIPortHandler;
import org.martin.sireader.server.SIReaderListener;

import valmo.geco.basics.Announcer;
import valmo.geco.basics.GecoRequestHandler;
import valmo.geco.basics.GecoResources;
import valmo.geco.basics.TimeManager;
import valmo.geco.basics.WindowsRegistryQuery;
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

	// 9:00
	private static final int DEFAULT_ZEROTIME = 32400000;
	
	private static final boolean DEBUGMODE = false;
	

	private GecoRequestHandler requestHandler;
	
	private SIPortHandler portHandler;

	private SerialPort siPort;
	
	private Vector<SerialPort> serialPorts;
	
	private long zeroTime = DEFAULT_ZEROTIME;
	
	private int nbTry;
	
	private boolean starting;

	
	public static class SerialPort {
		private String port;
		private String friendlyName;
		public SerialPort(String port, String friendlyName){
			this.port = port;
			this.friendlyName = friendlyName;
		}
		public String name(){
			return port;
		}
		public String toString(){
			return friendlyName;
		}
	}
	
	
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
	
	private void changePortName() {
		serialPorts = null; // reset listPorts
		String port = stage().getProperties().getProperty(portNameProperty());
		if( port!=null ) {
			for (SerialPort serial : listPorts()) {
				if( serial.name().equals(port) ){
					setPort(serial);
					return;
				}
			}
		}
		setPort(detectSIPort());
	}
	
	public static long readZeroTime(Stage stage) {
		try {
			return Long.parseLong(stage.getProperties().getProperty(zerotimeProperty()));
		} catch (NumberFormatException e) {
			return DEFAULT_ZEROTIME;
		}		
	}
	
	private void changeZeroTime() {
		setNewZeroTime( readZeroTime(stage()) );			
	}
	
	public void setNewZeroTime(long newZerotime) {
		setZeroTime( newZerotime );			
		if( portHandler!=null )
			portHandler.setCourseZeroTime(getZeroTime());
	}
	
	public Vector<SerialPort> listPorts() {
		if( serialPorts==null ){
			@SuppressWarnings("rawtypes")
			Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
			Vector<String> sPorts = new Vector<String>();
			if( DEBUGMODE )
				geco().debug("*** CommPort listing ***");
			while( portIdentifiers.hasMoreElements() ){
				CommPortIdentifier port = (CommPortIdentifier) portIdentifiers.nextElement();
				if( port.getPortType()==CommPortIdentifier.PORT_SERIAL ){
					if( DEBUGMODE )
						geco().debug(port.getName());
					sPorts.add(port.getName());
				}
			}
			serialPorts = createFriendlyPorts(sPorts);			
		}
		return serialPorts;
	}
	private Vector<SerialPort> createFriendlyPorts(Vector<String> serialPorts) {
		Vector<SerialPort> ports = new Vector<SerialPort>(serialPorts.size());
		ports.add(new SerialPort("", "")); // empty port //$NON-NLS-1$ //$NON-NLS-2$
		if( GecoResources.platformIsWindows() ){
			// "HKLM\\System\\CurrentControlSet\\Enum\\USB\\Vid_10c4&Pid_800a\\78624 /v FriendlyName";
			String[] reg =
				WindowsRegistryQuery.listRegistryEntries("HKLM\\System\\CurrentControlSet\\Enum").split("\n"); //$NON-NLS-1$ //$NON-NLS-2$
			HashMap<String,String> friendlyNames = new HashMap<String,String>();
			Pattern comPattern = Pattern.compile("FriendlyName.+REG_SZ(.+)\\((COM\\d+)\\)"); //$NON-NLS-1$
			if( DEBUGMODE )
				geco().debug("*** Registry listing ***");
			for (String string : reg) {
				Matcher match = comPattern.matcher(string);
				if( match.find() ){
					String com = match.group(2);
					String fname = com + ": " + match.group(1).trim(); //$NON-NLS-1$
					friendlyNames.put(com, fname);
					if( DEBUGMODE )
						geco().debug(fname);
				}
				// The following did not always match well?
//				if( string.contains("FriendlyName") && string.contains("COM") ){ //$NON-NLS-1$ //$NON-NLS-2$
//					int s = string.indexOf("COM"); //$NON-NLS-1$
//					int e = string.indexOf(')', s);
//					if( e>=0 ){
//						String com = string.substring(s, e); // expect (COMx) format
//						String fname = com + ": " //$NON-NLS-1$
//										+ string.substring(string.lastIndexOf("\t") + 1, s - 1).trim(); //$NON-NLS-1$
//						friendlyNames.put(com, fname);						
//					} // else we match something which is not like COMx)
//				}
			}
			if( DEBUGMODE )
				geco().debug("*** Match ***");
			for (String port : serialPorts) {
				String friendlyName = friendlyNames.get(port);
				ports.add(new SerialPort(port, (friendlyName==null) ? port : friendlyName ));
				if( DEBUGMODE )
					geco().debug(port + " -> " + friendlyName);
			}
		} else {
			for (String port : serialPorts) {
				ports.add(new SerialPort(port, port));
			}
		}
		return ports;
	}

	private SerialPort detectSIPort() {
		String match;
		if( GecoResources.platformIsWindows() ){
			match = "SPORTident"; //$NON-NLS-1$
		} else { // Linux, Mac
			match = "/dev/tty.SLAB_USBtoUART"; //$NON-NLS-1$
		}
		for (SerialPort serial : listPorts()) {
			if( serial.toString().contains(match) ){
				return serial;
			}
		}
		return serialPorts.firstElement();
	}
	
	public SerialPort getPort() {
		return siPort;
	}

	public void setPort(SerialPort port) {
		this.siPort = port;
	}

	public long getZeroTime() {
		return zeroTime;
	}

	private void setZeroTime(long zeroTime) {
		this.zeroTime = zeroTime;
	}
	
	private void configure() {
		portHandler = new SIPortHandler(new ResultData());
		portHandler.addListener(this);
		portHandler.setPortName(getPort().name());
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
				if( runner.rentedEcard() ){
					geco().announcer().announceRentedCard(card.getSiIdent());
				}
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
	private void handleStarttime(RunnerRaceData runnerData, IResultData<PunchObject, PunchRecordData> card) {
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
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(portNameProperty(), getPort().name());
		properties.setProperty(zerotimeProperty(), Long.toString(getZeroTime()));
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
