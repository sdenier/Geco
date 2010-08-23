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
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class ResultBuilder extends Control {
	
	public enum ResultType { CourseResult, CategoryResult, MixedResult }
	
	public static class ResultConfig {
		private Object[] selectedPools;
		private ResultType resultType;
		private boolean showEmptySets;
		private boolean showNC;
		private boolean showOthers;
		private boolean showPenalties;
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
				case Unknown:
					result.addOtherRunner(data);
				}
			}
		}
		result.sortRankedRunners();
		return result;
	}


	
	private Vector<Result> refreshResults(ResultConfig config) {
		Vector<Result> results = new Vector<Result>();
		for (Object selName : config.selectedPools) {
			switch (config.resultType) {
			case CourseResult:
				results.add(
						buildResultForCourse(registry().findCourse((String) selName)) );
				break;
			case CategoryResult:
				results.add(
						buildResultForCategory(registry().findCategory((String) selName)) );
				break;
			case MixedResult:
				results.addAll(
						buildResultForCategoryByCourses(registry().findCategory((String) selName)));
			}
		}
		return results;
	}

	public void exportFile(String filename, String format, ResultConfig config) throws IOException {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		if( format.equals("html") ) {
			writer.write(generateHtmlResults(config));	
		}
		if( format.equals("csv") ) {
			generateCsvResult(config, writer);
		}
		writer.close();
	}


	public String generateHtmlResults(ResultConfig config) {
		Vector<Result> results = refreshResults(config);
		Html html = new Html();
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
		html.tag("h1", result.getIdentifier());
		html.open("table");
		if( config.showPenalties ){
			html.open("tr").th("").th("Name").th("Race time").th("MP").th("Time").closeTr();
		}
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			html.openTr();
			html.td(runner.getRank()).td(data.getRunner().getName());
			appendHtmlPenalties(config.showPenalties, data, html);
			html.td(data.getResult().formatRacetime());
			html.closeTr();
		}
		html.openTr().td("").td("").td("").closeTr();
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				html.openTr();
				html.td("").td(runner.getName());
				appendHtmlPenalties(config.showPenalties, runnerData, html);
				html.td(runnerData.getResult().formatStatus());
				html.closeTr();
			} else if( config.showNC ) {
				RunnerResult runnerResult = runnerData.getResult();
				html.openTr();
				html.td("NC").td(runner.getName());
				appendHtmlPenalties(config.showPenalties, runnerData, html);
				html.td( runnerResult.shortFormat() );
				html.closeTr();
			}
		}
		if( config.showOthers ) {
			html.openTr().td("").td("").td("").closeTr();
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				html.openTr();
				html.td("").td(runnerData.getRunner().getName());
				if( config.showPenalties ) {
					html.td("").td("");
				}
				html.td(runnerData.getResult().formatStatus());
				html.closeTr();
			}			
		}
		html.close("table");
	}

	private void appendHtmlPenalties(boolean showPenalties, RunnerRaceData data, Html html) {
		if( showPenalties ){
			html.td(TimeManager.time(data.realRaceTime())).td(data.getResult().getNbMPs());
		}
	}

	/**
	 * @param writer
	 * @throws IOException 
	 */
	public void generateCsvResult(ResultConfig config, BufferedWriter writer) throws IOException {
		// TODO: use CsvWriter
		Vector<Result> results = refreshResults(config);
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
	private void appendCsvResult(Result result, ResultConfig config, BufferedWriter writer) throws IOException {
		String id = result.getIdentifier();
		for (RankedRunner rRunner : result.getRanking()) {
			RunnerRaceData runnerData = rRunner.getRunnerData();
			String[] line = new String[] {
					id,
					Integer.toString(rRunner.getRank()),
					runnerData.getRunner().getFirstname(),
					runnerData.getRunner().getLastname(),
					runnerData.getResult().formatRacetime(),
			};
			writer.write(Util.join(line, ",", new StringBuffer()));
			if( config.showPenalties ){
				line = new String[] {
					TimeManager.time(runnerData.realRaceTime()),
					Integer.toString(runnerData.getResult().getNbMPs()) };
				writer.write(",");
				writer.write(Util.join(line, ",", new StringBuffer()));
			};
			writer.write("\n");
		}
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				String[] line = new String[] {
						id,
						runnerData.getResult().formatStatus(),
						runnerData.getRunner().getFirstname(),
						runnerData.getRunner().getLastname(),
				};
				writer.write(Util.join(line, ",", new StringBuffer()));
				if( config.showPenalties ){
					line = new String[] {
						"",
						TimeManager.time(runnerData.realRaceTime()),
						Integer.toString(runnerData.getResult().getNbMPs()) };
					writer.write(",");
					writer.write(Util.join(line, ",", new StringBuffer()));
				};
				writer.write("\n");
			} else if( config.showNC ) {
				String[] line = new String[] {
						id,
						"NC",
						runnerData.getRunner().getFirstname(),
						runnerData.getRunner().getLastname(),
				};
				writer.write(Util.join(line, ",", new StringBuffer()));
				if( runnerData.getResult().is(Status.OK) ) {
					writer.write("," + runnerData.getResult().formatRacetime());
				} else {
					writer.write(","); // empty cell for race time
				}
				if( config.showPenalties ){
					line = new String[] {
						TimeManager.time(runnerData.realRaceTime()),
						Integer.toString(runnerData.getResult().getNbMPs()) };
					writer.write(",");
					writer.write(Util.join(line, ",", new StringBuffer()));
				};
				writer.write("\n");
			}
		}
		if( config.showOthers ) {
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				String[] line = new String[] {
						id,
						runnerData.getResult().formatStatus(),
						runnerData.getRunner().getFirstname(),
						runnerData.getRunner().getLastname(),
				};
				writer.write(Util.join(line, ",", new StringBuffer()));
				writer.write("\n");
			}			
		}
	}
	
}
