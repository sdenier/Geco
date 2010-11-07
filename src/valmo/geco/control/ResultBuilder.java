/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.Pool;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.ResultType;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.iocsv.CsvWriter;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class ResultBuilder extends Control implements IResultBuilder {
	
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
			result.setIdentifier(cat.getShortname() + " - " + entry.getKey().getName());
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
				case NDA:
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

	
	protected Vector<Result> buildResults(ResultConfig config) {
		Vector<Pool> pools = new Vector<Pool>();
		for (Object selName : config.selectedPools) {
			switch (config.resultType) {
			case CourseResult:
				pools.add( registry().findCourse((String) selName) );
				break;
			case CategoryResult:
			case MixedResult:
				pools.add( registry().findCategory((String) selName) );
			}
		}
		return buildResults(pools.toArray(new Pool[0]), config.resultType);
	}

	@Override
	public void exportFile(String filename, String format, ResultConfig config, int refreshInterval)
			throws IOException {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format;
		}
		if( format.equals("html") ) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(generateHtmlResults(config, refreshInterval));
			writer.close();
		}
		if( format.equals("csv") ) {
			CsvWriter writer = new CsvWriter(",", filename);
			generateCsvResult(config, writer);
			writer.close();
		}
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval) {
		Vector<Result> results = buildResults(config);
		Html html = new Html();
		if( refreshInterval>0 ) {
			html.open("head");
			html.contents("<meta http-equiv=\"refresh\" content=\"" + refreshInterval + "\" />");
			html.close("head");
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendHtmlResult(result, config, html);	
			}
		}
		return html.close();
	}

	/**
	 * @param result
	 * @param html
	 */
	private void appendHtmlResult(Result result, ResultConfig config, Html html) {
		html.tag("h1", result.getIdentifier()); // add finished / present
		html.open("table");
		if( config.showPenalties ){
			html.open("tr").th("").th("Name").th("Club")
			.th("Time", "align=\"right\"").th("MP", "align=\"right\"").th("Race time", "align=\"right\"")
			.closeTr();
		}
		// Format: rank, first name + last name, club [, real time, nb mps], time/status
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			writeHtml(
					data,
					Integer.toString(runner.getRank()),
					data.getResult().formatRacetime(),
					config.showPenalties,
					html);
		}
		html.openTr().closeTr(); // jump line
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				writeHtml(
						runnerData,
						"",
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						html);
			} else if( config.showNC ) {
				writeHtml(
						runnerData,
						"NC",
						runnerData.getResult().shortFormat(),
						config.showPenalties,
						html);
			}
		}
		if( config.showOthers ) {
			html.openTr().closeTr(); // jump line
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				writeHtml(
						runnerData,
						"",
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						html);
			}			
		}
		html.close("table");
	}
	
	private void writeHtml(RunnerRaceData runnerData, String rank, String timeOrStatus,
			boolean showPenalties, Html html) {
		html.openTr();
		html.td(rank);
		html.td(runnerData.getRunner().getName());
		html.td(runnerData.getRunner().getClub().getName());
		html.th(timeOrStatus, "align=\"right\"");
		if( showPenalties ){
			html.td(Integer.toString(runnerData.getResult().getNbMPs()), "align=\"right\"");
			html.td(TimeManager.time(runnerData.realRaceTime()), "align=\"right\"");
		}
		html.closeTr();
	}


	
	
	
	
	
	/**
	 * @param writer
	 * @throws IOException 
	 */
	public void generateCsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		Vector<Result> results = buildResults(config);
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendCsvResult(result, config, writer);
			}
		}
	}

	/**
	 * @param result
	 * @param writer
	 * @throws IOException 
	 */
	private void appendCsvResult(Result result, ResultConfig config, CsvWriter writer) throws IOException {
		String id = result.getIdentifier();
		// Format: result id, rank/status, first name, last name, club, [, time/status [, real time, nb mps]]
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
						"NC",
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
		if( showPenalties ){
			writer.writeRecord(
				id,
				rankOrStatus,
				runner.getFirstname(),
				runner.getLastname(),
				runner.getClub().getName(),
				timeOrStatus,
				TimeManager.time(runnerData.realRaceTime()),
				Integer.toString(runnerData.getResult().getNbMPs()));
		} else {
			writer.writeRecord(
				id,
				rankOrStatus,
				runner.getFirstname(),
				runner.getLastname(),
				runner.getClub().getName(),
				timeOrStatus);				
		}
	}
	
}
