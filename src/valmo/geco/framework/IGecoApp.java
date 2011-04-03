/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.framework;

import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Apr 3, 2011
 *
 */
public interface IGecoApp extends IGeco {

	public void exit();
	
	public boolean leisureModeOn();
	
	public String version();
	
	public Stage stage();

	public void openStage(String startDir);
	
	public void saveCurrentStage();
	
	public String getCurrentStagePath();

	public boolean hasPreviousStage();
	
	public String getPreviousStageDir();
	
	public String getPreviousStagePath();
	
	public boolean hasNextStage();

	public String getNextStageDir();
	
	public String getNextStagePath();
	
	public void switchToPreviousStage();

	public void switchToNextStage();
	
}
