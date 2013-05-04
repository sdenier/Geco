/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.geco.app.AppBuilder;
import net.geco.app.GecoStageLaunch;
import net.geco.basics.Announcer;
import net.geco.basics.GService;
import net.geco.basics.GecoResources;
import net.geco.basics.Logger;
import net.geco.basics.MergeRequestHandler;
import net.geco.control.ArchiveManager;
import net.geco.control.Checker;
import net.geco.control.GecoControl;
import net.geco.control.HeatBuilder;
import net.geco.control.MergeControl;
import net.geco.control.RegistryStats;
import net.geco.control.RunnerControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.StageBuilder;
import net.geco.control.StageControl;
import net.geco.control.StartlistImporter;
import net.geco.control.results.CNCalculator;
import net.geco.control.results.ResultBuilder;
import net.geco.control.results.ResultExporter;
import net.geco.control.results.SingleSplitPrinter;
import net.geco.control.results.SplitExporter;
import net.geco.framework.IGecoApp;
import net.geco.framework.IStageLaunch;
import net.geco.functions.GecoFunction;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.ui.GecoLauncher;
import net.geco.ui.GecoWindow;
import net.geco.ui.merge.MergeWizard;


/**
 * Geco is the main class, responsible for launching the application and managing the current stage.
 * It also acts as a Facade between UI widgets and controls.  
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 */
public class Geco implements IGecoApp, MergeRequestHandler {
	
	public static String VERSION;
	
	public static String BUILDNUMBER;

	public static String BUILDSTAMP;

	private static boolean leisureMode = false;

	private static String startDir = null;
	
	private static LinkedList<IStageLaunch> history = new LinkedList<IStageLaunch>();
	
	{
		Properties prop = new Properties();
		VERSION = "x.x"; //$NON-NLS-1$
		BUILDNUMBER	= "x"; //$NON-NLS-1$
		BUILDSTAMP = "x"; //$NON-NLS-1$
		try {
			prop.load(getClass().getResourceAsStream("/version.prop")); //$NON-NLS-1$
			VERSION = prop.getProperty("version.num"); //$NON-NLS-1$
			BUILDNUMBER	= prop.getProperty("build.num"); //$NON-NLS-1$
			BUILDSTAMP	= prop.getProperty("build.stamp"); //$NON-NLS-1$
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
	
	private String appName;

	/*
	 * Controls
	 */
	private StageControl stageControl;
	
	private RunnerControl runnerControl;
	
	private SIReaderHandler siHandler;


	public static void main(String[] args) {
		setLaunchOptions(args);
		if( GecoResources.platformIsMacOs() ) {
			GecoMacos.earlySetup();
		}

		try {
			loadStageHistory();
			GecoStageLaunch stageLaunch = initStageLauncher(startDir);
			new Geco().startup(stageLaunch);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
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

	private static GecoStageLaunch initStageLauncher(String startDir) {
		GecoStageLaunch stageLaunch = new GecoStageLaunch();
		if( startDir!=null ) {
			if( !GecoResources.exists(startDir) ) {
				System.out.println(Messages.getString("Geco.NoPathWarning") + startDir); //$NON-NLS-1$
				System.exit(0);
			} else {
				stageLaunch.loadFromFileSystem(startDir);
			}
		} else {
			boolean cancelled = new GecoLauncher(null, stageLaunch, history).showLauncher();
			if( cancelled ){
				System.exit(0);				
			}
		}
		return stageLaunch;
	}

	public static void loadStageHistory() {
		try {
			File historyFile = new File(historyFilePath());
			historyFile.createNewFile();
			BufferedReader reader = new BufferedReader(new FileReader(historyFile));
			String line;
			do {
				line = reader.readLine();
				if( StageBuilder.directoryHasData(line) ){
					history.addLast(new GecoStageLaunch().loadFromFileSystem(line));
				}
			} while( line != null );
			reader.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static String historyFilePath() {
		return GecoResources.getGecoSupportDirectory() + GecoResources.sep + "history"; //$NON-NLS-1$
	}

	public Geco() {
		if( GecoResources.platformIsMacOs() ) {
			GecoMacos.setupQuitAction(this);
		}
		window = new GecoWindow(this);
	}
	
	public void startup(GecoStageLaunch stageLaunch) throws Exception {
		GecoFunction.resetAll();
		AppBuilder builder = stageLaunch.getAppBuilder();
		appName = builder.getAppName();
		GecoControl gecoControl = new GecoControl(builder);
		history.remove(stageLaunch);
		history.addFirst(stageLaunch);
		gecoControl.openStage(stageLaunch.getStageDir());
		initControls(builder, gecoControl);
		window.initAndLaunchGUI(builder);
		System.gc();
	}
	
	@Override
	public String getAppName() {
		return appName;
	}

	@Override
	public IStageLaunch createStageLaunch() {
		return new GecoStageLaunch();
	}
	
	@Override
	public void restart(IStageLaunch stageLaunch) throws Exception {
		shutdown();
		startup((GecoStageLaunch) stageLaunch);
	}
	
	@Override
	public List<IStageLaunch> history(){
		return history;
	}

	public void initControls(AppBuilder builder, GecoControl gecoControl) {
		this.gecoControl = gecoControl;
		this.gecoControl.registerService(MergeRequestHandler.class, (MergeRequestHandler) this);
		builder.buildControls(gecoControl);
		stageControl = getService(StageControl.class);
		runnerControl = getService(RunnerControl.class);
		siHandler = getService(SIReaderHandler.class);
	}
	
	public String version() {
		return VERSION;
	}
	
	public String buildNumber() {
		return BUILDNUMBER;
	}

	public String buildStamp() {
		return BUILDSTAMP;
	}

	public boolean leisureModeOn() {
		return leisureMode;
	}
	
	public void exit() {
		shutdown();
		System.exit(0);
	}

	public void shutdown() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(historyFilePath()));
			for (IStageLaunch stageLaunch : history()) {
				writer.write(stageLaunch.getStageDir());
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		gecoControl.closeCurrentStage();
	}
	
	private <T extends GService> T getService(Class<T> clazz) {
		return this.gecoControl.getService(clazz);
	}
	
	public void openStage(String startDir) {
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
	
	public Checker checker() {
		return gecoControl.checker();
	}
	public StageControl stageControl() {
		return this.stageControl;
	}
	public RunnerControl runnerControl() {
		return this.runnerControl;
	}
	public ResultBuilder resultBuilder() {
		return getService(ResultBuilder.class);
	}
	public ResultExporter resultExporter() {
		return getService(ResultExporter.class);
	}
	public SplitExporter splitsExporter() {
		return getService(SplitExporter.class);
	}
	public SingleSplitPrinter splitPrinter() {
		return getService(SingleSplitPrinter.class);
	}
	public HeatBuilder heatBuilder() {
		return getService(HeatBuilder.class);
	}	
	public RegistryStats registryStats() {
		return getService(RegistryStats.class);
	}
	public SIReaderHandler siHandler() {
		return this.siHandler;
	}
	public ArchiveManager archiveManager() {
		return getService(ArchiveManager.class);
	}
	public StartlistImporter startlistImporter() {
		return getService(StartlistImporter.class);
	}
	public CNCalculator cnCalculator() {
		return getService(CNCalculator.class);
	}
	public MergeControl mergeControl() {
		return getService(MergeControl.class);
	}
	
	public MergeRequestHandler defaultMergeHandler() {
		return this;
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

	public String getCurrentStagePath() {
		return new File(stage().getBaseDir()).getAbsolutePath();
	}
	
	@Override
	public String requestMergeUnknownRunner(RunnerRaceData data, String ecard, Course course) {
		return new MergeWizard(this, window).showMergeUnknownECard(data, ecard, course);
	}

	@Override
	public String requestMergeExistingRunner(RunnerRaceData data, Runner target, Course course) {
		return new MergeWizard(this, window).showMergeDuplicateECard(data, target, course);
	}
	
}
