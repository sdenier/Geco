/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Simon Denier
 * @since Jun 1, 2011
 *
 */
public class RunnerRegistry {

	private HashMap<Integer, Runner> runnersById;
	
	private int maxStartId;

	private HashMap<Category, List<Runner>> runnersByCategory;

	private HashMap<String, Runner> runnersByEcard;


	public RunnerRegistry() {
		runnersById = new HashMap<Integer, Runner>();
		runnersByCategory = new HashMap<Category, List<Runner>>();
		runnersByEcard = new HashMap<String, Runner>();
		maxStartId = 0;
	}

	public int maxStartId() {
		return maxStartId;
	}

	private void checkMaxStartId(Runner runner) {
		maxStartId = Math.max(maxStartId, runner.getStartId().intValue());
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
		addRunnerWithId(runner);
		putRunnerByEcard(runner);
		putRunnerInCategoryList(runner, runner.getCategory());
	}

	private void putRunnerByEcard(Runner runner) {
		String ecard = runner.getEcard();
		if( ecard!=null && ! ecard.equals("")){
			runnersByEcard.put(ecard, runner);
		}
	}

	private void addRunnerWithId(Runner runner) {
		runnersById.put(runner.getStartId(), runner);
		checkMaxStartId(runner);
	}

	public void addRunnerWithoutId(Runner runner) {
		runner.setStartId(Integer.valueOf(maxStartId + 1));
		addRunner(runner);
	}

	public void addRunnerSafely(Runner runner) {
		if( availableStartId(runner.getStartId()) ){
			addRunner(runner);
		} else {
			addRunnerWithoutId(runner);
		}
	}

	public void removeRunner(Runner runner) {
		runnersById.remove(runner.getStartId());
		runnersByEcard.remove(runner.getEcard());
		runnersByCategory.get(runner.getCategory()).remove(runner);
		detectMaxStartId();
	}

	public void updateRunnerStartId(Integer oldId, Runner runner) {
		runnersById.remove(oldId);
		addRunnerWithId(runner);
	}

	public boolean availableStartId(Integer id) {
		return id!=null && ! runnersById.containsKey(id);
	}

	private void putRunnerInCategoryList(Runner runner, Category category) {
		runnersByCategory.get(category).add(runner);
	}

	public Collection<Runner> getRunnersFromCategory(Category category) {
		return runnersByCategory.get(category);
	}

	public void categoryCreated(Category category) { // TODO protected?
		runnersByCategory.put(category, new LinkedList<Runner>());
	}

	public void updateRunnerCategory(Category oldCat, Runner runner) {
		if( !oldCat.equals(runner.getCategory()) ){
			runnersByCategory.get(oldCat).remove(runner);
			putRunnerInCategoryList(runner, runner.getCategory());
		}
	}

	public Runner findRunnerByEcard(String ecard) {
		return runnersByEcard.get(ecard);
	}

	public void updateRunnerEcard(String oldEcard, Runner runner) {
		runnersByEcard.remove(oldEcard);
		putRunnerByEcard(runner);
	}

}
