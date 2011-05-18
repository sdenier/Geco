/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.geco.basics.Announcer;
import net.geco.basics.GecoRequestHandler;
import net.geco.basics.GecoResources;
import net.geco.basics.GecoWarning;
import net.geco.basics.Logger;
import net.geco.control.ArchiveManager;
import net.geco.control.AutoMergeHandler;
import net.geco.control.CNCalculator;
import net.geco.control.GecoControl;
import net.geco.control.HeatBuilder;
import net.geco.control.PenaltyChecker;
import net.geco.control.RegistryStats;
import net.geco.control.ResultBuilder;
import net.geco.control.ResultExporter;
import net.geco.control.RunnerControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.SingleSplitPrinter;
import net.geco.control.SplitExporter;
import net.geco.control.StageControl;
import net.geco.control.StartlistImporter;
import net.geco.framework.IGecoApp;
import net.geco.functions.GeneratorFunction;
import net.geco.functions.RecheckFunction;
import net.geco.functions.StartTimeFunction;
import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.ui.GecoWindow;
import net.geco.ui.basics.GecoLauncher;
import net.geco.ui.components.MergeRunnerDialog;


/**
 * Geco is the main class, responsible for launching the application and managing the current stage.
 * It also acts as a Facade between UI widgets and controls.  
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 */
public class Geco implements IGecoApp, GecoRequestHandler {
	
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
	
	private HeatBuilder heatBuilder;
	
	private SIReaderHandler siHandler;

	private RegistryStats stats;


	public static void main(String[] args) {
		setLaunchOptions(args);
		if( GecoResources.platformIsMacOs() ) {
			GecoMacos.earlySetup();
		}

		final Geco geco = new Geco(startDir);
		if( GecoResources.platformIsMacOs() ) {
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
	
	public String version() {
		return VERSION;
	}

	public boolean leisureModeOn() {
		return leisureMode;
	}
	
	public void exit() {
		shutdown();
		System.exit(0);
	}

	public void shutdown() {
		gecoControl.closeCurrentStage();
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
			} catch (GecoWarning w) {
				System.out.println(w.getLocalizedMessage());
				System.exit(0);
			} catch (Exception e) {
				System.err.println(Messages.getString("Geco.FatalLaunchError")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);
			}
		}

		gecoControl = new GecoControl();
		gecoControl.openStage(startDir);
		
		// TODO: call AppBuilder (launch AppWizard)

		stageControl = new StageControl(gecoControl);
		runnerControl = new RunnerControl(gecoControl);
		new ResultBuilder(gecoControl);
		new ResultExporter(gecoControl);
		new SplitExporter(gecoControl);
		new SingleSplitPrinter(gecoControl);
		heatBuilder = new HeatBuilder(gecoControl);
		stats = new RegistryStats(gecoControl);
		new AutoMergeHandler(gecoControl);
		siHandler = new SIReaderHandler(gecoControl);
		siHandler.setRequestHandler(defaultMergeHandler());
		new ArchiveManager(gecoControl);
		new StartlistImporter(gecoControl);
		new CNCalculator(gecoControl);
		
		new StartTimeFunction(gecoControl);
		new RecheckFunction(gecoControl);
		new GeneratorFunction(gecoControl);
			
		window = new GecoWindow(this);
	}

	private String launcher() throws Exception {
		return new GecoLauncher(System.getProperty("user.dir")).open(null); //$NON-NLS-1$
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
	public ResultExporter resultExporter() {
		return this.gecoControl.getService(ResultExporter.class);
	}
	public SplitExporter splitsExporter() {
		return this.gecoControl.getService(SplitExporter.class);
	}
	public SingleSplitPrinter splitPrinter() {
		return this.gecoControl.getService(SingleSplitPrinter.class);
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
	public CNCalculator cnCalculator() {
		return this.gecoControl.getService(CNCalculator.class);
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

	public String getCurrentStagePath() {
		return new File(stage().getBaseDir()).getAbsolutePath();
	}
	
	@Override
	public String requestMergeUnknownRunner(RunnerRaceData data, String ecard) {
		return new MergeRunnerDialog(
					this,
					window,
					Messages.getString("Geco.UnknownEcardTitle")) //$NON-NLS-1$
						.showMergeDialogFor(data, ecard, Status.UNK);
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
