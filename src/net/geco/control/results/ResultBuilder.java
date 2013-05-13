/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import net.geco.basics.TimeManager;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Pool;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Trace;


/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class ResultBuilder extends Control {
	
	public static class ResultConfig {
		protected Object[] selectedPools;
		protected ResultType resultType;
		protected boolean showEmptySets;
		protected boolean showNC;
		protected boolean showOthers;
		protected boolean showPenalties;
	}
	
	public static ResultConfig createResultConfig(
			Object[] selectedPools,
			ResultType courseConfig,
			boolean showEmptySets,
			boolean showNC,
			boolean showOthers,
			boolean showPenalties) {
		ResultConfig config = new ResultConfig();
		config.selectedPools = selectedPools;
		config.resultType = courseConfig;
		config.showEmptySets = showEmptySets;
		config.showNC = showNC;
		config.showOthers = showOthers;
		config.showPenalties = showPenalties;
		return config;
	}
	
	public ResultBuilder(GecoControl gecoControl) {
		super(ResultBuilder.class, gecoControl);
	}
	
	public List<Result> buildResultForCategoryByCourses(Category cat) {
		Map<Course, List<Runner>> runnersMap = registry().getRunnersByCourseFromCategory(cat.getName());
		List<Result> results = new Vector<Result>();
		for (Entry<Course, List<Runner>> entry : runnersMap.entrySet()) {
			Result result = factory().createResult();
			result.setIdentifier(cat.getShortname() + " - " + entry.getKey().getName()); //$NON-NLS-1$
			results.add(sortResult(result, entry.getValue()));
		}
		return results;
	}
	
	public Result buildResultForCategory(Category cat) {
		Result result = factory().createResult();
		result.setIdentifier(cat.getShortname());
		List<Runner> runners = registry().getRunnersFromCategory(cat);
		if( runners!=null ){
			sortResult(result, runners);
		}
		return result;
	}
	
	public Result buildResultForCourse(Course course) {
		Result result = factory().createResult();
		result.setIdentifier(course.getName());
		List<Runner> runners = registry().getRunnersFromCourse(course);
		if( runners!=null ) {
			sortResult(result, runners);
		}
		return result;
	}

	protected Result sortResult(Result result, List<Runner> runners) {
		for (Runner runner : runners) {
			RunnerRaceData data = registry().findRunnerData(runner);
			if( runner.isNC() ) {
				result.addUnrankedRunner(data);
			} else {
				switch (data.getResult().getStatus()) {
				case OK: 
					result.addRankedRunner(data);
					break;
				case DNF:
				case DSQ:
				case MP:
				case OOT:
					result.addUnrankedRunner(data);
					break;
				case NOS:
				case RUN:
				case UNK:
				case DUP:
					result.addUnresolvedRunner(data);
					break;
				case DNS:
					// just ignore
				}
			}
		}
		result.sortRankedRunners();
		return result;
	}

	public List<Result> buildResults(Pool[] pools, ResultType type) {
		List<Result> results = new ArrayList<Result>(pools.length);
		for (Pool pool : pools) {
			switch (type) {
			case CourseResult:
				results.add(
						buildResultForCourse((Course) pool));
				break;
			case CategoryResult:
				results.add(
						buildResultForCategory((Category) pool));
				break;
			case MixedResult:
				results.addAll(
						buildResultForCategoryByCourses((Category) pool));
			}
		}
		return results;
	}

	

	public static class SplitTime {
		public SplitTime(String seq, Trace trace, long time, long split) {
			this.seq = seq;
			this.trace = trace;
			this.time = time;
			this.split = split;
		}
		public final String seq;
		public final Trace trace;
		public long time;
		public long split;
		
		public String getBasicCode() {
			return trace == null ? "" : trace.getBasicCode();
		}
		
		public boolean isFinish() {
			return "F".equals(seq);
		}
	}
	
	private SplitTime[] buildNormalSplits(RunnerRaceData data, SplitTime[] bestSplits) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		ArrayList<SplitTime> added = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in normal mode, added splits appear after normal splits
		buildSplits(data, splits, added, bestSplits, true);
		splits.addAll(added);
		return splits.toArray(new SplitTime[0]);
	}	

	public SplitTime[] buildNormalSplits(RunnerRaceData data) {
		return buildNormalSplits(data, new SplitTime[0]);
	}
	
	public SplitTime[] buildLinearSplits(RunnerRaceData data) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in linear mode, added splits are kept in place with others
		buildSplits(data, splits, splits, new SplitTime[0], false);
		return splits.toArray(new SplitTime[0]);
	}

	// TODO: add flag to include or not finish split
	protected void buildSplits(RunnerRaceData data, List<SplitTime> splits, List<SplitTime> added,
																SplitTime[] bestSplits, boolean cutSubst) {
		long startTime = data.getOfficialStarttime().getTime();
		long previousTime = startTime;
		int control = 1;
		for (Trace trace : data.getResult().getTrace()) {
			long time = trace.getTime().getTime();
			if( trace.isOK() ) {
				SplitTime split = createSplit(Integer.toString(control), trace, startTime, previousTime, time);
				splits.add(split);
				previousTime = time;
				if( bestSplits.length > 0 ){
					SplitTime bestSplit = bestSplits[control - 1];
					bestSplit.time = Math.min(bestSplit.time, split.time);
					bestSplit.split = Math.min(bestSplit.split, split.split);
				}
				control++;
			} else if( trace.isSubst() ) {
				if( cutSubst ) {
					String code = trace.getCode();
					int cut = code.indexOf("+"); //$NON-NLS-1$
					Trace mpTrace = factory().createTrace(code.substring(0, cut), TimeManager.NO_TIME);
					splits.add(createSplit(Integer.toString(control), mpTrace, startTime, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l));
					Trace addedTrace = factory().createTrace(code.substring(cut), trace.getTime());
					added.add(createSplit("", addedTrace, startTime, TimeManager.NO_TIME_l, time)); //$NON-NLS-1$
				} else {
					splits.add(createSplit(Integer.toString(control), trace, startTime, TimeManager.NO_TIME_l, time));
				}
				control++;
			} else if( trace.isMP() ) {
				splits.add(createSplit(Integer.toString(control), trace, startTime, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l));
				control++;
			} else { // added trace
				added.add(createSplit("", trace, startTime, TimeManager.NO_TIME_l, time)); //$NON-NLS-1$
			}
		}
		// TODO: do not use null value for final trace --> use if finish if necessary
		SplitTime fSplit = createSplit("F", null, startTime, previousTime, data.getFinishtime().getTime()); //$NON-NLS-1$
		splits.add(fSplit); //$NON-NLS-1$
		if( bestSplits.length > 0 ){
			SplitTime bestSplit = bestSplits[bestSplits.length - 1];
			bestSplit.time = Math.min(bestSplit.time, fSplit.time);
			bestSplit.split = Math.min(bestSplit.split, fSplit.split);
		}

	}

	protected SplitTime createSplit(String seq, Trace trace, long startTime, long previousTime, long time) {
		return new SplitTime(seq, trace, TimeManager.computeSplit(startTime, time), TimeManager.computeSplit(previousTime, time));
	}

	public SplitTime[] initializeBestSplits(Result result, ResultType resultType) {
		SplitTime[] bestSplits = new SplitTime[0];
		if( ! result.isEmpty() ){
			Course course = result.anyCourse();
			boolean sameCourse = true; // default for CourseResult and MixedResult
			if( resultType==ResultType.CategoryResult ){
				// check that all runners in category share the same course
				for (RunnerRaceData runnerData : result.getRankedRunners()) {
					sameCourse &= runnerData.getCourse()==course;
				}
				for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
					sameCourse &= runnerData.getCourse()==course;
				}
			}
			if( ! sameCourse ) {
				geco().log(Messages.getString("ResultBuilder.NoBestSplitForCategoryWarning") + result.getIdentifier()); //$NON-NLS-1$
			} else { // initialize bestSplits
				bestSplits = new SplitTime[course.nbControls() + 1];
				for (int i = 0; i < bestSplits.length; i++) {
					bestSplits[i] = new SplitTime("", null, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l); //$NON-NLS-1$
				}
			}		
		}
		return bestSplits;
	}
	
	public Map<RunnerRaceData, SplitTime[]> buildAllNormalSplits(Result result, SplitTime[] bestSplits) {
		Map<RunnerRaceData,SplitTime[]> allSplits = new HashMap<RunnerRaceData, SplitTime[]>();
		for (RunnerRaceData runnerData : result.getRankedRunners()) {
			allSplits.put(runnerData, buildNormalSplits(runnerData, bestSplits));
		}
		for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
			allSplits.put(runnerData, buildNormalSplits(runnerData, bestSplits));
		}
		return allSplits;
	}
	
}
