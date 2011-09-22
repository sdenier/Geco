/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Properties;

import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.Stage;


public class StageImpl implements Stage {
	
	private String name;
	private String baseDir;
	private long zeroHour;
	
	private Registry registry;
	private Properties properties;
	
	private int nbBackups;
	private int autosaveDelay; // in minutes
	
	private boolean version12; // MIGR12
	private String appBuilderName;

	
	private static final int DEFAULT_ZEROHOUR = 32400000;	// 9:00

	
	public StageImpl() {
		initializeDefault();
	}

	protected void initializeDefault() {
		name = Messages.uiGet("StagePanel.StageNameDefault"); //$NON-NLS-1$
		zeroHour = DEFAULT_ZEROHOUR;
		nbBackups = 9;
		autosaveDelay = 2;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	public String getBaseDir() {
		return baseDir;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getZeroHour() {
		return zeroHour;
	}
	public void setZeroHour(long zeroHour) {
		this.zeroHour = zeroHour;
	}
	public Registry registry() {
		return registry;
	}
	public void setRegistry(Registry registry) {
		this.registry = registry;
	}


	public int getAutosaveDelay() {
		return autosaveDelay;
	}	

	public void setAutosaveDelay(int autosaveDelay) {
		this.autosaveDelay = autosaveDelay;
	}
	
	public int getNbAutoBackups() {
		return nbBackups;
	}

	public void setNbAutoBackups(int nbBackups) {
		this.nbBackups = nbBackups;
	}
	
	public String filepath(String filename) {
		return baseDir + "/" + filename; //$NON-NLS-1$
	}
	
	public boolean version12(){
		return version12;
	}

	/**
	 * Return the properties for the current stage, create new ones if necessary.
	 * 
	 * @return
	 */
	public Properties getProperties() {
		if( properties==null ) {
			saveProperties(new Properties());
		}
		return properties;
	}
	private void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * Update stage attributes with given properties.
	 * 
	 * @param properties
	 */
	public void loadProperties(Properties properties) {
		setProperties(properties);
		
		String prop = properties.getProperty(nameProperty());
		if( prop!=null ) {
			setName(prop);
		}
		
		prop = properties.getProperty(zerohourProperty());
		if (prop != null) {
			try {
				setZeroHour(Long.parseLong(prop));
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
		}
		
		prop = properties.getProperty(autosaveDelayProperty());
		if( prop!=null ) {
			try {
				setAutosaveDelay(Integer.parseInt(prop));				
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
		}
		
		prop = properties.getProperty(nbAutoBackupsProperty());
		if( prop!=null ) {
			try {
				setNbAutoBackups(Integer.parseInt(prop));				
			} catch (NumberFormatException e) {
				System.err.println(e);
			}
		}
		
		prop = properties.getProperty(versionProperty());
		if( prop!=null ) {
			version12 = true;
		} else {
			version12 = false;
		}
		
		appBuilderName = properties.getProperty(appBuilderProperty(), defaultAppBuilderName());
	}
	
	/**
	 * Save stage attributes in given properties, which become the new properties.
	 * 
	 * @param properties
	 */
	public void saveProperties(Properties properties) {
		properties.setProperty(nameProperty(), getName());
		properties.setProperty(zerohourProperty(), Long.toString(getZeroHour()));
		properties.setProperty(autosaveDelayProperty(), Integer.toString(getAutosaveDelay()));
		properties.setProperty(nbAutoBackupsProperty(), Integer.toString(getNbAutoBackups()));
		properties.setProperty(versionProperty(), "V1.2"); //$NON-NLS-1$
		properties.setProperty(appBuilderProperty(), appBuilderName);
		setProperties(properties);
	}

	public static String nameProperty() {
		return "name"; //$NON-NLS-1$
	}
	
	public static String zerohourProperty() { // MIGR12
		return "SIZeroTime"; //$NON-NLS-1$
	}

	public static String autosaveDelayProperty() {
		return "AutosaveDelay"; //$NON-NLS-1$
	}

	public static String nbAutoBackupsProperty() {
		return "NbAutoBackups"; //$NON-NLS-1$
	}
	
	public static String versionProperty() {
		return "Version"; //$NON-NLS-1$
	}
	
	public static String appBuilderProperty() {
		return "AppBuilder"; //$NON-NLS-1$
	}

	public static String defaultAppBuilderName() {
		return "net.geco.app.OrientShowAppBuilder"; //$NON-NLS-1$
	}

}
