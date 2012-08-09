/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.framework;

import java.util.List;

import net.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Apr 3, 2011
 *
 */
public interface IGecoApp extends IGeco {

	public void exit();
	
	public boolean leisureModeOn();
	
	public String version();
	
	public String buildNumber();
	
	public String getAppName();

	public Stage stage();

	public void openStage(String startDir);
	
	public void saveCurrentStage();
	
	public String getCurrentStagePath();

	public abstract IStageLaunch createStageLaunch();

	public void restart(IStageLaunch stageLaunch) throws Exception;

	public List<IStageLaunch> history();

}
