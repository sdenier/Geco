/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.geco.basics.CsvWriter;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.context.ContextList;
import net.geco.control.context.GenericContext;
import net.geco.control.context.ResultContext;
import net.geco.control.context.RunnerContext;
import net.geco.control.context.StageContext;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Section;

/**
 * @author Simon Denier
 * @since Aug 12, 2013
 *
 */
public class SectionsExporter extends AResultExporter {

	public SectionsExporter(GecoControl gecoControl) {
		super(SectionsExporter.class, gecoControl);
	}

	@Override
	protected String getInternalTemplatePath() {
		return "/resources/formats/results_sections_internal.mustache"; //$NON-NLS-1$
	}

	@Override
	protected String getExternalTemplatePath() {
		return getInternalTemplatePath();
	}

	@Override
	protected GenericContext buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;
		List<Result> results = buildResults(config);

		StageContext stageCtx = new StageContext(
				stage().getName(), isSingleCourseResult, config.showPenalties, refreshInterval, outputType);
		ContextList resultsCollection = stageCtx.createResultsCollection(results.size());
		mergeI18nProperties(stageCtx);
		mergeCustomStageProperties(stageCtx);

		for (Result result : results) {
			if( ! result.isEmpty() && result.sameCourse() ) {
				List<Section> sections = result.anyCourse().getSections();
				SplitTime[] bestSplits = resultBuilder.initializeBestSectionSplits(sections);
				Map<RunnerRaceData, SplitTime[]> allSections =
						resultBuilder.buildAllSectionSplits(result, sections, bestSplits);
				long bestTime = result.bestTime();

				ResultContext resultCtx =
						resultsCollection.addContext(new ResultContext(result, isSingleCourseResult));
				buildSectionsHeader(sections, resultCtx);
				
				ContextList rankingCollection = resultCtx.createRankedRunnersCollection();
				ContextList unrankedCollection = resultCtx.createUnrankedRunnersCollection();

				for (RankedRunner rankedRunner : result.getRanking()) {
					SplitTime[] runnerSectionTimes = allSections.get(rankedRunner.getRunnerData());
					RunnerContext runnerCtx =
							rankingCollection.addContext(RunnerContext.createRankedRunner(rankedRunner, bestTime));
					createRunnerSplits(runnerCtx, runnerSectionTimes, bestSplits);
				}

				for (RunnerRaceData data : result.getUnrankedRunners()) {
					SplitTime[] runnerSectionTimes = allSections.get(data);
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							RunnerContext runnerCtx =
									unrankedCollection.addContext(RunnerContext.createNCRunner(data));
							createRunnerSplits(runnerCtx, runnerSectionTimes, bestSplits);
						} // else nothing
					} else {
						RunnerContext runnerCtx =
								unrankedCollection.addContext(RunnerContext.createUnrankedRunner(data));
						createRunnerSplits(runnerCtx, runnerSectionTimes, bestSplits);
					}
				}
			}
		}
		return stageCtx;
	}

	public void buildSectionsHeader(List<Section> sections, GenericContext context) {
		ContextList sectionsList = context.createContextList("geco_SectionHeaders", sections.size()); //$NON-NLS-1$
		for (Section section : sections) {
			GenericContext sectionCtx = new GenericContext();
			sectionCtx.put("geco_SectionName", section.getName()); //$NON-NLS-1$
			sectionCtx.put("geco_SectionStartControl", section.getStartControl()); //$NON-NLS-1$
			sectionsList.add(sectionCtx);
		}
	}

	public void createRunnerSplits(RunnerContext runnerCtx, SplitTime[] runnerSectionTimes, SplitTime[] bestSplits) {
		ContextList timeRow = runnerCtx.createContextList("geco_SectionTimeRow"); //$NON-NLS-1$
		ContextList splitRow = runnerCtx.createContextList("geco_SectionSplitRow"); //$NON-NLS-1$
		for (int i = 0; i < runnerSectionTimes.length; i++) {
			SplitTime splitTime = runnerSectionTimes[i];

			GenericContext timeCtx = timeRow.addContext(new GenericContext());
			boolean isBestTime = i < bestSplits.length && splitTime.time == bestSplits[i].time;
			timeCtx.put("geco_BestTime?", isBestTime); //$NON-NLS-1$
			timeCtx.put("geco_CumulTime", TimeManager.time(splitTime.time)); //$NON-NLS-1$
			timeCtx.put("geco_CumulTimeS", splitTime.time / 1000); //$NON-NLS-1$
			
			GenericContext splitCtx = splitRow.addContext(new GenericContext());
			boolean isBestSplit = i < bestSplits.length && splitTime.split == bestSplits[i].split;
			String label = (splitTime.isOK()) ? TimeManager.time(splitTime.split) : ""; //$NON-NLS-1$
			splitCtx.put("geco_BestSplit?", isBestSplit); //$NON-NLS-1$
			splitCtx.put("geco_SectionTime", label); //$NON-NLS-1$
			splitCtx.put("geco_SectionTimeS", splitTime.split / 1000); //$NON-NLS-1$
		}
	}

	@Override
	protected void exportOSplitsFiles(String filename, ResultConfig config, int refreshInterval) throws IOException {
		geco().info(Messages.getString("CNCalculator.NotFunctionalLabel"), true); //$NON-NLS-1$
	}

	@Override
	protected String getCustomTemplatePath() {
		return getService(SplitExporter.class).getCustomTemplatePath();
	}

	@Override
	protected GenericContext buildCustomContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		return buildDataContext(config, refreshInterval, outputType);
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
	public void generateXMLResult(ResultConfig config, String filename) throws Exception {
		new SplitXmlExporter(geco()).generateXMLResult(buildResults(config), filename, false);		
	}

}
