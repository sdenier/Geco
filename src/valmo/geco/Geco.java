/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import valmo.geco.control.ArchiveManager;
import valmo.geco.control.AutoMergeHandler;
import valmo.geco.control.GecoControl;
import valmo.geco.control.HeatBuilder;
import valmo.geco.control.PenaltyChecker;
import valmo.geco.control.RegistryStats;
import valmo.geco.control.ResultBuilder;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.SIReaderHandler;
import valmo.geco.control.SplitBuilder;
import valmo.geco.control.StageControl;
import valmo.geco.control.StartlistImporter;
import valmo.geco.core.Announcer;
import valmo.geco.core.GecoRequestHandler;
import valmo.geco.core.GecoResources;
import valmo.geco.core.Logger;
import valmo.geco.core.Messages;
import valmo.geco.core.Util;
import valmo.geco.functions.GeneratorFunction;
import valmo.geco.functions.RecheckFunction;
import valmo.geco.functions.StartTimeFunction;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;
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

	private static boolean leisureMode = false;

	private static String startDir = null;
	
	{
		Properties prop = new Properties();
		VERSION = "x.x"; //$NON-NLS-1$
		try {
			prop.load(getClass().getResourceAsStream("/version.prop")); //$NON-NLS-1$
			VERSION = prop.getProperty("version.num"); //$NON-NLS-1$
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
		setLaunchOptions(args);
		if( platformIsMacOs() ) {
			GecoMacos.earlySetup();
		}

		final Geco geco = new Geco(startDir);
		if( platformIsMacOs() ) {
			GecoMacos.setupQuitAction(geco);
		}
		geco.window.launchGUI();
	}
	
	private static void setLaunchOptions(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if( arg.equals("--leisure") ) { //$NON-NLS-1$
				leisureMode = true;
				continue;
			}
			if( arg.equals("--startdir") ) { //$NON-NLS-1$
				if( i < args.length-1 ) {
					startDir = args[i+1];
					i++; // skip next arg
				} else {
					System.out.println(Messages.getString("Geco.MissingStartdirOptionWarning")); //$NON-NLS-1$
				}
				continue;
			}
			System.out.println(Messages.getString("Geco.UnrecognizedOptionWarning") + arg); //$NON-NLS-1$
		}
	}

	public static boolean platformIsMacOs() {
		// See for more: http://oreilly.com/pub/a/mac/2002/09/06/osx_java.html
		return System.getProperty("mrj.version")!=null; //$NON-NLS-1$
	}
	
	public static boolean leisureModeOn() {
		return leisureMode;
	}
	
	public void exit() {
		shutdown();
		System.exit(0);
	}

	public void shutdown() {
		gecoControl.closeAllStages();
	}

	public Geco(String startDir) {
		if( startDir!=null ) {
			if( !GecoResources.exists(startDir) ) {
				System.out.println(Messages.getString("Geco.NoPathWarning") + startDir); //$NON-NLS-1$
				System.exit(0);
			}
		} else {
			try {
				startDir = launcher();
			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
				System.exit(0);
			}
		}

		updateStageList(startDir);
		gecoControl = new GecoControl();
		gecoControl.openStage(startDir);

		stageControl = new StageControl(gecoControl);
		runnerControl = new RunnerControl(gecoControl);
		resultBuilder = new ResultBuilder(gecoControl);
		new SplitBuilder(gecoControl);
		heatBuilder = new HeatBuilder(gecoControl, resultBuilder);
		stats = new RegistryStats(gecoControl);
		new AutoMergeHandler(gecoControl);
		siHandler = new SIReaderHandler(gecoControl, defaultMergeHandler());
		new ArchiveManager(gecoControl);
		new StartlistImporter(gecoControl);
		
		new StartTimeFunction(gecoControl);
		new RecheckFunction(gecoControl);
		new GeneratorFunction(gecoControl, runnerControl, siHandler);
			
		window = new GecoWindow(this);
	}

	private String launcher() throws Exception {
		return new GecoLauncher(System.getProperty("user.dir")).open(null); //$NON-NLS-1$
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
		return this.gecoControl.getService(ResultBuilder.class);
	}
	public SplitBuilder splitsBuilder() {
		return this.gecoControl.getService(SplitBuilder.class);
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
	public ArchiveManager archiveManager() {
		return this.gecoControl.getService(ArchiveManager.class);
	}
	public StartlistImporter startlistImporter() {
		return this.gecoControl.getService(StartlistImporter.class);
	}
	
	public GecoRequestHandler defaultMergeHandler() {
		return this;
	}
	public GecoRequestHandler autoMergeHandler() {
		return this.gecoControl.getService(AutoMergeHandler.class);
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
		String stageFile = fileInParentDir("stages.prop"); //$NON-NLS-1$
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
			return ""; //$NON-NLS-1$
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
			return ""; //$NON-NLS-1$
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
	public String requestMergeUnknownRunner(RunnerRaceData data, String chip) {
		return new MergeRunnerDialog(
					this,
					window,
					Messages.getString("Geco.UnknownEcardTitle")) //$NON-NLS-1$
						.showMergeDialogFor(data, chip, Status.UNK);
	}

	@Override
	public String requestMergeExistingRunner(RunnerRaceData data,	Runner target) {
		return new MergeRunnerDialog(
					this,
					window,
					Messages.getString("Geco.ExistingRunnerDataTitle")) //$NON-NLS-1$
						.showOverwriteDialogFor(data, target);
	}
	
}
