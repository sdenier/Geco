/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import valmo.geco.control.HeatBuilder;
import valmo.geco.control.PenaltyChecker;
import valmo.geco.control.PunchChecker;
import valmo.geco.control.RegistryStats;
import valmo.geco.control.ResultBuilder;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.StageBuilder;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Stage;
import valmo.geco.model.impl.POFactory;
import valmo.geco.ui.GecoWindow;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Geco is the main class, responsible for launching the application and managing the current stage.
 * It also acts as a Facade between UI widgets and controls.  
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 */
public class Geco {
	
	private class RuntimeStage {
		private Stage stage;
		
		private Logger logger;

		public RuntimeStage(Stage stage, Logger logger) {
			super();
			this.stage = stage;
			this.logger = logger;
		}

		public Stage stage() {
			return this.stage;
		}
		
		public Logger logger() {
			return this.logger;
		}	
	}
	
	/*
	 * stupid accessor against null value
	 */
	private static Stage getStage(RuntimeStage rstage) {
		return ( rstage!=null ) ? rstage.stage() : null;
	}

	/*
	 * General
	 */
	private Factory factory;
	
	private RuntimeStage current;
	
	private RuntimeStage previous;
	
	private RuntimeStage next;

	private GecoWindow window;
	
	private Announcer announcer;
	
	/*
	 * Controls
	 */
	private PenaltyChecker checker;
	
	private RunnerControl runnerControl;
	
	private ResultBuilder resultBuilder;
	
	private HeatBuilder heatBuilder;

	private StageBuilder stageBuilder;

	/*
	 * Stage list 
	 */
	private String stageListFile;

	private Vector<String> stageList;

	private int stageIndex;

	private String parentDir;

	private RegistryStats stats;
	

	public static void main(String[] args) {
		final Geco geco = new Geco();
		if( System.getProperty("mrj.version")!=null ) {
			// http://oreilly.com/pub/a/mac/2002/09/06/osx_java.html
			Application app = Application.getApplication();
			app.addApplicationListener(new ApplicationAdapter() {
				@Override
				public void handleQuit(ApplicationEvent arg0) {
					geco.exit();
				}
			});			
		}
		geco.window.launchGUI();
	}

	public void exit() {
		closeAllStages();
		System.exit(0);
	}

	/**
	 * 
	 */
	public Geco() {
		/*
		 * Bootstrapper/Launcher: select base dir
		 * if geco prop exists, open, else import Or
		 * launch GUI
		 * 
		 * -importing Or data: import data, check punches, setup stage
		 * 
		 * -opening an existing stage: load properties, setup stage
		 * -setup a stage: set properties, import data, setup heats
		 * 
		 * - switching to previous/next: if null, import/open stage
		 * switch current stage
		 */
		
//		try {
//			System.setErr(new PrintStream("error.log"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
		factory = new POFactory();
		announcer = new Announcer();
		stageBuilder = new StageBuilder(factory);
//		checker = new PunchChecker(factory);
		checker = new PenaltyChecker(factory);
		
		if( !importStage() ) {
			System.exit(-1);
		}

		runnerControl = new RunnerControl(factory, stage(), this, announcer);
		resultBuilder = new ResultBuilder(factory, stage(), announcer);
		heatBuilder = new HeatBuilder(factory);
		stats = new RegistryStats(factory, stage(), announcer);
		window = new GecoWindow(this, announcer);
	}

	public boolean importStage() {
		RuntimeStage oldStage = current;
		try {
			RuntimeStage newStage = loadStage(launcher());
			closeAllStages();
			current = newStage;
			updateStageList(stage().getBaseDir());
		} catch (Exception e) {
//			logger().log(e.toString());
			return false;
		}
		announcer.announceChange(getStage(oldStage), stage());
		return true;
	}

	private String launcher() throws Exception {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("./testData"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(null);
		if( result==JFileChooser.CANCEL_OPTION || result==JFileChooser.ERROR_OPTION ) {
			throw new Exception("Cancelled import");
		}
		String baseDir = chooser.getSelectedFile().getAbsolutePath();
		if( !new File(baseDir + File.separator + "Competition.csv").exists() ) {
			JOptionPane.showMessageDialog(null, "Directory does not contain Òr data", "Exit", JOptionPane.ERROR_MESSAGE);
			throw new Exception("Incorrect directory");
		}
		return baseDir;
	}

	private RuntimeStage loadStage(String baseDir) {
		Stage stage = stageBuilder.loadStage(baseDir, checker);
		Logger logger = initializeLogger(stage);
		return new RuntimeStage(stage, logger);
	}
	private Logger initializeLogger(Stage stage) {
		Logger logger = new Logger(stage.getBaseDir(), "geco.log");
		logger.initSessionLog(stage.getName());
		return logger;
	}
	
	private void updateStageList(String baseDir) {
		parentDir = new File(baseDir).getParent();
		String stageFile = fileInParentDir("stages.prop");
		if( !stageFile.equals(this.stageListFile) ) {
			this.stageListFile = stageFile;
			this.stageList = new Vector<String>();
			try {
				BufferedReader reader = new BufferedReader(new FileReader(this.stageListFile));
				String line = reader.readLine();
				while( line!=null ) {
					this.stageList.add(line);
					line = reader.readLine();
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// no file, proceed
			} catch (IOException e) {
				logger().debug(e);
			}
			updateStageIndex(baseDir);
		}
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

	public void saveCurrentStage() {
		Properties props = new Properties();
		announcer.announceSave(stage(), props);
		stageBuilder.save(stage(), props, "backup.zip");
	}
	
	public void closeStage(RuntimeStage runStage) {
		if( runStage!=null ) {
			announcer.announceClose(runStage.stage());
			runStage.stage().close();
			runStage.logger().close();
		}
	}
	private void closeCurrentStage() {
		closeStage(current);
	}
	private void closeNextStage() {
		closeStage(next);
		next = null;
	}
	private void closePreviousStage() {
		closeStage(previous);
		previous = null;
	}
	public void closeAllStages() {
		closeCurrentStage();
		closePreviousStage();
		closeNextStage();
	}

	public Logger logger() {
		return current.logger();
	}
	public Stage stage() {
		return current.stage();
	}
	public Registry registry() {
		return stage().registry();
	}
	public PunchChecker checker() {
		return this.checker;
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

	public String getPreviousStageDir() {
		if( stageIndex<=0 ) { // no stage list or first stage
			return "";
		} else {
			return this.stageList.get(this.stageIndex - 1);
		}			
	}
	public String getNextStageDir() {
		if( stageIndex==-1 || stageIndex==this.stageList.size()-1 ) {
			// no stage list or last stage
			return "";
		} else {
			return this.stageList.get(this.stageIndex + 1);
		}
	}
	
	public void switchToPreviousStage() {
		if( getPreviousStageDir()!="" ) {
			// save current stage to disk?
			if( previous==null ) {
				previous = loadStage(fileInParentDir(getPreviousStageDir()));
			}
			stageIndex--;
			// previous loaded, proceed with switching references around
			closeNextStage();
			next = current; // current becomes next
			current = previous; // previous becomes current
			previous = null; // unset previous ref (we dont want to automatically load previous one)
			announcer.announceChange(getStage(next), stage());
		} // else do nothing
	}

	public void switchToNextStage() {
		if( getNextStageDir()!="" ) {
			// save current stage to disk?
			if( next==null ) {
				next = loadStage(fileInParentDir(getNextStageDir()));
			}
			stageIndex++;
			// next loaded, proceed with switching references around
			closePreviousStage();
			previous = current;
			current = next;
			next = null;
			announcer.announceChange(getStage(previous), stage());
		} // else do nothing
	}

}
