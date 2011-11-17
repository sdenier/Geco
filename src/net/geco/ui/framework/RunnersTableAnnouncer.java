/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.framework;

import java.util.LinkedList;
import java.util.List;

import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Sep 24, 2011
 *
 */
public class RunnersTableAnnouncer {

	public interface RunnersTableListener {

		public void selectedRunnerChanged(RunnerRaceData raceData);

	}

	private List<RunnersTableListener> runnersTableListeners;

	public RunnersTableAnnouncer() {
		runnersTableListeners = new LinkedList<RunnersTableAnnouncer.RunnersTableListener>();
	}
	
	public void registerRunnersTableListener(RunnersTableListener listener) {
		runnersTableListeners.add(listener);
	}

	public void announceSelectedRunnerChange(RunnerRaceData raceData) {
		for (RunnersTableListener listener : runnersTableListeners) {
			listener.selectedRunnerChanged(raceData);
		}
	}

}
