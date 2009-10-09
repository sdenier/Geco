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

	public interface StageListener {
		
		/**
		 * Signal a change in the current stage, either of internal data or a switching from previous stage to current.
		 * 
		 * @param previous
		 * @param next
		 */
		public void changed(Stage previous, Stage current);
		
		/**
		 * Signal a save request for the stage. Components can store their persistent properties in the passing object, which will be
		 * synchronized and saved. 
		 * 
		 * @param stage
		 * @param properties
		 */
		public void saving(Stage stage, Properties properties);
		
		/**
		 * Signal that the application is about to quit, indicating that any resource should be cleanly released.
		 * 
		 * @param stage
		 */
		public void closing(Stage stage);
	}
	
	public interface RunnerListener {
		
		/**
		 * Signal a card has been read by the handler.
		 * 
		 * @param chip
		 */
		public void cardRead(String chip);
		
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
	}

	
	private Vector<StageListener> stageListeners;
	
	private Vector<RunnerListener> runnerListeners;
	
	public Announcer() {
		this.stageListeners = new Vector<StageListener>();
		this.runnerListeners = new Vector<RunnerListener>();
	}
	
	public void registerStageListener(StageListener listener) {
		this.stageListeners.add(listener);
	}

	public void registerRunnerListener(RunnerListener listener) {
		this.runnerListeners.add(listener);
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
	 * Runner annoucements
	 */
	public void announceCardRead(String chip) {
		for (RunnerListener listener : this.runnerListeners) {
			listener.cardRead(chip);
		}
	}
	
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

}
