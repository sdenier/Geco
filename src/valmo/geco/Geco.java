/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import valmo.geco.control.GecoControl;
import valmo.geco.control.HeatBuilder;
import valmo.geco.control.PenaltyChecker;
import valmo.geco.control.RegistryStats;
import valmo.geco.control.ResultBuilder;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.SIReaderHandler;
import valmo.geco.control.StageControl;
import valmo.geco.core.Announcer;
import valmo.geco.core.GecoRequestHandler;
import valmo.geco.core.Logger;
import valmo.geco.core.Util;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.ui.GecoLauncher;
import valmo.geco.ui.GecoWindow;
import valmo.geco.ui.MergeRunnerDialog;

/**
 * Geco is the main class, responsible for launching the application and managing the current stage.
 * It also acts as a Facade between UI widgets and controls.  
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 */
public class Geco implements GecoRequestHandler {
	
	public static String VERSION; 
	
	{
		Properties prop = new Properties();
		VERSION = "x.x";
		try {
			prop.load(getClass().getResourceAsStream("/version.prop"));
			VERSION = prop.getProperty("version.num");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/*
	 * General
	 */
	private GecoControl gecoControl;

	private GecoWindow window;
	
	/*
	 * Controls
	 */
	private StageControl stageControl;
	
	private RunnerControl runnerControl;
	
	private ResultBuilder resultBuilder;
	
	private HeatBuilder heatBuilder;
	
	private SIReaderHandler siHandler;

	private RegistryStats stats;

	/*
	 * Stage list
	 */
	private String stageListFile;

	private Vector<String> stageList;

	private int stageIndex;

	private String parentDir;


	public static void main(String[] args) {
		final Geco geco = new Geco();
		if( platformIsMacOs() ) {
			GecoMacos.setupQuitAction(geco);
		}
		geco.window.launchGUI();
	}

	public static boolean platformIsMacOs() {
		// See for more: http://oreilly.com/pub/a/mac/2002/09/06/osx_java.html
		return System.getProperty("mrj.version")!=null;
	}
	
	
	public void exit() {
		gecoControl.closeAllStages();
		System.exit(0);
	}

	public Geco() {
		String startDir = null;
		try {
			startDir = launcher();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(0);
		}

		updateStageList(startDir);
		gecoControl = new GecoControl(startDir);

		stageControl = new StageControl(gecoControl);
		runnerControl = new RunnerControl(gecoControl);
		resultBuilder = new ResultBuilder(gecoControl);
		heatBuilder = new HeatBuilder(gecoControl, resultBuilder);
		stats = new RegistryStats(gecoControl);
		siHandler = new SIReaderHandler(gecoControl, this);
			
		window = new GecoWindow(this);
	}

	private String launcher() throws Exception {
		return new GecoLauncher(System.getProperty("user.dir")).open(null);
	}
	
	public void openStage(String startDir) {
		updateStageList(startDir);
		gecoControl.openStage(startDir);
	}
	
	public void saveCurrentStage() {
		gecoControl.saveCurrentStage();
	}
	
	
	public Announcer announcer() {
		return gecoControl.announcer();
	}
	public Stage stage() {
		return gecoControl.stage();
	}
	public Registry registry() {
		return gecoControl.registry();
	}
	
	public PenaltyChecker checker() {
		return gecoControl.checker();
	}
	public StageControl stageControl() {
		return this.stageControl;
	}
	public RunnerControl runnerControl() {
		return this.runnerControl;
	}
	public ResultBuilder resultBuilder() {
		return this.resultBuilder;
	}
	public HeatBuilder heatBuilder() {
		return this.heatBuilder;
	}	
	public RegistryStats registryStats() {
		return this.stats;
	}
	public SIReaderHandler siHandler() {
		return this.siHandler;
	}
	
	public Logger logger() {
		return gecoControl.logger();
	}
	public void debug(String message) {
		gecoControl.debug(message);
	}
	public void log(String message) {
		gecoControl.log(message);
	}
	public void info(String message, boolean warning) {
		gecoControl.info(message, warning);
	}

	
	private void updateStageList(String baseDir) {
		parentDir = new File(baseDir).getParent();
		String stageFile = fileInParentDir("stages.prop");
		if( !stageFile.equals(this.stageListFile) ) {
			this.stageListFile = stageFile;
			this.stageList = new Vector<String>();
			try {
				this.stageList = Util.readLines(this.stageListFile);
			} catch (FileNotFoundException e) {
				// no file, proceed
			} catch (IOException e) {
				logger().debug(e);
			}
		}
		updateStageIndex(baseDir);
	}
	private String fileInParentDir(String filename) {
		return parentDir + File.separator + filename;
	}
	private void updateStageIndex(String baseDir) {
		this.stageIndex = -1;
		for (int i = 0; i < this.stageList.size(); i++) {
			if( baseDir.endsWith(this.stageList.get(i)) ) {
				this.stageIndex = i;
				break;
			}
		}
	}
	
	public String getCurrentStagePath() {
		return new File(stage().getBaseDir()).getAbsolutePath();
	}

	public boolean hasPreviousStage() {
		return stageIndex > 0;
	}
	
	public String getPreviousStageDir() {
		if( hasPreviousStage() ) {
			return this.stageList.get(this.stageIndex - 1);
		} else {
			return "";
		}
	}
	
	public String getPreviousStagePath() {
		return fileInParentDir(getPreviousStageDir());
	}
	
	public boolean hasNextStage() {
		return stageIndex >= 0 && stageIndex < this.stageList.size()-1;
	}

	public String getNextStageDir() {
		if( hasNextStage() ) {
			return this.stageList.get(this.stageIndex + 1);
		} else {
			return "";
		}
	}
	
	public String getNextStagePath() {
		return fileInParentDir(getNextStageDir());
	}
	
	public void switchToPreviousStage() {
		if( hasPreviousStage() ) {
			gecoControl.preloadPreviousStage(getPreviousStagePath());
			stageIndex--;
			gecoControl.switchToPreviousStage();
		} // else do nothing
	}

	public void switchToNextStage() {
		if( hasNextStage() ) {
			gecoControl.preloadNextStage(getNextStagePath());
			stageIndex++;
			gecoControl.switchToNextStage();
		} // else do nothing
	}

	@Override
	public void requestMergeUnknownRunner(RunnerRaceData data, String chip) {
		new MergeRunnerDialog(this, window, "Unknown Chip").showMergeDialogFor(data, chip);		
	}

	@Override
	public void requestMergeExistingRunner(RunnerRaceData data,	Runner target) {
		new MergeRunnerDialog(this, window, "Existing Data for Runner").showOverwriteDialogFor(data, target);
	}
	
}
