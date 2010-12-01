/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.Pool;
import valmo.geco.model.Result;
import valmo.geco.model.ResultType;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Trace;

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
		super(gecoControl);
		gecoControl.registerService(ResultBuilder.class, this);
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
				result.addNRRunner(data);
			} else {
				switch (data.getResult().getStatus()) {
				case OK: 
					result.addRankedRunner(data);
					break;
				case DNF:
				case DSQ:
				case MP:
					result.addNRRunner(data);
					break;
				case DNS:
				case NOS:
				case RUN:
				case UNK:
				case DUP:
					result.addOtherRunner(data);
				}
			}
		}
		result.sortRankedRunners();
		return result;
	}

	public Vector<Result> buildResults(Pool[] pools, ResultType type) {
		Vector<Result> results = new Vector<Result>();
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
		String seq;
		Trace trace;
		long time;
		long split;
		
	}
	
	public SplitTime[] buildNormalSplits(RunnerRaceData data, SplitTime[] bestSplits) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		ArrayList<SplitTime> added = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in normal mode, added splits appear after normal splits
		buildSplits(data, splits, added, bestSplits, true);
		splits.addAll(added);
		return splits.toArray(new SplitTime[0]);
	}	
	
	public SplitTime[] buildLinearSplits(RunnerRaceData data) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in linear mode, added splits are kept in place with others
		buildSplits(data, splits, splits, null, false);
		return splits.toArray(new SplitTime[0]);
	}

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
				if( bestSplits!=null ){
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
		SplitTime fSplit = createSplit("F", null, startTime, previousTime, data.getFinishtime().getTime());
		splits.add(fSplit); //$NON-NLS-1$
		if( bestSplits!=null ){
			SplitTime bestSplit = bestSplits[bestSplits.length - 1];
			bestSplit.time = Math.min(bestSplit.time, fSplit.time);
			bestSplit.split = Math.min(bestSplit.split, fSplit.split);
		}

	}

	protected SplitTime createSplit(String seq, Trace trace, long startTime, long previousTime, long time) {
		return new SplitTime(seq, trace, computeSplit(startTime, time), computeSplit(previousTime, time));
	}
	protected long computeSplit(long baseTime, long time) {
		if( baseTime==TimeManager.NO_TIME_l || time==TimeManager.NO_TIME_l ) {
			return TimeManager.NO_TIME_l;
		} else {
			if( baseTime < time ) {
				return time - baseTime;
			} else {
				return TimeManager.NO_TIME_l;
			}
		}		
	}
	
	public SplitTime[] buildAllNormalSplits(Result result, ResultConfig config, Map<RunnerRaceData, SplitTime[]> allSplits) {
		SplitTime[] bestSplits = null;
		if( config.resultType==ResultType.CourseResult ) {
			int nbControls = registry().findCourse(result.getIdentifier()).nbControls();
			bestSplits = new SplitTime[nbControls + 1];
			for (int i = 0; i < bestSplits.length; i++) {
				bestSplits[i] = new SplitTime("", null, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l);
			}
		}
		for (RunnerRaceData runnerData : result.getRankedRunners()) {
			allSplits.put(runnerData, buildNormalSplits(runnerData, bestSplits));
		}
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			allSplits.put(runnerData, buildNormalSplits(runnerData, bestSplits));
		}
		
		if( config.resultType==ResultType.CourseResult ) {
			return bestSplits;
		} else {
			return new SplitTime[0]; // do not care about best splits
		}
	}
	
}
