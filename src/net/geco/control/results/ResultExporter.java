/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.control.GecoControl;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.context.ContextList;
import net.geco.control.results.context.GenericContext;
import net.geco.control.results.context.RunnerContext;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import com.samskivert.mustache.Mustache;

/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public class ResultExporter extends AResultExporter {
	
	public ResultExporter(GecoControl gecoControl) {
		super(ResultExporter.class, gecoControl);
	}

	@Override
	protected void exportHtmlFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		buildHtmlResults("results_ranking.mustache", config, refreshInterval, writer, OutputType.FILE);
		writer.close();
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval,
			OutputType outputType) {
		StringWriter out = new StringWriter();
		try {
			// TODO display or printer template + I18N template headers
			buildHtmlResults("results_ranking.mustache", config, refreshInterval, out, outputType);
		} catch (IOException e) {
			geco().logger().debug(e);
		}
		return out.toString();
	}

	protected void buildHtmlResults(String templateFile, ResultConfig config, int refreshInterval,
			Writer out, OutputType outputType) throws IOException {
		Reader template = GecoResources.getSafeReaderFor("formats/" + templateFile);
		// TODO: lazy cache of template
		Mustache.compiler()
			.defaultValue("N/A")
			.compile(template)
			.execute(buildDataContext(config, refreshInterval, outputType), out);
		template.close();
	}

	
	protected Object buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;

		// TODO remove show empty/others from config
		// TODO load and merge properties for customization by user-defined tags
		// TODO remove header/footer prop
		GenericContext stageContext = new GenericContext();
		stageContext.put("geco_StageTitle", stage().getName());

		// General layout
		stageContext.put("geco_SingleCourse?", isSingleCourseResult);
		stageContext.put("geco_RunnerCategory?", isSingleCourseResult);
		stageContext.put("geco_Penalties?", config.showPenalties);

		// Meta info
		stageContext.put("geco_FileOutput?", outputType == OutputType.FILE);
		stageContext.put("geco_AutoRefresh?", refreshInterval > 0);
		stageContext.put("geco_RefreshInterval", refreshInterval);
		stageContext.put("geco_PrintMode?", outputType == OutputType.PRINTER);
		stageContext.put("geco_Timestamp", new SimpleDateFormat("H:mm").format(new Date()));
		
		List<Result> results = buildResults(config);
		ContextList resultsCollection = stageContext.createContextList("geco_ResultsCollection", results.size());
		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();

				GenericContext resultContext = new GenericContext();
				resultsCollection.add(resultContext);

				resultContext.put("geco_ResultName", result.getIdentifier());
				resultContext.put("geco_NbFinishedRunners", result.nbFinishedRunners());
				resultContext.put("geco_NbPresentRunners", result.nbPresentRunners());
				if( isSingleCourseResult ) {
					resultContext.put("geco_CourseLength", result.anyCourse().getLength());
					resultContext.put("geco_CourseClimb", result.anyCourse().getClimb());
				}
				
				ContextList runnersCollection =
						resultContext.createContextList("geco_RankedRunners", result.getRanking().size());
				for (RankedRunner rankedRunner : result.getRanking()) {
					runnersCollection.add(RunnerContext.createRankedRunner(rankedRunner, bestTime));
				}

				runnersCollection =
						resultContext.createContextList("geco_UnrankedRunners", result.getUnrankedRunners().size());
				for (RunnerRaceData data : result.getUnrankedRunners()) {
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							runnersCollection.add(RunnerContext.createNCRunner(data));
						} // else nothing
					} else {
						runnersCollection.add(RunnerContext.createUnrankedRunner(data));
					}
				}
			}
		}
		return stageContext;
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
	
}
