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
public class GecoAppLauncher {

	private static String baseDir;
	private static AppBuilder appBuilder;

	public static void loadStageProperties(String dir) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		baseDir = dir;
		Properties properties = StageBuilder.loadProperties(baseDir);
		String appBuilderClassname = properties.getProperty("AppBuilder");
		if( appBuilderClassname==null ){
			appBuilderClassname = "net.geco.app.OrientShowAppBuilder";
		}
		createAppBuilder(appBuilderClassname);
	}

	private static void createAppBuilder(String appBuilderClassname) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> appBuilderClass = Class.forName(appBuilderClassname);
		appBuilder = (AppBuilder) appBuilderClass.newInstance();
	}

	public static String getStageDir() {
		return baseDir;
	}

	public static AppBuilder getAppBuilder() {
		return appBuilder;
	}

}
