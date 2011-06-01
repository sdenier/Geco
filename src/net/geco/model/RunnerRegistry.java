/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.Collection;
import java.util.HashMap;

/**
 * @author Simon Denier
 * @since Jun 1, 2011
 *
 */
public class RunnerRegistry {

	private HashMap<Integer, Runner> runnersById;
	
	private int maxStartId;

	public RunnerRegistry() {
		runnersById = new HashMap<Integer, Runner>();
		maxStartId = 0;
	}

	public int maxStartId() {
		return maxStartId;
	}

	private void checkMaxStartId(Runner runner) {
		maxStartId = Math.max(maxStartId, runner.getStartId());
	}
	
	private void detectMaxStartId() {
		maxStartId = 0;
		for (Runner runner : runnersById.values()) {
			checkMaxStartId(runner);
		}
	}

	public Collection<Runner> getRunners() {
		return runnersById.values();
	}

	public Runner findRunnerById(Integer id) {
		return runnersById.get(id);
	}
	
	public void addRunner(Runner runner) {
		addRunnerWithId(runner.getStartId(), runner);
	}

	private void addRunnerWithId(Integer startId, Runner runner) {
		runnersById.put(startId, runner);
		maxStartId = Math.max(maxStartId, startId);
	}

	public void registerRunner(Runner runner) {
		runner.setStartId(maxStartId + 1);
		addRunner(runner);
	}

	public void removeRunner(Runner runner) {
		runnersById.remove(runner.getStartId());
		detectMaxStartId();
	}

	public void updateRunnerStartId(Integer oldId, Runner runner) {
		runnersById.remove(oldId);
		addRunnerWithId(runner.getStartId(), runner);
	}

}
