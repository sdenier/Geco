/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.geco.model.Course;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.RunnerRaceData;


/**
 * @author Simon Denier
 * @since Jan 18, 2009
 *
 */
public class ResultImpl implements Result {
	
	private String identifier;
	private List<RunnerRaceData> rankedRunners; // status OK
	private List<RunnerRaceData> unrankedRunners; // MP, DNF, DSQ, OOT, NC
	private List<RunnerRaceData> unresolvedRunners; // Unresolved status
	
	private List<RankedRunner> memoRanking; // ranking cache


	public ResultImpl() {
		this.rankedRunners = new ArrayList<RunnerRaceData>();
		this.unrankedRunners = new ArrayList<RunnerRaceData >();
		this.unresolvedRunners = new ArrayList<RunnerRaceData>();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public boolean isEmpty() {
		return rankedRunners.isEmpty() && unrankedRunners.isEmpty();
	}

	private RunnerRaceData anyRunner() {
		if( !this.rankedRunners.isEmpty() ){
			return this.rankedRunners.get(0);
		}
		if( !this.unrankedRunners.isEmpty() ){
			return this.unrankedRunners.get(0);
		}
		return null;
	}

	public Course anyCourse() {
		return anyRunner().getCourse();
	}

	public long bestTime() {
		if( this.rankedRunners.isEmpty() ){
			return 0;
		} else {
			return getRanking().get(0).getRunnerData().getRacetime();
		}
	}

	public int nbFinishedRunners() {
		return rankedRunners.size() + unrankedRunners.size();
	}

	public int nbPresentRunners() {
		return nbFinishedRunners() + unresolvedRunners.size();
	}

	public List<RunnerRaceData> getRankedRunners() {
		return new ArrayList<RunnerRaceData>(rankedRunners);
	}
	
	public void addRankedRunner(RunnerRaceData runner) {
		this.rankedRunners.add(runner);
		this.memoRanking = null; // invalidate cache
	}

	public void sortRankedRunners() {
		Collections.sort(this.rankedRunners, new Comparator<RunnerRaceData>() {
			public int compare(RunnerRaceData o1, RunnerRaceData o2) {
				long diff = o1.getResult().getRacetime() - o2.getResult().getRacetime();
				if( diff < 0) {
					return -1;
				}
				if( diff==0 ) {
					return 0;
				}
				return 1;
			}
		});
	}

	public List<RankedRunner> getRanking() {
		if( this.memoRanking==null ) {
			this.memoRanking = new ArrayList<RankedRunner>(rankedRunners.size());
			int rank = 1;
			int counter = 1;
			RunnerRaceData previous = null;
			for (RunnerRaceData runner : this.rankedRunners) {
				if( previous!=null &&
						previous.getResult().getRacetime()!=runner.getResult().getRacetime() ) {
					rank = counter; // increment or jump to current counter
				} // else: same rank, do not increment
				this.memoRanking.add(new RankedRunnerImpl(rank, runner));
				previous = runner;
				counter++;
			}
		}
		return this.memoRanking;
	}

	
	public List<RunnerRaceData> getUnrankedRunners() {
		return new ArrayList<RunnerRaceData>(unrankedRunners);
	}
	
	public void addUnrankedRunner(RunnerRaceData runner) {
		this.unrankedRunners.add(runner);
	}

	public List<RunnerRaceData> getUnresolvedRunners() {
		return new ArrayList<RunnerRaceData>(unresolvedRunners);
	}

	public void addUnresolvedRunner(RunnerRaceData runner) {
		this.unresolvedRunners.add(runner);
	}

}
