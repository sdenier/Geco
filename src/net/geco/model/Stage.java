/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.Properties;

/**
 * Stage represents a single basic race, where competitors run to get one result. It holds all specific 
 * data for the stage, including the registry, properties of controls and of widgets. 
 * 
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public interface Stage {

	public void setBaseDir(String baseDir);

	public String getBaseDir();

	public String getName();

	public void setName(String name);

	public long getZeroHour();
	
	public void setZeroHour(long zeroHour);

	public Registry registry();

	public void setRegistry(Registry registry);

	public int getAutosaveDelay();	

	public void setAutosaveDelay(int autosaveDelay);
	
	public int getNbAutoBackups();

	public void setNbAutoBackups(int nbBackups);
	
	public String filepath(String filename);
	
	public boolean version12(); // MIGR12


	/**
	 * Return the properties for the current stage, create new ones if necessary.
	 * 
	 * @return
	 */
	public Properties getProperties();

	/**
	 * Update stage attributes with given properties.
	 * 
	 * @param properties
	 */
	public void loadProperties(Properties properties);

	/**
	 * Save stage attributes in given properties, which become the new properties.
	 * 
	 * @param properties
	 */
	public void saveProperties(Properties properties);

}