/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
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
import net.geco.model.Stage;

import com.samskivert.mustache.Mustache;

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
	protected void exportHtmlFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		BufferedReader template = GecoResources.getSafeReaderFor(getRankingTemplate().getAbsolutePath());
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		buildHtmlResults(template, config, refreshInterval, writer, OutputType.FILE);
		writer.close();
		template.close();
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval, OutputType outputType) {
		Reader reader;
		StringWriter out = new StringWriter();
		try {
			switch (outputType) {
			case DISPLAY:
				// TODO I18N template headers
				reader = GecoResources.getResourceReader("/resources/formats/results_ranking_internal.mustache");
				break;
			case PRINTER:
			default:
				reader = GecoResources.getSafeReaderFor(getRankingTemplate().getAbsolutePath());
			}
			buildHtmlResults(reader, config, refreshInterval, out, outputType);
			reader.close();
		} catch (IOException e) {
			geco().logger().debug(e);
		}
		return out.toString();
	}

	protected void buildHtmlResults(Reader template, ResultConfig config, int refreshInterval,
			Writer out, OutputType outputType) {
		// TODO: lazy cache of template
		Mustache.compiler()
			.defaultValue("N/A")
			.compile(template)
			.execute(buildDataContext(config, refreshInterval, outputType), out);
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

	public File getRankingTemplate() {
		return rankingTemplate;
	}

	public void setRankingTemplate(File selectedFile) {
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
