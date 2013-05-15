/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.control.GecoControl;
import net.geco.control.OEImporter;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.context.GenericContext;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Nov 26, 2010
 *
 */
public class CNCalculator extends AResultExporter implements StageListener {

	private File cnFile = new File(""); //$NON-NLS-1$
	
	private Map<Integer, Integer> cnScores;
	
	public class CNImporter extends OEImporter {
		protected CNImporter(GecoControl gecoControl) {
			super(gecoControl);
			try {
				loadArchiveFrom(getCnFile()); //$NON-NLS-1$
			} catch (IOException e) {
				gecoControl.debug(e.toString());
			}
		}

		@Override
		protected void importRunnerRecord(String[] record) {
//			licence -> CN
			cnScores.put(Integer.valueOf(trimQuotes(record[4])), Integer.valueOf(trimQuotes(record[1])));
		}
		
	}

	/**
	 * @param clazz
	 * @param gecoControl
	 */
	public CNCalculator(GecoControl gecoControl) {
		super(CNCalculator.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}
	
	public File getCnFile() {
		return cnFile;
	}

	public void setCnFile(File cnFile) {
		cnScores = null;
		this.cnFile = cnFile;
	}

	private void importCN() {
		if( cnScores==null ){
			cnScores = new HashMap<Integer, Integer>();
			new CNImporter(geco());
		}
	}

	@Override
	protected String getInternalTemplatePath() {
		return "/resources/formats/results_cn_internal.mustache"; //$NON-NLS-1$
	}

	@Override
	protected Reader getInternalTemplateReader() {
		return GecoResources.getResourceReader(getInternalTemplatePath());
	}

	@Override
	protected String getExternalTemplatePath() {
		return ""; //$NON-NLS-1$
	}

	@Override
	protected Reader getExternalTemplateReader() throws FileNotFoundException {
		return getInternalTemplateReader();
	}

	@Override
	protected GenericContext buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshDelay, OutputType outputType) {
		if( config.resultType!=ResultType.CourseResult )
			return Messages.getString("CNCalculator.CNCourseWarning"); //$NON-NLS-1$

		List<Result> results = buildResults(config);
		importCN();
		Html html = new Html();
		includeHeader(html, "result.css", outputType); //$NON-NLS-1$
		if( outputType != OutputType.DISPLAY ) {
			html.nl().tag("h1", stage().getName() //$NON-NLS-1$
								+ " - "			  //$NON-NLS-1$
								+ Messages.getString("CNCalculator.CNOutputTitle")); //$NON-NLS-1$
		}
		for (Result result : results) {
			double courseScore = computeCourseScore(result);
			html.nl().tag("h2", result.getIdentifier()).nl(); //$NON-NLS-1$
			html.open("table").nl(); //$NON-NLS-1$
			html.openTr("runner") //$NON-NLS-1$
				.th("") //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.NameHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.ClubHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.CategoryHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.TimeHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("CNCalculator.CNHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("CNCalculator.ScoreHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.closeTr();
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
		html.td(runner.getCategory().getName());
		html.td(timeOrStatus, "class=\"time\""); //$NON-NLS-1$
		html.td(yScore, "class=\"right\""); //$NON-NLS-1$
		html.td(score, "class=\"time\""); //$NON-NLS-1$
		html.closeTr();
	}


	private double computeCourseScore(Result result) {
		List<RunnerRaceData> selection = selectTwoThirdRankedRunners(result);
		if( selection.isEmpty() ) {
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

	private List<RunnerRaceData> selectTwoThirdRankedRunners(Result result) {
		ArrayList<RunnerRaceData> cnRunners = new ArrayList<RunnerRaceData>(result.getRankedRunners().size());
		for (RunnerRaceData data : result.getRankedRunners()) {
			Integer id = data.getRunner().getArchiveId();
			if( id!=null && cnScores.get(id)!=null ){
				cnRunners.add(data);
			}
		}
		if( cnRunners.size()<3 ){
			return Collections.emptyList();
		}
		int twoThird = Math.max(3, cnRunners.size() * 2 / 3);
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
	public void generateXMLResult(ResultConfig config, String filename)
			throws Exception {
		new SplitXmlExporter(geco()).generateXMLResult(buildResults(config), filename, false);		
	}
	
	@Override
	public void changed(Stage previous, Stage current) {
		try {
			setCnFile(new File( stage().getProperties().getProperty(cnFileProperty()) ));
		} catch (NullPointerException e) {
			setCnFile(new File("")); //$NON-NLS-1$
		}
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		if( getCnFile().exists() ){
			properties.setProperty(cnFileProperty(), getCnFile().getAbsolutePath());
		}
	}

	@Override
	public void closing(Stage stage) {}

	public static String cnFileProperty() {
		return "CNFile"; //$NON-NLS-1$
	}

}
