/**
 * Copyright (c) 2008 Simon Denier
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

	
	public StageImpl() {
		name = "";
	}
	
	public void initialize(String baseDir) {
		this.baseDir = baseDir;
	}

	public void close() {
		// TODO: ask for saving
	}
	
//	public void save() {
//		saveProperties();
//	}

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
		setName(properties.getProperty("name"));
	}
	/**
	 * Save stage attributes in given properties, which become the new properties.
	 * 
	 * @param properties
	 */
	public void saveProperties(Properties properties) {
		properties.setProperty("name", getName());
		setProperties(properties);
	}

}
