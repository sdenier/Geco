/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.ResultBuilder.ResultConfig;
import net.geco.model.Pool;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.iocsv.CsvWriter;


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

	protected AResultExporter(Class<? extends Control> clazz, GecoControl gecoControl) {
		super(clazz, gecoControl);
		resultBuilder = getService(ResultBuilder.class);
	}

	public void exportFile(String filename, String format, ResultConfig config, int refreshInterval)
			throws Exception {
				if( !filename.endsWith(format) ) {
					filename = filename + "." + format; //$NON-NLS-1$
				}
				if( format.equals("html") ) { //$NON-NLS-1$
					exportHtmlFile(filename, config, refreshInterval);
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
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		writer.write(generateHtmlResults(config, refreshInterval, OutputType.FILE));
		writer.close();
	}

	protected void exportCsvFile(String filename, ResultConfig config)
			throws IOException {
		CsvWriter writer = new CsvWriter(";", filename); //$NON-NLS-1$
		generateCsvResult(config, writer);
		writer.close();
	}

	protected void exportOECsvFile(String filename, ResultConfig config)
			throws IOException {
		CsvWriter writer = new CsvWriter(";").initialize(filename); //$NON-NLS-1$
		writer.open(Charset.forName("windows-1252")); //$NON-NLS-1$
		generateOECsvResult(config, writer);
		writer.close();
	}
	
	protected void exportXmlFile(String filename, ResultConfig config)
			throws Exception {
		generateXMLResult(config, filename);
	}

	public abstract String generateHtmlResults(ResultConfig config, int refreshDelay, OutputType outputType);
	
	public void includeHeader(Html html, String cssfile, OutputType outputType) {
		html.open("head").nl(); //$NON-NLS-1$
		if( outputType == OutputType.FILE ){
			html.contents("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"); //$NON-NLS-1$
		}
		generateHtmlHeader(html);
		html.inlineCss(stage().filepath(cssfile));
		html.close("head"); //$NON-NLS-1$
	}
	
	protected void generateHtmlHeader(Html html) { }
	
	protected void emptyTr(Html html) {
		html.openTr("empty").td("&nbsp;").closeTr(); // jump line //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void generateCsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		Vector<Result> results = buildResults(config);
		writer.write("start id;ecard;archive id;last name;first name;short cat;long cat;short club;long club;" + //$NON-NLS-1$
				"result id;rank;status;official time;nc;start time;finish time;race time;mps;" + //$NON-NLS-1$
				"course;distance;climb;nb punches;control 1;punch 1;...;\n"); //$NON-NLS-1$
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
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
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() || config.showNC ) {
				writeCsvResult(runnerData, resultId, "", writer); //$NON-NLS-1$
			}
		}
		if( config.showOthers ) {
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
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
				runnerData.getResult().formatRacetime(),
				(runner.isNC()) ? "NC" : "", //$NON-NLS-1$ //$NON-NLS-2$
				TimeManager.fullTime(runnerData.getOfficialStarttime()),
				TimeManager.fullTime(runnerData.getFinishtime()),
				TimeManager.time(runnerData.realRaceTime()),
				Integer.toString(runnerData.getResult().getNbMPs()),
				runner.getCourse().getName(),
				Integer.toString(runner.getCourse().getLength()),
				Integer.toString(runner.getCourse().getClimb()),
				Integer.toString(runner.getCourse().nbControls()),
		});
	}

	public abstract void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException;

	public abstract void generateXMLResult(ResultConfig config, String filename) throws Exception ;
	
	protected Vector<Result> buildResults(ResultConfig config) {
		Vector<Pool> pools = new Vector<Pool>();
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
	
}