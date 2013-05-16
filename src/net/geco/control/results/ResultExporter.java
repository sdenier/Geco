/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.CsvWriter;
import net.geco.control.GecoControl;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.context.ContextList;
import net.geco.control.results.context.GenericContext;
import net.geco.control.results.context.ResultContext;
import net.geco.control.results.context.RunnerContext;
import net.geco.control.results.context.StageContext;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public class ResultExporter extends AResultExporter implements StageListener {
	
	private File rankingTemplate;

	public ResultExporter(GecoControl gecoControl) {
		super(ResultExporter.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}

	@Override
	protected String getInternalTemplatePath() {
		return "/resources/formats/results_ranking_internal.mustache"; //$NON-NLS-1$
	}

	@Override
	protected String getExternalTemplatePath() {
		return getRankingTemplate().getAbsolutePath();
	}

	@Override
	protected GenericContext buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		// TODO remove show empty/others from config
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;
		List<Result> results = buildResults(config);

		StageContext stageCtx = new StageContext(
				stage().getName(), isSingleCourseResult, config.showPenalties, refreshInterval, outputType);
		ContextList resultsCollection = stageCtx.createResultsCollection(results.size());
		mergeCustomStageProperties(stageCtx);

		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();

				ResultContext resultCtx =
						resultsCollection.addContext(new ResultContext(result, isSingleCourseResult));
				ContextList rankingCollection = resultCtx.createRankedRunnersCollection();
				ContextList unrankedCollection = resultCtx.createUnrankedRunnersCollection();
				
				for (RankedRunner rankedRunner : result.getRanking()) {
					rankingCollection.add(RunnerContext.createRankedRunner(rankedRunner, bestTime));
				}

				for (RunnerRaceData data : result.getUnrankedRunners()) {
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							unrankedCollection.add(RunnerContext.createNCRunner(data));
						} // else nothing
					} else {
						unrankedCollection.add(RunnerContext.createUnrankedRunner(data));
					}
				}
			}
		}
		return stageCtx;
	}

	@Override
	public void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		getService(SplitExporter.class).generateOECsvResult(config, false, writer);
	}

	@Override
	public void generateXMLResult(ResultConfig config, String filename)
			throws Exception {
		new SplitXmlExporter(geco()).generateXMLResult(buildResults(config), filename, false);		
	}

	public File getRankingTemplate() {
		return rankingTemplate;
	}

	public void setRankingTemplate(File selectedFile) {
		if( getRankingTemplate() != null ){
			resetTemplate(getExternalTemplatePath());
		}
		rankingTemplate = selectedFile;
	}
	
	@Override
	public void changed(Stage previous, Stage current) {
		try {
			setRankingTemplate(new File( stage().getProperties().getProperty(rankingTemplateProperty()) ));
		} catch (NullPointerException e) {
			setRankingTemplate(new File("formats/results_ranking.mustache")); //$NON-NLS-1$
		}
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		if( getRankingTemplate().exists() ){
			properties.setProperty(rankingTemplateProperty(), getRankingTemplate().getAbsolutePath());
		}
	}

	@Override
	public void closing(Stage stage) {}
	
	public static String rankingTemplateProperty() {
		return "RankingTemplate"; //$NON-NLS-1$
	}

}
