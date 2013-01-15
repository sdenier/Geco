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
import net.geco.model.Messages;
import net.geco.model.impl.StageImpl;

/**
 * @author Simon Denier
 * @param <T>
 * @since May 18, 2011
 *
 */
public class GecoStageLaunch implements IStageLaunch {

	public static final String[] TEMPLATE_FILES = {
		"geco.prop", //$NON-NLS-1$
		"store.json", //$NON-NLS-1$
		"result.css", //$NON-NLS-1$
		"ticket.css", //$NON-NLS-1$
	};
	
	private String stageName;
	private String stageDir;
	private String appBuilderName;
	private AppBuilder appBuilder;
	
	public GecoStageLaunch() {
		setStageName(Messages.getString("GecoStageLaunch.DefaultStageName")); //$NON-NLS-1$
		setStageDir(System.getProperty("user.dir")); //$NON-NLS-1$
		setAppBuilderName("net.geco.app.ClassicAppBuilder"); //$NON-NLS-1$
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof IStageLaunch && ((IStageLaunch) obj).getStageDir().equals(stageDir);
	}

	@Override
	public IStageLaunch loadFromFileSystem(String dir) {
		setStageDir(dir);
		Properties properties = StageBuilder.loadProperties(dir);
		setStageName(properties.getProperty(StageImpl.nameProperty(), Messages.getString("GecoStageLaunch.DefaultStageName"))); //$NON-NLS-1$
		setAppBuilderName(properties.getProperty(StageImpl.appBuilderProperty(),
												 StageImpl.defaultAppBuilderName()));
		return this;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Object clone = super.clone();
		((IStageLaunch) clone).copyFrom(this);
		return clone;
	}

	@Override
	public void copyFrom(IStageLaunch stageLaunch) {
		this.stageName = stageLaunch.getStageName();
		this.stageDir = stageLaunch.getStageDir();
		this.appBuilderName = stageLaunch.getAppBuilderName();
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
	public String toString() {
		return getStageName();
	}

	@Override
	public String getStageDir() {
		return stageDir;
	}

	@Override
	public void setStageDir(String path) {
		stageDir = path;
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
		new File(stageDir).mkdir();
		new File(stageDir + File.separator + "backups").mkdir(); //$NON-NLS-1$
		createDataFiles(stageDir);
		Properties properties = StageBuilder.loadProperties(stageDir);
		properties.setProperty(StageImpl.nameProperty(), stageName);
		properties.setProperty(StageImpl.appBuilderProperty(), appBuilderName);
		StageBuilder.exportProperties(stageDir, properties);
	}
	
	private void createDataFiles(String baseDir) {
		for (String datafile : TEMPLATE_FILES) {
			createFile(baseDir, datafile);
		}
	}

	private void createFile(String baseDir, String filename) {
		try {
			URL url = getClass().getResource("/resources/templates/" + filename); //$NON-NLS-1$
			ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
			FileOutputStream outStream = new FileOutputStream(new File(baseDir + File.separator + filename));
			FileChannel outChannel = outStream.getChannel();
			outChannel.transferFrom(inChannel, 0, url.openConnection().getContentLength());
			outStream.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}

}
