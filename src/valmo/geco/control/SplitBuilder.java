/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Announcer.StageListener;
import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.ResultType;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Trace;
import valmo.geco.model.iocsv.CsvWriter;

/**
 * @author Simon Denier
 * @since Oct 15, 2010
 *
 */
public class SplitBuilder extends Control implements IResultBuilder, StageListener {
	
	private int nbColumns = 12;
	
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


	
	/**
	 * @param gecoControl
	 */
	public SplitBuilder(GecoControl gecoControl) {
		super(gecoControl);
		geco().announcer().registerStageListener(this);
		geco().registerService(SplitBuilder.class, this);
	}
	
	
	public int nbColumns() {
		return nbColumns;
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

	private void buildSplits(RunnerRaceData data, List<SplitTime> splits, List<SplitTime> added, SplitTime[] bestSplits, boolean cutSubst) {
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

	private SplitTime createSplit(String seq, Trace trace, long startTime, long previousTime, long time) {
		return new SplitTime(seq, trace, computeSplit(startTime, time), computeSplit(previousTime, time));
	}
	private long computeSplit(long baseTime, long time) {
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

	
	
	private ResultBuilder resultBuilder() {
		return geco().getService(ResultBuilder.class);
	}
	

	@Override
	public void exportFile(String filename, String format, ResultConfig config, int refreshInterval)
			throws IOException {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format; //$NON-NLS-1$
		}
		if( format.equals("html") ) { //$NON-NLS-1$
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(generateHtmlResults(config, refreshInterval));
			writer.close();
		}
		if( format.equals("csv") ) { //$NON-NLS-1$
			CsvWriter writer = new CsvWriter(";", filename); //$NON-NLS-1$
			generateCsvResult(config, writer);
			writer.close();
		}
		if( format.equals("cn.csv") ) { // delegate //$NON-NLS-1$
			resultBuilder().exportFile(filename, format, config, refreshInterval);
		}
	}
	
	public void generateCsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		Vector<Result> results = resultBuilder().buildResults(config);
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendCsvResult(result, config, writer);
			}
		}
	}
	
	private void appendCsvResult(Result result, ResultConfig config, CsvWriter writer) throws IOException {
		String id = result.getIdentifier();

		for (RankedRunner rRunner : result.getRanking()) {
			RunnerRaceData runnerData = rRunner.getRunnerData();
			writeCsvResult(
					id,
					runnerData,
					Integer.toString(rRunner.getRank()),
					runnerData.getResult().formatRacetime(),
					config.showPenalties,
					writer);
		}
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				writeCsvResult(
						id,
						runnerData,
						runnerData.getResult().formatStatus(),
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						writer);
			} else if( config.showNC ) {
				writeCsvResult(
						id,
						runnerData,
						"NC", //$NON-NLS-1$
						runnerData.getResult().shortFormat(), // time or status
						config.showPenalties,
						writer);
			}
		}
		if( config.showOthers ) {
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				writeCsvResult(
						id,
						runnerData,
						runnerData.getResult().formatStatus(),
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						writer);
			}			
		}
	}

	private void writeCsvResult(String id, RunnerRaceData runnerData, String rankOrStatus, String timeOrStatus,
			boolean showPenalties, CsvWriter writer) throws IOException {
		Runner runner = runnerData.getRunner();
		Vector<String> csvData = new Vector<String>(
				Arrays.asList(new String[] {
					id,
					rankOrStatus,
					runner.getFirstname(),
					runner.getLastname(),
					runner.getClub().getName(),
					timeOrStatus,
					( showPenalties) ? TimeManager.time(runnerData.realRaceTime()) : "", //$NON-NLS-1$
					( showPenalties) ? Integer.toString(runnerData.getResult().getNbMPs()) : "", //$NON-NLS-1$
					TimeManager.fullTime(runnerData.getOfficialStarttime()),
					TimeManager.fullTime(runnerData.getFinishtime()),
					Integer.toString(runner.getCourse().nbControls())
				}));
		
		for (SplitTime split: buildNormalSplits(runnerData, null)) {
			if( split.trace!=null ) { // finish split handled above
				csvData.add(split.trace.getBasicCode());
				csvData.add(TimeManager.fullTime(split.time));
			}
		}
		
		writer.writeRecord(csvData.toArray(new String[0]));
	}


	
	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval) {
		Vector<Result> results = resultBuilder().buildResults(config);
		Html html = new Html();
		if( refreshInterval>0 ) {
			html.open("head"); //$NON-NLS-1$
			html.contents("<meta http-equiv=\"refresh\" content=\"" + refreshInterval + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
			html.close("head"); //$NON-NLS-1$
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty() ) {
				Map<RunnerRaceData, SplitTime[]> allSplits = new HashMap<RunnerRaceData, SplitBuilder.SplitTime[]>(); 
				SplitTime[] bestSplit = buildAllNormalSplits(result, config, allSplits);
				appendHtmlResultsWithSplits(result, allSplits, bestSplit, config, html);
			}
		}
		return html.close();
	}

	private void appendHtmlResultsWithSplits(Result result, Map<RunnerRaceData, SplitTime[]> allSplits,
													SplitTime[] bestSplit, ResultConfig config, Html html) {
		html.tag("h1", result.getIdentifier()); //$NON-NLS-1$
		html.open("table"); //$NON-NLS-1$
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			generateHtmlSplitsFor(
					data,
					Integer.toString(runner.getRank()),
					data.getResult().formatRacetime(),
					allSplits.get(data),
					bestSplit,
					html);
			html.openTr().closeTr();
		}
		html.openTr().closeTr();
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			if( ! runnerData.getRunner().isNC() ) {
				generateHtmlSplitsFor(
						runnerData,
						"",
						runnerData.getResult().formatStatus(),
						allSplits.get(runnerData),
						bestSplit,
						html); //$NON-NLS-1$
			} else if( config.showNC ) {
				generateHtmlSplitsFor(
						runnerData,
						"NC",
						runnerData.getResult().shortFormat(),
						allSplits.get(runnerData),
						bestSplit,
						html); //$NON-NLS-1$
			}
			html.openTr().closeTr();
		}
		if( config.showOthers ) {
			html.openTr().closeTr();
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				generateHtmlSplitsFor(
						runnerData,
						"",
						runnerData.getResult().formatStatus(),
						buildNormalSplits(runnerData, null),
						bestSplit,
						html); //$NON-NLS-1$
				html.openTr().closeTr();
			}			
		}
		html.close("table"); //$NON-NLS-1$
	}

	public void generateHtmlSplitsFor(RunnerRaceData data, String rank, String statusTime,
													SplitTime[] splits, SplitTime[] bestSplits, Html html) {
		html.openTr();
		html.th(rank);
		html.th(data.getRunner().getName(), "align=\"left\" colspan=\"3\""); //$NON-NLS-1$
		html.th(statusTime);
		html.closeTr();
		appendHtmlSplitsInColumns(splits, bestSplits, nbColumns(), html);
	}
	
	/**
	 * @param buildNormalSplits
	 * @param html
	 */
	protected void appendHtmlSplitsInColumns(SplitTime[] splits, SplitTime[] bestSplits, int nbColumns,
																								Html html) {
		int nbRows = (splits.length / nbColumns) + 1;
		int rowStart = 0;
		for (int i = 0; i < nbRows; i++) {
			// if last line, take the last remaining splits, not a full row
			int limit = ( i==nbRows-1 ) ? (splits.length % nbColumns) : nbColumns;
			
			if( limit==0 )
				break; // in case we have splits.length a multiple of nbColumns, we can stop now
			
			// first line with seq and control number/code
			html.openTr().td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				if( split.trace != null ) {
					String label = split.seq + " (" + split.trace.getBasicCode() +")"; //$NON-NLS-1$ //$NON-NLS-2$
					html.td(label, "align=\"right\""); //$NON-NLS-1$
				} else {
					html.td(split.seq, "align=\"right\""); //$NON-NLS-1$
				}
			}
			html.closeTr();
			// second line is cumulative split since start
			html.openTr().td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				int k = j + rowStart;
				SplitTime split = splits[k];
				String label = TimeManager.time(split.time);
//				if( split.trace!=null && ! split.trace.isOK() ) {
//					label = Html.tag("i", label, new StringBuffer()).toString();
//				}
				long best = 0;
				if( k < bestSplits.length ){
					best = bestSplits[k].time; 
				}
				showWithBestSplit(label, split.time, best, html);
			}
			html.closeTr();
			// third line is partial split since previous ok punch
			html.openTr().td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				int k = j + rowStart;
				SplitTime split = splits[k];
				String label = TimeManager.time(split.split);
				if( split.trace!=null && ! split.trace.isOK() ) {
					label = "&nbsp;"; //$NON-NLS-1$
//					label = Html.tag("i", label, new StringBuffer()).toString();
				}
				long best = 0;
				if( k < bestSplits.length ){
					best = bestSplits[k].split;
				}
				showWithBestSplit(label, split.split, best, html);
			}
			html.closeTr();
			rowStart += nbColumns;
		}
	}
	
	private void showWithBestSplit(String label, long split, long best, Html html) {
		if( split==best ){
			html.th(label, "align=\"right\""); //$NON-NLS-1$
		} else {
			html.td(label, "align=\"right\""); //$NON-NLS-1$
		}
	}
	
	protected void appendHtmlSplitsInLine(SplitTime[] linearSplits, Html html) {
		for (SplitTime splitTime : linearSplits) {
			html.openTr();
			Trace trace = splitTime.trace;
			String time = TimeManager.time(splitTime.time);
			if( trace!=null ) {
				html.td(splitTime.seq);
				html.td(splitTime.trace.getCode());
				if( trace.isOK() ) {
					html.th(time, "align=\"right\""); //$NON-NLS-1$
				} else {
					if( trace.isAdded() || trace.isSubst() ) {
						time = Html.tag("i", time, new StringBuilder()).toString(); //$NON-NLS-1$
					}
					html.td(time, "align=\"right\""); //$NON-NLS-1$
				}
				html.td(TimeManager.time(splitTime.split), "align=\"right\""); //$NON-NLS-1$
			} else {
				html.td(splitTime.seq);
				html.td(""); //$NON-NLS-1$
				html.th(time, "align=\"right\""); //$NON-NLS-1$
				html.td(TimeManager.time(splitTime.split), "align=\"right\""); //$NON-NLS-1$
			}
			html.closeTr();
		}
	}
	
	@Override
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		String nbCol = props.getProperty(splitNbColumnsProperty());
		if( nbCol!=null ){
			nbColumns = Integer.parseInt(nbCol);
		}
	}
	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitNbColumnsProperty(), Integer.toString(nbColumns));
	}
	@Override
	public void closing(Stage stage) {	}

	public static String splitNbColumnsProperty() {
		return "SplitNbColumns"; //$NON-NLS-1$
	}

}
