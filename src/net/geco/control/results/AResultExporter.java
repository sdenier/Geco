/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.basics.TimeManager;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.context.GenericContext;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.model.Messages;
import net.geco.model.Pool;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;


/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public abstract class AResultExporter extends Control {

	public enum OutputType {
		DISPLAY, FILE, PRINTER
	}
	
	protected final ResultBuilder resultBuilder;

	protected Map<String, Template> templates;
	
	protected AResultExporter(Class<? extends Control> clazz, GecoControl gecoControl) {
		super(clazz, gecoControl);
		resultBuilder = getService(ResultBuilder.class);
		templates = new HashMap<String, Template>(2);
	}

	protected List<Result> buildResults(ResultConfig config) {
		List<Pool> pools = new ArrayList<Pool>();
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
		return resultBuilder.buildResults(pools.toArray(new Pool[0]), config.resultType);
	}

	public void exportFile(String filename, String format, ResultConfig config, int refreshInterval)
			throws Exception {
		if( filename.indexOf(".") == -1 ) {
			filename = filename + "." + format; //$NON-NLS-1$
		}
		if( format.equals("html") ) { //$NON-NLS-1$
			exportHtmlFile(filename, config, refreshInterval);
		}
		if( format.equals("custom") ) { //$NON-NLS-1$
			exportCustomFile(filename, config, refreshInterval);
		}
		if( format.equals("csv") ) { //$NON-NLS-1$
			exportCsvFile(filename, config);
		}
		if( format.equals("oe.csv") ) { //$NON-NLS-1$
			exportOECsvFile(filename, config);
		}
		if( format.equals("xml") ) { //$NON-NLS-1$
			exportXmlFile(filename, config);
		}
	}

	protected void exportHtmlFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		Template template = getExternalTemplate();
		Writer writer = GecoResources.getSafeWriterFor(filename);
		buildHtmlResults(template, config, refreshInterval, writer, OutputType.FILE);
		writer.close();
	}

	public String generateHtmlResults(ResultConfig config, int refreshInterval, OutputType outputType) {
		StringWriter out = new StringWriter();
		try {
			Template template;
			switch (outputType) {
			case DISPLAY:
				template = getInternalTemplate();
				break;
			case PRINTER:
			default:
				template = getExternalTemplate();
			}
			buildHtmlResults(template, config, refreshInterval, out, outputType);
		} catch (IOException e) {
			geco().logger().debug(e);
		}
		return out.toString();
	}

	protected void buildHtmlResults(Template template, ResultConfig config, int refreshInterval,
			Writer out, OutputType outputType) {
		template.execute(buildDataContext(config, refreshInterval, outputType), out);
	}

	protected Template getInternalTemplate() throws IOException {
		return getInternalTemplate(getInternalTemplatePath());
	}
	
	protected Template getInternalTemplate(String templatePath) throws IOException {
		Template template = templates.get(templatePath);
		if( template==null ) {
			Reader templateReader = GecoResources.getResourceReader(templatePath);
			template = loadTemplate(templateReader, templatePath);
			templateReader.close();
		}
		return template;
	}

	protected Template getExternalTemplate() throws IOException {
		return getExternalTemplate(getExternalTemplatePath());
	}

	protected Template getExternalTemplate(String templatePath) throws IOException {
		Template template = templates.get(templatePath);
		if( template == null ) {
			Reader templateReader = GecoResources.getSafeReaderFor(templatePath);
			template = loadTemplate(templateReader, templatePath);
			templateReader.close();
		}
		return template;
	}
	
	protected Template loadTemplate(Reader templateReader, String templatePath) {
		Template template = Mustache.compiler().defaultValue("N/A").compile(templateReader); //$NON-NLS-1$
		templates.put(templatePath, template);
		return template;
	}
	
	public void resetTemplate(String templatePath) {
		templates.remove(templatePath);
	}

	protected abstract String getInternalTemplatePath();

	protected abstract String getExternalTemplatePath();

	protected abstract GenericContext buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType);

	protected void exportCustomFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		Template template = getInternalTemplate("/resources/formats/results_osplits.mustache");
		Writer writer = GecoResources.getSafeWriterFor(filename);
		buildCustomResults(template, config, refreshInterval, writer, OutputType.FILE);
		writer.close();
	}
	
	protected void buildCustomResults(Template template, ResultConfig config, int refreshInterval,
			Writer writer, OutputType outputType) {
		template.execute(buildCustomContext(config, refreshInterval, outputType), writer);
	}

	protected abstract GenericContext buildCustomContext(ResultConfig config, int refreshInterval, OutputType outputType);

	protected void mergeI18nProperties(GenericContext stageCtx) {
		stageCtx.put("i18n_RankingTitle", Messages.getString("ResultExporter.ResultsOutputTitle")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_SplitsTitle", Messages.getString("SplitExporter.SplitsOutputTitle")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_CNTitle", Messages.getString("CNCalculator.CNOutputTitle")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_NameHeader", Messages.getString("ResultBuilder.NameHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_ClubHeader", Messages.getString("ResultBuilder.ClubHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_CategoryHeader", Messages.getString("ResultBuilder.CategoryHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_TimeHeader", Messages.getString("ResultBuilder.TimeHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_DiffHeader", Messages.getString("ResultExporter.DiffHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_PaceHeader", Messages.getString("ResultExporter.minkmLabel")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_MPHeader", Messages.getString("ResultBuilder.MPHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_RacetimeHeader", Messages.getString("ResultBuilder.RacetimeHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_LastUpdateLabel", Messages.getString("ResultExporter.LastUpdateLabel")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_CNHeader", Messages.getString("CNCalculator.CNHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_ScoreHeader", Messages.getString("CNCalculator.ScoreHeader")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void mergeCustomStageProperties(GenericContext context) {
		final String customPropertiesPath = stage().filepath("formats.prop"); //$NON-NLS-1$
		if( GecoResources.exists(customPropertiesPath) ) {
			Properties props = new Properties();
			try {
				props.load( GecoResources.getSafeReaderFor(customPropertiesPath) );
				context.mergeProperties(props);
			} catch (IOException e) {
				geco().logger().debug(e);
			}
		}
	}

	protected void exportCsvFile(String filename, ResultConfig config)
			throws IOException {
		CsvWriter writer = new CsvWriter(";", filename); //$NON-NLS-1$
		generateCsvResult(config, writer);
		writer.close();
	}

	public void generateCsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		List<Result> results = buildResults(config);
		writer.write("start id;ecard;archive id;last name;first name;short cat;long cat;short club;long club;" + //$NON-NLS-1$
				"result id;rank;status;official time;nc;start time;finish time;race time;mps;" + //$NON-NLS-1$
				"course;distance;climb;nb punches;control 1;punch 1;...;\n"); //$NON-NLS-1$
		for (Result result : results) {
			if( ! result.isEmpty() ){
				appendCsvResult(result, config, writer);
			}
		}
	}

	private void appendCsvResult(Result result, ResultConfig config, CsvWriter writer) throws IOException {
		String resultId = result.getIdentifier();
		for (RankedRunner rRunner : result.getRanking()) {
			RunnerRaceData runnerData = rRunner.getRunnerData();
			writeCsvResult(runnerData, resultId, Integer.toString(rRunner.getRank()), writer);
		}
		for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() || config.showNC ) {
				writeCsvResult(runnerData, resultId, "", writer); //$NON-NLS-1$
			}
		}
	}
	
	
	private void writeCsvResult(RunnerRaceData runnerData, String resultId, String rank, CsvWriter writer)
			throws IOException {
		writer.writeRecord(computeCsvRecord(runnerData, resultId, rank));
	}

	protected Collection<String> computeCsvRecord(RunnerRaceData runnerData, String resultId, String rank) {
//		start id;ecard;archive id;last name;first name;short cat;long cat;short club;long club;
//		result id;rank;status;official time;nc;start time;finish time;race time;mps;
//		course;distance;climb;nb punches;control 1;punch 1
		Runner runner = runnerData.getRunner();
		return Arrays.asList(new String[] {
				runner.getStartId().toString(),
				runner.getEcard(),
				(runner.getArchiveId()==null) ? "" : runner.getArchiveId().toString(), //$NON-NLS-1$
				runner.getLastname(),
				runner.getFirstname(),
				runner.getCategory().getShortname(),
				runner.getCategory().getLongname(),
				runner.getClub().getShortname(),
				runner.getClub().getName(),
				resultId,
				rank,
				runnerData.getResult().formatStatus(),
				TimeManager.fullTime(runnerData.getResult().getRacetime()),
				(runner.isNC()) ? "NC" : "", //$NON-NLS-1$ //$NON-NLS-2$
				TimeManager.fullTime(runnerData.getOfficialStarttime()),
				TimeManager.fullTime(runnerData.getFinishtime()),
				TimeManager.fullTime(runnerData.realRaceTime()),
				Integer.toString(runnerData.getResult().getNbMPs()),
				runner.getCourse().getName(),
				Integer.toString(runner.getCourse().getLength()),
				Integer.toString(runner.getCourse().getClimb()),
				Integer.toString(runner.getCourse().nbControls()),
		});
	}

	protected void exportOECsvFile(String filename, ResultConfig config)
			throws IOException {
		CsvWriter writer = new CsvWriter(";").initialize(filename); //$NON-NLS-1$
		writer.open(Charset.forName("windows-1252")); //$NON-NLS-1$
		generateOECsvResult(config, writer);
		writer.close();
	}

	public abstract void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException;
	
	protected void exportXmlFile(String filename, ResultConfig config)
			throws Exception {
		generateXMLResult(config, filename);
	}

	public abstract void generateXMLResult(ResultConfig config, String filename) throws Exception ;
	
}