/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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
import net.gecosi.CommStatus;
import net.gecosi.SiDataFrame;
import net.gecosi.SiHandler;
import net.gecosi.SiListener;


/**
 * @author Simon Denier
 * @since Oct 8, 2009
 *
 */
public class SIReaderHandler extends Control implements Announcer.StageListener, SiListener {
	
	private static final boolean DEBUGMODE = false;
	

	private SiHandler siHandler;

	private SerialPort siPort;
	
	private List<SerialPort> serialPorts;
	
	
	private ECardMode currentEcardMode;
	
	private boolean autoHandlerOn;

	private boolean archiveLookupOn;

	
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
		public boolean equals(Object obj) {
			return obj instanceof SerialPort &&
					port.equals(((SerialPort) obj).port) &&
					friendlyName.equals(((SerialPort) obj).friendlyName);
		}
		@Override
		public int hashCode() {
			return port.hashCode() & friendlyName.hashCode();
		}
		
	}
	
	
	public SIReaderHandler(GecoControl geco) {
		super(SIReaderHandler.class, geco);

		CourseDetector courseDetector = new CourseDetector(geco);
		new ECardRacingMode(geco, courseDetector);
		new ECardTrainingMode(geco, courseDetector);
		new ECardRegisterMode(geco);
		setupHandlers();
		selectECardMode(ECardRacingMode.class);
		
		changePortName();
		geco.announcer().registerStageListener(this);
	}

	public void selectECardMode(Class<? extends ECardMode> modeClass) {
		currentEcardMode = getService(modeClass);
	}
	
	public boolean autoHandlerEnabled() {
		return autoHandlerOn;
	}
	
	public void enableAutoHandler() {
		autoHandlerOn = true;
		getService(ECardRacingMode.class).enableAutoHandler(archiveLookupOn);
		getService(ECardTrainingMode.class).enableAutoHandler(archiveLookupOn);		
	}
	
	public void enableManualHandler() {
		autoHandlerOn = false;
		getService(ECardRacingMode.class).enableManualHandler();
		getService(ECardTrainingMode.class).enableManualHandler();
	}
	
	public boolean archiveLookupEnabled() {
		return archiveLookupOn;
	}
	
	private void toggleArchiveLookup(boolean toggle) {
		archiveLookupOn = toggle;
		getService(ECardRacingMode.class).toggleArchiveLookup(toggle);
		getService(ECardTrainingMode.class).toggleArchiveLookup(toggle);		
	}
	
	public void enableArchiveLookup() {
		toggleArchiveLookup(true);
	}
	
	public void disableArchiveLookup() {
		toggleArchiveLookup(false);
	}
	
	private void setupHandlers() {
		autoHandlerOn = Boolean.parseBoolean(
						stage().getProperties().getProperty(autoHandlerProperty(), "true")); //$NON-NLS-1$
		archiveLookupOn = Boolean.parseBoolean(
						stage().getProperties().getProperty(archiveLookupProperty(), "true")); //$NON-NLS-1$
		if( autoHandlerOn ) {
			enableAutoHandler();
		} else {
			enableManualHandler();
		}
	}
	
	public static String autoHandlerProperty() {
		return "AutoHandler"; //$NON-NLS-1$
	}
	
	public static String archiveLookupProperty() {
		return "ArchiveLookup"; //$NON-NLS-1$
	}
	
	public static String portNameProperty() {
		return "SIPortname"; //$NON-NLS-1$
	}
	
	private void changePortName() {
		List<SerialPort> currentPorts = refreshPorts();
		String port = stage().getProperties().getProperty(portNameProperty());
		if( port != null ) {
			for (SerialPort serial : currentPorts) {
				if( serial.name().equals(port) ){
					setPort(serial);
					return;
				}
			}
		}
		setPort(detectSIPort(currentPorts));
	}
	
	public void changeZeroTime() {
		if( siHandler!=null )
			siHandler.setZeroHour( stage().getZeroHour() );
	}
	
	public List<SerialPort> refreshPorts() {
		serialPorts = null;
		return listPorts();
	}

	public List<SerialPort> listPorts() {
		if( serialPorts==null ){
			@SuppressWarnings("rawtypes")
			Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
			List<String> sPorts = new ArrayList<String>();
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
	private List<SerialPort> createFriendlyPorts(List<String> serialPorts) {
		List<SerialPort> ports = new ArrayList<SerialPort>(serialPorts.size());
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

	private SerialPort detectSIPort(List<SerialPort> currentPorts) {
		String match;
		if( GecoResources.platformIsWindows() ){
			match = "SPORTident"; //$NON-NLS-1$
		} else { // Linux, Mac
			match = "/dev/tty.SLAB_USBtoUART"; //$NON-NLS-1$
		}
		for (SerialPort serial : currentPorts) {
			if( serial.toString().contains(match) ){
				return serial;
			}
		}
		return currentPorts.get(0);
	}
	
	public SerialPort getPort() {
		return siPort;
	}

	public void setPort(SerialPort port) {
		this.siPort = port;
	}

	public void start() {
		siHandler = new SiHandler(this);
		changeZeroTime();
		try {
			siHandler.connect(getPort().name());
		} catch (Exception e) {
			geco().debug(e.getLocalizedMessage());
		}
	}
	
	public void stop() {
		if( siHandler==null )
			return;
		siHandler.stop();
	}
	
	public boolean isOn() {
		return siHandler!=null && siHandler.isAlive();
	}
	
	@Override
	public void handleEcard(SiDataFrame card) {
		currentEcardMode.processECard(card);		
	}

	@Override
	public void notify(CommStatus status) {
		// TODO Auto-generated method stub
		
//		if( status.equals("     Open      ") ){ //$NON-NLS-1$
//		geco().announcer().announceStationStatus("Ready"); //$NON-NLS-1$
//	}
//	if( status.equals("     Connecting") ){ //$NON-NLS-1$
//		nbTry++;
//	}
//	if( nbTry>=2 ) { // catch any tentative to re-connect after a deconnexion
//		siHandler.interrupt(); // one last try, after interruption
//		if( starting ) { // wrong port
//			geco().announcer().announceStationStatus("NotFound"); //$NON-NLS-1$
//		} else { // station was disconnected?
//			geco().announcer().announceStationStatus("Failed"); //$NON-NLS-1$
//		}
//	}
		geco().log(status.name());
	}

	@Override
	public void notify(CommStatus errorStatus, String errorMessage) {
		// TODO Auto-generated method stub
		geco().log(errorMessage);
	}

	@Override
	public void changed(Stage previous, Stage next) {
		changePortName();
		changeZeroTime();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(autoHandlerProperty(), Boolean.toString(autoHandlerOn));
		properties.setProperty(archiveLookupProperty(), Boolean.toString(archiveLookupOn));
		properties.setProperty(portNameProperty(), getPort().name());
	}

	@Override
	public void closing(Stage stage) {
		stop();
	}
	
}
