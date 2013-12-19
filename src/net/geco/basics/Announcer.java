/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.basics;

import java.util.ArrayList;
import java.util.Properties;

import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;


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
	
	public interface CourseListener {

		public void courseChanged(Course course);
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
		 * @param ecard
		 */
		public void cardRead(String ecard);

		
		/**
		 * Signal a card with an unregistered number has been read by the handler.
		 * 
		 * @param ecard
		 */
		public void unknownCardRead(String ecard);

		/**
		 * Signal a card with existing data in registry has been read again by the handler.
		 * 
		 * @param ecard
		 */
		public void cardReadAgain(String ecard);


		/**
		 * @param ecard
		 */
		public void rentedCard(String ecard);


		/**
		 * @param ecard
		 */
		public void registeredCard(String ecard);
	}
	
	public interface StationListener {
		void stationReady(String status);
		void stationError(String status, String errorMessage);
	}

	private ArrayList<Logging> loggers;
	
	private ArrayList<StageListener> stageListeners;
	
	private ArrayList<StageConfigListener> stageConfigListeners;
	
	private ArrayList<RunnerListener> runnerListeners;
	
	private ArrayList<CardListener> cardListeners;
	
	private ArrayList<StationListener> stationListeners;

	private ArrayList<CourseListener> courseListeners;
	
	public Announcer() {
		this.loggers = new ArrayList<Logging>();
		this.stageListeners = new ArrayList<StageListener>();
		this.stageConfigListeners = new ArrayList<StageConfigListener>();
		this.runnerListeners = new ArrayList<RunnerListener>();
		this.cardListeners = new ArrayList<CardListener>();
		this.stationListeners = new ArrayList<StationListener>();
		this.courseListeners = new ArrayList<CourseListener>();
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
	
	public void registerCourseListener(CourseListener listener) {
		this.courseListeners.add(listener);
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
	public void announceCardRead(String ecard) {
		for (CardListener listener : this.cardListeners) {
			listener.cardRead(ecard);
		}
	}

	public void announceUnknownCardRead(String ecard) {
		for (CardListener listener : this.cardListeners) {
			listener.unknownCardRead(ecard);
		}
	}

	public void announceCardReadAgain(String ecard) {
		for (CardListener listener : this.cardListeners) {
			listener.cardReadAgain(ecard);
		}
	}

	public void announceRentedCard(String ecard) {
		for (CardListener listener : this.cardListeners) {
			listener.rentedCard(ecard);
		}		
	}

	public void announceCardRegistered(String ecard) {
		for (CardListener listener : this.cardListeners) {
			listener.registeredCard(ecard);
		}
	}
	
	public void announceStationReady(String status) {
		for (StationListener listener : this.stationListeners) {
			listener.stationReady(status);
		}		
	}

	public void announceStationError(String status, String errorMessage) {
		for (StationListener listener : this.stationListeners) {
			listener.stationError(status, errorMessage);
		}
	}

	public void announceCourseChanged(Course course) {
		for (CourseListener listener: this.courseListeners) {
			listener.courseChanged(course);
		}
	}

}
