/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.geco.basics.Announcer;
import net.geco.basics.GService;
import net.geco.basics.Logger;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;


/**
 * @author Simon Denier
 * @since Aug 20, 2010
 *
 */
public class GecoControl {
	
	private static class RuntimeStage {
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
	
	
	private RuntimeStage current;
	
	private final Factory factory;
	
	private final Announcer announcer;

	private StageBuilder stageBuilder;

	private Checker checker;

	private Thread autosaveThread;

	private final SimpleDateFormat backupDateFormat = new SimpleDateFormat("yyMMdd-HHmmss"); //$NON-NLS-1$
	
	
	/*
	 * Services
	 */
	@SuppressWarnings("rawtypes")
	private Map services = new HashMap();

	@SuppressWarnings("unchecked")
	public <T extends GService, U extends T> void registerService(Class<U> clazz, T service) {
		services.put(clazz, service);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GService> T getService(Class<T> clazz) {
		return (T) services.get(clazz);
	}
	
	
	/**
	 * Constructor for a generic GecoControl. See openStage() for the full initialization
	 * TODO: refactor GecoControl constructors (no more default tracer/checker)
	 */
	public GecoControl() {
		factory = new POFactory();
		announcer = new Announcer();

		// early controls
		stageBuilder = new StageBuilder(factory);
		checker = new PenaltyChecker(this, new InlineTracer(factory));
	}

	/**
	 * Utility constructor for apps which typically work on a single stage 
	 * and do not need autosaving enabled by default.
	 * @param startDir
	 * @param withLogger 
	 */
	public GecoControl(String startDir, boolean withLogger) {
		this();
		current = loadStage(startDir, withLogger);
		announcer.announceChange(null, stage()); // unnecessary? Checker already initialized
	}
	
	public GecoControl(String startDir) {
		this(startDir, true);
	}

	public GecoControl(ControlBuilder builder) {
		announcer = new Announcer();
		factory = builder.getFactory();
		stageBuilder = builder.createStageBuilder();
		checker = builder.createChecker(this);
	}

	public Factory factory() {
		return this.factory;
	}
	public Announcer announcer() {
		return this.announcer;
	}
	public Stage stage() {
		return current.stage();
	}
	public Registry registry() {
		return stage().registry();
	}
	public Checker checker() {
		return checker;
	}

	/**
	 * Open new stage with data in the provided dir. Clean up previous stages and start autosaving.
	 * 
	 * @param baseDir
	 */
	public void openStage(String baseDir) {
		Stage oldStage = null;
		if( current!=null ){
			oldStage = current.stage();
			closeCurrentStage();
		}

		current = loadStage(baseDir, true);

		announcer.announceChange(oldStage, stage());
		startAutosave();
	}
	private RuntimeStage loadStage(String baseDir, boolean withLogger) {
		Stage stage = stageBuilder.loadStage(baseDir, checker);
		Logger logger = null;
		if( withLogger ) {
			logger = initializeLogger(stage);
		}
		return new RuntimeStage(stage, logger);
	}

	private String backupFilename(String id) {
		return "backups" + File.separator + "backup" + id + ".zip"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	private void saveStage(String backupName) {
		Properties props = new Properties();
		announcer.announceSave(stage(), props);
		stageBuilder.save(	stage(), 
							props, 
							backupName);
	}

	public void saveCurrentStage() {
		saveStage( backupFilename( backupDateFormat.format(new Date()) ));
	}
	
	public void closeCurrentStage() {
		stopAutosave();
		saveCurrentStage();
		announcer.announceClose(stage());
		logger().close();
	}
	
	public Thread startAutosave() {
		final long saveDelay = stage().getAutosaveDelay() * 60 * 1000;
		autosaveThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				int id = stage().getNbAutoBackups();
				while( true ){
					try {
						wait(saveDelay);
						id ++;
						if( id > stage().getNbAutoBackups() ) {
							id = 1;
						}
						saveStage(backupFilename(Integer.toString(id)));
					} catch (InterruptedException e) {
						return;
					}					
				}
			}});
		autosaveThread.start();
		return autosaveThread;
	}
	
	public void stopAutosave() {
		if( autosaveThread!=null ) {
			autosaveThread.interrupt();
			try {
				autosaveThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Logger initializeLogger(Stage stage) {
		Logger logger = new Logger(stage.getBaseDir(), "geco.log"); //$NON-NLS-1$
		logger.initSessionLog(stage.getName());
		return logger;
	}
	public Logger logger() {
		return current.logger();
	}
	public void debug(String message) {
		logger().debug(message);
		announcer().log(message, true);
	}
	public void log(String message) {
		logger().log(message);
		announcer().log(message, false);
	}
	public void info(String message, boolean warning) {
		announcer().info(message, warning);
	}

}
