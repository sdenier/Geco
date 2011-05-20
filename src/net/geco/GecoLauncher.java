/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco;

import java.util.Properties;

import net.geco.app.AppBuilder;
import net.geco.control.StageBuilder;

/**
 * @author Simon Denier
 * @param <T>
 * @since May 18, 2011
 *
 */
public class GecoLauncher<T extends AppBuilder> {

	private String baseDir;
	private T appBuilder;

	/**
	 * @param string
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void loadStageProperties(String baseDir) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.baseDir = baseDir;
		Properties properties = StageBuilder.loadProperties(baseDir);
		String appBuilderClassname = properties.getProperty("AppBuilder");
		if( appBuilderClassname==null ){
			appBuilderClassname = "net.geco.app.OrientShowAppBuilder";
		}
		createAppBuilder(appBuilderClassname);
	}

	@SuppressWarnings("unchecked")
	private void createAppBuilder(String appBuilderClassname) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> appBuilderClass = Class.forName(appBuilderClassname);
		appBuilder = (T) appBuilderClass.newInstance();
	}

	/**
	 * @return
	 */
	public String getStageDir() {
		return this.baseDir;
	}

	/**
	 * @return
	 */
	public T getAppBuilder() {
		return appBuilder;
	}

}
