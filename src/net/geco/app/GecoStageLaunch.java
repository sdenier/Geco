/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import net.geco.control.StageBuilder;
import net.geco.framework.IStageLaunch;
import net.geco.model.impl.StageImpl;

/**
 * @author Simon Denier
 * @param <T>
 * @since May 18, 2011
 *
 */
public class GecoStageLaunch implements IStageLaunch {

	public static final String[] FILES = {
		"Classes.csv", //$NON-NLS-1$
		"Clubs.csv", //$NON-NLS-1$
		"Competitors.csv", //$NON-NLS-1$
		"Courses.csv", //$NON-NLS-1$
		"CardData.csv", //$NON-NLS-1$
		"ResultData.csv", //$NON-NLS-1$
		"geco.prop", //$NON-NLS-1$
		"result.css", //$NON-NLS-1$
		"ticket.css", //$NON-NLS-1$
	};
	
	private String stageName;
	private String baseDir;
	private String appBuilderName;
	private AppBuilder appBuilder;
	
	public void loadFromFileSystem(String dir) {
		setStageDir(dir);
		Properties properties = StageBuilder.loadProperties(dir);
		setAppBuilderName(properties.getProperty(StageImpl.appBuilderProperty(),
												 StageImpl.defaultAppBuilderName()));
	}
	
	public AppBuilder getAppBuilder() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if( appBuilder==null ){
			Class<?> appBuilderClass = Class.forName(appBuilderName);
			appBuilder = (AppBuilder) appBuilderClass.newInstance();
		}
		return appBuilder;
	}

	@Override
	public String getStageName() {
		return stageName;
	}

	@Override
	public void setStageName(String name) {
		stageName = name;
	}

	@Override
	public String getStageDir() {
		return baseDir;
	}

	@Override
	public void setStageDir(String path) {
		baseDir = path;
	}

	@Override
	public String getAppBuilderName() {
		return appBuilderName;
	}

	@Override
	public void setAppBuilderName(String name) {
		appBuilderName = name;
	}

	@Override
	public void initDirWithTemplateFiles() {
		new File(baseDir).mkdir();
		new File(baseDir + File.separator + "backups").mkdir(); //$NON-NLS-1$
		createDataFiles(baseDir);
		Properties properties = StageBuilder.loadProperties(baseDir);
		properties.setProperty(StageImpl.nameProperty(), stageName);
		properties.setProperty(StageImpl.appBuilderProperty(), appBuilderName);
		StageBuilder.saveProperties(baseDir, properties);
	}
	
	private void createDataFiles(String baseDir) {
		for (String datafile : FILES) {
			createFile(baseDir, datafile);
		}
	}

	private void createFile(String baseDir, String filename) {
		try {
			URL url = getClass().getResource("/resources/templates/" + filename); //$NON-NLS-1$
			ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
			FileChannel outChannel = new FileOutputStream(new File(baseDir + File.separator + filename)).getChannel();
			outChannel.transferFrom(inChannel, 0, url.openConnection().getContentLength());
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}

}
