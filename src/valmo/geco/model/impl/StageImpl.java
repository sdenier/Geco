/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import java.util.Date;
import java.util.Properties;

import valmo.geco.model.Registry;
import valmo.geco.model.Stage;

public class StageImpl implements Stage {
	
	private String name;
	private String baseDir;
	private Date date;
	
	private Registry registry;
	private Properties properties;
	
	private int nbBackups;
	private int autosaveDelay; // in minutes

	
	public StageImpl() {
		initializeDefault();
	}

	protected void initializeDefault() {
		name = ""; //$NON-NLS-1$
		nbBackups = 9;
		autosaveDelay = 2;
	}
	
	public void initialize(String baseDir) {
		this.baseDir = baseDir;
	}

	public void close() {}
	
	public String getBaseDir() {
		return baseDir;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
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

		prop = properties.getProperty(autosaveDelayProperty());
		if( prop!=null ) {
			try {
				setAutosaveDelay(new Integer(prop));				
			} catch (NumberFormatException e) {
				setAutosaveDelay( 2 );
				System.err.println(e);
			}
		}
		
		prop = properties.getProperty(nbAutoBackupsProperty());
		if( prop!=null ) {
			try {
				setNbAutoBackups(new Integer(prop));				
			} catch (NumberFormatException e) {
				setNbAutoBackups( 9 );
				System.err.println(e);
			}
		}	
	}
	
	
	/**
	 * Save stage attributes in given properties, which become the new properties.
	 * 
	 * @param properties
	 */
	public void saveProperties(Properties properties) {
		properties.setProperty(nameProperty(), getName());
		properties.setProperty(autosaveDelayProperty(), new Integer(getAutosaveDelay()).toString());
		properties.setProperty(nbAutoBackupsProperty(), new Integer(getNbAutoBackups()).toString());
		setProperties(properties);
	}

	public static String nameProperty() {
		return "name"; //$NON-NLS-1$
	}

	public static String autosaveDelayProperty() {
		return "AutosaveDelay"; //$NON-NLS-1$
	}

	public static String nbAutoBackupsProperty() {
		return "NbAutoBackups"; //$NON-NLS-1$
	}

}
