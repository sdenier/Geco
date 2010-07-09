/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import valmo.geco.core.Announcer;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class ResultBuilder extends Control {
	
	public static class ResultConfig {
		private Object[] selectedPools;
		private boolean courseConfig;
		private boolean showEmptySets;
		private boolean showNC;
		private boolean showOthers;
	}
	
	public static ResultConfig createResultConfig(
			Object[] selectedPools,
			boolean courseConfig,
			boolean showEmptySets,
			boolean showNC,
			boolean showOthers) {
		ResultConfig config = new ResultConfig();
		config.selectedPools = selectedPools;
		config.courseConfig = courseConfig;
		config.showEmptySets = showEmptySets;
		config.showNC = showNC;
		config.showOthers = showOthers;
		return config;
	}
	
	public ResultBuilder(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
	}
	
	public Result buildResultForCategory(Category cat) {
		Result result = factory().createResult();
		result.setIdentifier(cat.getShortname());
		List<Runner> runners = registry().getRunnersFromCategory(cat);
		if( runners!=null ) {
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
			if( config.courseConfig ) {
				results.add(
						buildResultForCourse(registry().findCourse((String) selName)) );
			} else {
				results.add(
						buildResultForCategory(registry().findCategory((String) selName)) );
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
		StringBuffer res = new StringBuffer("<html>");
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendHtmlResult(result, config, res);	
			}
		}
		res.append("</html>");
		return res.toString();
	}

	/**
	 * @param result
	 * @param res
	 */
	private void appendHtmlResult(Result result, ResultConfig config, StringBuffer res) {
		res.append("<h1>").append(result.getIdentifier()).append("</h1>");
		res.append("<table>");
		for (RankedRunner runner : result.getRanking()) {
			res.append("<tr>");
			res.append("<td>").append(runner.getRank()).append("</td><td>").append(runner.getRunnerData().getRunner().getName());
			res.append("</td><td>").append(TimeManager.time(runner.getRunnerData().getResult().getRacetime()));
			res.append("</td></tr>");
		}
		res.append("<tr><td></td><td></td><td></td></tr>");
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				res.append("<tr>");
				res.append("<td></td><td>").append(runner.getName());
				res.append("</td><td>").append(runnerData.getResult().getStatus());
				res.append("</td></tr>");				
			} else if( config.showNC ) {
				res.append("<tr>");
				res.append("<td>NC</td><td>").append(runner.getName());
				RunnerResult runnerResult = runnerData.getResult();
				res.append("</td><td>");
				res.append( (runnerResult.getStatus().equals(Status.OK))? TimeManager.time(runnerResult.getRacetime()) : runnerResult.getStatus());
				res.append("</td></tr>");
			}
		}
		if( config.showOthers ) {
			res.append("<tr><td></td><td></td><td></td></tr>");
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				res.append("<tr>");
				res.append("<td></td><td>").append(runnerData.getRunner().getName());
				res.append("</td><td>").append(runnerData.getResult().getStatus());
				res.append("</td></tr>");
			}			
		}
		res.append("</table>");
	}

	/**
	 * @param writer
	 * @throws IOException 
	 */
	public void generateCsvResult(ResultConfig config, BufferedWriter writer) throws IOException {
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
					TimeManager.time(runnerData.getResult().getRacetime()),
			};
			writer.write(Util.join(line, ",", new StringBuffer()));
			writer.write("\n");
		}
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				String[] line = new String[] {
						id,
						runnerData.getResult().getStatus().toString(),
						runnerData.getRunner().getFirstname(),
						runnerData.getRunner().getLastname(),
				};
				writer.write(Util.join(line, ",", new StringBuffer()));
				writer.write("\n");
			} else if( config.showNC ) {
				String[] line = new String[] {
						id,
						"NC",
						runnerData.getRunner().getFirstname(),
						runnerData.getRunner().getLastname(),
				};
				writer.write(Util.join(line, ",", new StringBuffer()));
				if( runnerData.getResult().getStatus().equals(Status.OK) ) {
					writer.write("," + TimeManager.time(runnerData.getResult().getRacetime()));
				}
				writer.write("\n");
			}
		}
		if( config.showOthers ) {
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				String[] line = new String[] {
						id,
						runnerData.getResult().getStatus().toString(),
						runnerData.getRunner().getFirstname(),
						runnerData.getRunner().getLastname(),
				};
				writer.write(Util.join(line, ",", new StringBuffer()));
				writer.write("\n");
			}			
		}
	}
	
}
