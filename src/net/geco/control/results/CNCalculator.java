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
import net.geco.control.GecoControl;
import net.geco.control.OEImporter;
import net.geco.control.context.ContextList;
import net.geco.control.context.GenericContext;
import net.geco.control.context.ResultContext;
import net.geco.control.context.RunnerContext;
import net.geco.control.context.StageContext;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Nov 26, 2010
 *
 */
public class CNCalculator extends AResultExporter implements StageListener {

	private File cnFile = new File(""); //$NON-NLS-1$
	
	private Map<Integer, Integer> cnArchive;
	
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
			cnArchive.put(Integer.valueOf(trimQuotes(record[4])), Integer.valueOf(trimQuotes(record[1])));
		}
		
	}

	public CNCalculator(GecoControl gecoControl) {
		super(CNCalculator.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}
	
	public File getCnFile() {
		return cnFile;
	}

	public void setCnFile(File cnFile) {
		cnArchive = null;
		this.cnFile = cnFile;
	}

	private void importCN() {
		if( cnArchive==null ){
			cnArchive = new HashMap<Integer, Integer>();
			new CNImporter(geco());
		}
	}

	@Override
	protected String getInternalTemplatePath() {
		return "/resources/formats/results_cn_internal.mustache"; //$NON-NLS-1$
	}

	@Override
	protected String getExternalTemplatePath() {
		return getInternalTemplatePath();
	}

	@Override
	protected Reader getExternalTemplateReader() throws FileNotFoundException {
		return getInternalTemplateReader();
	}

	@Override
	protected GenericContext buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;
		List<Result> results = buildResults(config);
		importCN();

		StageContext stageCtx = new StageContext(
				stage().getName(), isSingleCourseResult, config.showPenalties, refreshInterval, outputType);
		ContextList resultsCollection = stageCtx.createResultsCollection(results.size());
		mergeI18nProperties(stageCtx);
		mergeCustomStageProperties(stageCtx);
		if( config.resultType != ResultType.CourseResult ) {
			stageCtx.put("geco_StageTitle", Messages.getString("CNCalculator.CNCourseWarning")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();
				double courseScore = computeCourseScore(result);
				
				ResultContext resultCtx =
						resultsCollection.addContext(new ResultContext(result, isSingleCourseResult));
				ContextList rankingCollection = resultCtx.createRankedRunnersCollection();
				ContextList unrankedCollection = resultCtx.createUnrankedRunnersCollection();
				
				for (RankedRunner rankedRunner : result.getRanking()) {
					RunnerRaceData runnerData = rankedRunner.getRunnerData();
					Runner runner = runnerData.getRunner();
					Integer id = runner.getArchiveId();
					String currentCN = (cnArchive.get(id) != null) ?
							cnArchive.get(id).toString() : ""; //$NON-NLS-1$
					String raceScore = (id != null) ?
							Integer.toString((int) (courseScore / runnerData.getRacetime())) : ""; //$NON-NLS-1$

					RunnerContext runnerCtx =
							rankingCollection.addContext(RunnerContext.createRankedRunner(rankedRunner, bestTime));
					runnerCtx.put("geco_CN", currentCN); //$NON-NLS-1$
					runnerCtx.put("geco_CNScore", raceScore); //$NON-NLS-1$
				}

				for (RunnerRaceData data : result.getUnrankedRunners()) {
					Runner runner = data.getRunner();
					Integer id = runner.getArchiveId();
					String currentCN = (cnArchive.get(id) != null) ?
							cnArchive.get(id).toString() : ""; //$NON-NLS-1$
					if( runner.isNC() ) {
						if( config.showNC ) {
							RunnerContext runnerCtx =
									unrankedCollection.addContext(RunnerContext.createNCRunner(data));
							runnerCtx.put("geco_CN", currentCN); //$NON-NLS-1$
						} // else nothing
					} else {
						RunnerContext runnerCtx =
								unrankedCollection.addContext(RunnerContext.createUnrankedRunner(data));
						runnerCtx.put("geco_CN", currentCN); //$NON-NLS-1$
					}
				}
			}
		}
		return stageCtx;
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
		return data.getResult().getRacetime() * cnArchive.get(data.getRunner().getArchiveId());
	}

	private List<RunnerRaceData> selectTwoThirdRankedRunners(Result result) {
		ArrayList<RunnerRaceData> cnRunners = new ArrayList<RunnerRaceData>(result.getRankedRunners().size());
		for (RunnerRaceData data : result.getRankedRunners()) {
			Integer id = data.getRunner().getArchiveId();
			if( id!=null && cnArchive.get(id)!=null ){
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
