/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.core;

import java.util.Properties;
import java.util.Vector;

import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jun 13, 2009
 *
 */
public class Announcer {
	
	public interface Logging {
		
		public void log(String message, boolean warning);
		
		public void info(String message, boolean warning);

		public void dataInfo(String data);
	}

	public interface StageListener {
		
		/**
		 * Signal a change in the current stage, either of internal data or a switching 
		 * from previous stage to current.
		 * 
		 * @param previous
		 * @param next
		 */
		public void changed(Stage previous, Stage current);
		
		/**
		 * Signal a save request for the stage. Components can store their persistent properties 
		 * in the passing object, which will be synchronized and saved. 
		 * 
		 * @param stage
		 * @param properties
		 */
		public void saving(Stage stage, Properties properties);
		
		/**
		 * Signal that the application is about to quit, indicating that any resource should be 
		 * cleanly released.
		 * 
		 * @param stage
		 */
		public void closing(Stage stage);
	}
	
	public interface StageConfigListener {
		
		public void coursesChanged();
		
		public void categoriesChanged();
		
		public void clubsChanged();
	}
	
	public interface RunnerListener {
		
		/**
		 * Signal creation of a new runner in registry.
		 * 
		 * @param runner
		 */
		public void runnerCreated(RunnerRaceData runner);

		/**
		 * Signal deletion of a runner (and associated data) in registry.
		 * 
		 * @param runner
		 */
		public void runnerDeleted(RunnerRaceData runner);
		
		/**
		 * Signal that the status of the runner has just changed.
		 * 
		 * @param runner
		 * @param oldStatus
		 */
		public void statusChanged(RunnerRaceData runner, Status oldStatus);

		/**
		 * Signal that the course of the runner has just changed.
		 * @param runner
		 * @param oldCourse
		 */
		public void courseChanged(Runner runner, Course oldCourse);
		
		/**
		 * Signal potential changes across all runners.
		 */
		public void runnersChanged();
	}
	
	public interface CardListener {
		/**
		 * Signal a card has been read by the handler.
		 * 
		 * @param chip
		 */
		public void cardRead(String chip);

		
		/**
		 * Signal a card with an unregistered number has been read by the handler.
		 * 
		 * @param chip
		 */
		public void unknownCardRead(String chip);

		/**
		 * Signal a card with existing data in registry has been read again by the handler.
		 * 
		 * @param chip
		 */
		public void cardReadAgain(String chip);


		/**
		 * @param siIdent
		 */
		public void rentedCard(String siIdent);
	}
	
	public interface StationListener {
		public void stationStatus(String status);
	}

	private Vector<Logging> loggers;
	
	private Vector<StageListener> stageListeners;
	
	private Vector<StageConfigListener> stageConfigListeners;
	
	private Vector<RunnerListener> runnerListeners;
	
	private Vector<CardListener> cardListeners;
	
	private Vector<StationListener> stationListeners;
	
	public Announcer() {
		this.loggers = new Vector<Logging>();
		this.stageListeners = new Vector<StageListener>();
		this.stageConfigListeners = new Vector<StageConfigListener>();
		this.runnerListeners = new Vector<RunnerListener>();
		this.cardListeners = new Vector<CardListener>();
		this.stationListeners = new Vector<StationListener>();
	}
	
	public void registerLogger(Logging logger) {
		this.loggers.add(logger);
	}
	
	public void registerStageListener(StageListener listener) {
		this.stageListeners.add(listener);
	}

	public void registerStageConfigListener(StageConfigListener listener) {
		this.stageConfigListeners.add(listener);
	}

	public void registerRunnerListener(RunnerListener listener) {
		this.runnerListeners.add(listener);
	}
	
	public void registerCardListener(CardListener listener) {
		this.cardListeners.add(listener);
	}

	public void unregisterCardListener(CardListener listener) {
		this.cardListeners.remove(listener);
	}
	
	public void registerStationListener(StationListener listener) {
		this.stationListeners.add(listener);
	}

	
	/*
	 * Log
	 */
	public void log(String message, boolean warning) {
		for (Logging logger : this.loggers) {
			logger.log(message, warning);
		}
	}
	
	public void info(String message, boolean warning) {
		for (Logging logger : this.loggers) {
			logger.info(message, warning);
		}
	}
	
	public void dataInfo(String data) {
		for (Logging logger : this.loggers) {
			logger.dataInfo(data);
		}
	}
	
	/*
	 * Stage announcements
	 */
	public void announceChange(Stage previous, Stage next) {
		for (StageListener listener : this.stageListeners) {
			listener.changed(previous, next);
		}
	}

	public void announceSave(Stage stage, Properties props) {
		for (StageListener listener : this.stageListeners) {
			listener.saving(stage, props);
		}		
	}

	public void announceClose(Stage stage) {
		for (StageListener listener : this.stageListeners) {
			listener.closing(stage);
		}		
	}
	
	/*
	 * Stage config announcements
	 */
	public void announceCoursesChanged() {
		for (StageConfigListener listener : this.stageConfigListeners) {
			listener.coursesChanged();
		}
	}

	public void announceCategoriesChanged() {
		for (StageConfigListener listener : this.stageConfigListeners) {
			listener.categoriesChanged();
		}
	}

	public void announceClubsChanged() {
		for (StageConfigListener listener : this.stageConfigListeners) {
			listener.clubsChanged();
		}
	}

	/*
	 * Runner annoucements
	 */
	public void announceRunnerCreation(RunnerRaceData runner) {
		for (RunnerListener listener : this.runnerListeners) {
			listener.runnerCreated(runner);
		}
	}
	
	public void announceRunnerDeletion(RunnerRaceData runner) {
		for (RunnerListener listener : this.runnerListeners) {
			listener.runnerDeleted(runner);
		}
	}

	public void announceStatusChange(RunnerRaceData runnerData, Status oldStatus) {
		for (RunnerListener listener : this.runnerListeners) {
			listener.statusChanged(runnerData, oldStatus);
		}
	}
	
	public void announceCourseChange(Runner runner, Course oldCourse) {
		for (RunnerListener listener : this.runnerListeners) {
			listener.courseChanged(runner, oldCourse);
		}
	}

	public void announceRunnersChange() {
		for (RunnerListener listener : this.runnerListeners) {
			listener.runnersChanged();
		}
	}
	
	/*
	 * Card announcements
	 */
	public void announceCardRead(String chip) {
		for (CardListener listener : this.cardListeners) {
			listener.cardRead(chip);
		}
	}

	public void announceUnknownCardRead(String chip) {
		for (CardListener listener : this.cardListeners) {
			listener.unknownCardRead(chip);
		}
	}

	public void announceCardReadAgain(String chip) {
		for (CardListener listener : this.cardListeners) {
			listener.cardReadAgain(chip);
		}
	}

	public void announceRentedCard(String siIdent) {
		for (CardListener listener : this.cardListeners) {
			listener.rentedCard(siIdent);
		}		
	}
	
	public void announceStationStatus(String status) {
		for (StationListener listener : this.stationListeners) {
			listener.stationStatus(status);
		}		
	}

}
