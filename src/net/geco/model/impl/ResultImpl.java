/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

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
	private List<RunnerRaceData> nrRunners; // MP, DNF, DSQ, NC, (unknown?)
	private List<RunnerRaceData> otherRunners; // DNS
	
	private List<RankedRunner> memoRanking; // ranking cache

	
	/**
	 * 
	 */
	public ResultImpl() {
		this.rankedRunners = new Vector<RunnerRaceData>();
		this.nrRunners = new Vector<RunnerRaceData >();
		this.otherRunners = new Vector<RunnerRaceData>();
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public boolean isEmpty() {
		return rankedRunners.isEmpty() && nrRunners.isEmpty();
	}

	public List<RunnerRaceData> getRankedRunners() {
		return new Vector<RunnerRaceData>(rankedRunners);
	}
	
	public void addRankedRunner(RunnerRaceData runner) {
		this.rankedRunners.add(runner);
		this.memoRanking = null; // invalidate cache
	}

	public void clearRankedRunners() {
		this.rankedRunners.clear();
	}
	
	public List<RankedRunner> getRanking() {
		if( this.memoRanking==null ) {
			this.memoRanking = new Vector<RankedRunner>(rankedRunners.size());
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

	
	public List<RunnerRaceData> getNRRunners() {
		return new Vector<RunnerRaceData>(nrRunners);
	}
	
	public void addNRRunner(RunnerRaceData runner) {
		this.nrRunners.add(runner);
		this.memoRanking = null; // invalidate cache
	}

	public void clearNrRunners() {
		this.nrRunners.clear();
	}

	
	public List<RunnerRaceData> getOtherRunners() {
		return new Vector<RunnerRaceData>(otherRunners);
	}

	public void addOtherRunner(RunnerRaceData runner) {
		this.otherRunners.add(runner);
		this.memoRanking = null; // invalidate cache
	}

	public void clearOtherRunners() {
		this.otherRunners.clear();
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
	
	public RunnerRaceData anyRunner() {
		if( !this.rankedRunners.isEmpty() ){
			return this.rankedRunners.get(0);
		}
		if( !this.nrRunners.isEmpty() ){
			return this.nrRunners.get(0);
		}
		return null;
	}

}
