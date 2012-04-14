/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import gnu.io.CommPortIdentifier;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.geco.basics.Announcer;
import net.geco.basics.GecoResources;
import net.geco.basics.WindowsRegistryQuery;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.control.ecardmodes.ECardMode;
import net.geco.control.ecardmodes.ECardRacingMode;
import net.geco.control.ecardmodes.ECardRegisterMode;
import net.geco.control.ecardmodes.ECardTrainingMode;
import net.geco.model.Stage;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.PunchRecordData;
import org.martin.sireader.common.ResultData;
import org.martin.sireader.server.IResultData;
import org.martin.sireader.server.PortMessage;
import org.martin.sireader.server.SIPortHandler;
import org.martin.sireader.server.SIReaderListener;


/**
 * @author Simon Denier
 * @since Oct 8, 2009
 *
 */
public class SIReaderHandler extends Control
	implements Announcer.StageListener, SIReaderListener<PunchObject,PunchRecordData> {
	
	private static final boolean DEBUGMODE = false;
	

	private SIPortHandler portHandler;

	private SerialPort siPort;
	
	private Vector<SerialPort> serialPorts;
	
	private int nbTry;
	
	private boolean starting;

	private CourseDetector courseDetector;
	
	private ECardMode currentEcardMode;

	
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
	
	
	public SIReaderHandler(GecoControl geco) {
		super(SIReaderHandler.class, geco);

		courseDetector = new CourseDetector(geco, getService(RunnerControl.class), registry().autoCourse());
		new ECardRacingMode(geco, courseDetector);
		new ECardTrainingMode(geco, courseDetector);
		new ECardRegisterMode(geco);
		selectECardMode(ECardRacingMode.class);
		
		changePortName();
		geco.announcer().registerStageListener(this);
	}
	
	public void selectECardMode(Class<? extends ECardMode> modeClass) {
		currentEcardMode = getService(modeClass);
	}
	
	public static String portNameProperty() {
		return "SIPortname"; //$NON-NLS-1$
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
	
	public void changeZeroTime() {
		if( portHandler!=null )
			portHandler.setCourseZeroTime( stage().getZeroHour() );
	}
	
	public Vector<SerialPort> listPorts() {
		if( serialPorts==null ){
			@SuppressWarnings("rawtypes")
			Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
			Vector<String> sPorts = new Vector<String>();
			if( DEBUGMODE )
				geco().debug("*** CommPort listing ***"); //$NON-NLS-1$
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
				geco().debug("*** Registry listing ***"); //$NON-NLS-1$
			for (String string : reg) {
				Matcher match = comPattern.matcher(string);
				if( match.find() ){
					String com = match.group(2);
					String fname = com + ": " + match.group(1).trim(); //$NON-NLS-1$
					friendlyNames.put(com, fname);
					if( DEBUGMODE )
						geco().debug(fname);
				}
			}
			if( DEBUGMODE )
				geco().debug("*** Match ***"); //$NON-NLS-1$
			for (String port : serialPorts) {
				String friendlyName = friendlyNames.get(port);
				ports.add(new SerialPort(port, (friendlyName==null) ? port : friendlyName ));
				if( DEBUGMODE )
					geco().debug(port + " -> " + friendlyName); //$NON-NLS-1$
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

	private void configure() {
		portHandler = new SIPortHandler(new ResultData());
		portHandler.addListener(this);
		portHandler.setPortName(getPort().name());
		portHandler.setDebugDir(stage().getBaseDir());
		changeZeroTime();
	}

	public void start() {
		configure();
		nbTry = 0;
		starting = true;
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
	
	public boolean isOn() {
		return portHandler!=null && portHandler.isAlive();
	}
	
	@Override
	public void newCardRead(IResultData<PunchObject,PunchRecordData> card) {
		currentEcardMode.processECard(card);		
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
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
