/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import valmo.geco.basics.Html;
import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.model.Messages;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.ResultType;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.iocsv.CsvWriter;

/**
 * @author Simon Denier
 * @since Nov 26, 2010
 *
 */
public class CNCalculator extends AResultExporter {

	private Map<Integer, Integer> cnScores;
	
	public class CNImporter extends OEImporter {
		protected CNImporter(GecoControl gecoControl) {
			super(gecoControl);
			try {
				loadArchiveFrom(new File("data/exportCN_index5discipline1.csv")); //$NON-NLS-1$
			} catch (IOException e) {
				gecoControl.debug(e.toString());
			}
		}

		@Override
		protected void importRunnerRecord(String[] record) {
			cnScores.put(Integer.valueOf(record[1]), Integer.valueOf(record[9]));			
		}
		
	}

	/**
	 * @param clazz
	 * @param gecoControl
	 */
	public CNCalculator(GecoControl gecoControl) {
		super(CNCalculator.class, gecoControl);
	}

	private void importCN() {
		if( cnScores==null ){
			cnScores = new HashMap<Integer, Integer>();
			new CNImporter(geco());
		}
	}

	/* (non-Javadoc)
	 * @see valmo.geco.control.IResultBuilder#generateHtmlResults(valmo.geco.control.ResultBuilder.ResultConfig, int)
	 */
	@Override
	public String generateHtmlResults(ResultConfig config, int refreshDelay) {
		if( config.resultType!=ResultType.CourseResult )
			return ""; //$NON-NLS-1$

		importCN();
		Html html = new Html();
		includeHeader(html, "result.css"); //$NON-NLS-1$
		Vector<Result> results = buildResults(config);
		for (Result result : results) {
			double courseScore = computeCourseScore(result);
			html.nl().tag("h2", "class=\"pool\"", result.getIdentifier()).nl(); //$NON-NLS-1$ //$NON-NLS-2$
			html.open("table").nl(); //$NON-NLS-1$
			for (RankedRunner data : result.getRanking()) {
				RunnerResult r = data.getRunnerData().getResult();
				writeHtml(
						data.getRunnerData(),
						Integer.toString(data.getRank()),
						r.formatRacetime(),
						Integer.toString((int) (courseScore / r.getRacetime())),
						html);
			}
			html.close("table").nl(); //$NON-NLS-1$
		}
		return html.close();
	}

	private void writeHtml(RunnerRaceData runnerData, String rank, String timeOrStatus, String score, Html html) {
		Runner runner = runnerData.getRunner();
		String yScore = ""; //$NON-NLS-1$
		Integer id = runner.getArchiveId();
		if( id != null ) {
			yScore = ( cnScores.get(id) != null) ? cnScores.get(id).toString() : ""; //$NON-NLS-1$
		} else {
			score = ""; //$NON-NLS-1$
		}
		html.openTr("runner"); //$NON-NLS-1$
		html.td(rank);
		html.td(runner.getName());
		html.td(runner.getClub().getName());
		html.td(yScore);
		html.td(score);
		html.td(timeOrStatus, "class=\"time\""); //$NON-NLS-1$
		html.closeTr();
	}


	private double computeCourseScore(Result result) {
		List<RunnerRaceData> selection = selectTwoThirdRankedRunners(result);
		if( selection==null ) {
			return 0;
		}
		double courseScore = 0;
		for (RunnerRaceData selected : selection) {
			courseScore += personalValue(selected);
		}
		courseScore /= selection.size();
		return courseScore;
	}
	
	private long personalValue(RunnerRaceData data) {
		return data.getResult().getRacetime() * cnScores.get(data.getRunner().getArchiveId());
	}

	/**
	 * @param result
	 * @return
	 */
	private List<RunnerRaceData> selectTwoThirdRankedRunners(Result result) {
		ArrayList<RunnerRaceData> cnRunners = new ArrayList<RunnerRaceData>(result.getRankedRunners().size());
		for (RunnerRaceData data : result.getRankedRunners()) {
			Integer id = data.getRunner().getArchiveId();
			if( id!=null && cnScores.get(id)!=null ){
				cnRunners.add(data);
			}
		}
		if( cnRunners.size()<3 ){
			return null;
		}
		int twoThird = Math.max(3, cnRunners.size());
		return cnRunners.subList(0, twoThird);
	}

	@Override
	public void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		getService(SplitExporter.class).generateOECsvResult(config, false, writer);
	}

	
	@Override
	protected void exportCsvFile(String filename, ResultConfig config) throws IOException {
		geco().info(Messages.getString("CNCalculator.NotFunctionalLabel"), true); //$NON-NLS-1$
	}

	@Override
	protected void writeCsvResult(String poolId, RunnerRaceData runnerData,
			String rankOrStatus, String timeOrStatus, boolean showPenalties,
			CsvWriter writer) throws IOException {
		// do nothing
	}

}
