/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.control.ResultBuilder.ResultConfig;
import net.geco.model.Messages;
import net.geco.model.Pool;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.iocsv.CsvWriter;


/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public abstract class AResultExporter extends Control {

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
		writer.write(generateHtmlResults(config, refreshInterval, true));
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
		CsvWriter writer = new CsvWriter(";", filename); //$NON-NLS-1$
		generateOECsvResult(config, writer);
		writer.close();
	}
	
	protected void exportXmlFile(String filename, ResultConfig config)
			throws Exception {
		if( config.resultType==ResultType.CategoryResult ) {
			try {
				generateXMLResult(config, filename);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new Exception(Messages.getString("AResultExporter.XmlCategoryExportWarning")); //$NON-NLS-1$
		}
	}

	public abstract String generateHtmlResults(ResultConfig config, int refreshDelay, boolean forFileExport);
	
	public void includeHeader(Html html, String cssfile, boolean forFileExport) {
		html.open("head").nl(); //$NON-NLS-1$
		if( forFileExport ){
			html.contents("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"); //$NON-NLS-1$
		}
		generateHtmlHeader(html);
		try {
			html.inlineCss(stage().filepath(cssfile));
		} catch (IOException e) {
			geco().debug(e.toString());
		}
		html.close("head"); //$NON-NLS-1$
	}
	
	protected void generateHtmlHeader(Html html) { }
	
	protected void emptyTr(Html html) {
		html.openTr("empty").td("&nbsp;").closeTr(); // jump line //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void generateCsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		Vector<Result> results = buildResults(config);
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendCsvResult(result, config, writer);
			}
		}
	}

	private void appendCsvResult(Result result, ResultConfig config, CsvWriter writer) throws IOException {
		String id = result.getIdentifier();
		// Format: result id, rank/status, first name, last name, club, [, time/status [, real time, nb mps]]
		for (RankedRunner rRunner : result.getRanking()) {
			RunnerRaceData runnerData = rRunner.getRunnerData();
			writeCsvResult(
					id,
					runnerData,
					Integer.toString(rRunner.getRank()),
					runnerData.getResult().formatRacetime(),
					config.showPenalties,
					writer);
		}
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				writeCsvResult(
						id,
						runnerData,
						runnerData.getResult().formatStatus(),
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						writer);
			} else if( config.showNC ) {
				writeCsvResult(
						id,
						runnerData,
						"NC", //$NON-NLS-1$
						runnerData.getResult().shortFormat(), // time or status
						config.showPenalties,
						writer);
			}
		}
		if( config.showOthers ) {
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				writeCsvResult(
						id,
						runnerData,
						runnerData.getResult().formatStatus(),
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						writer);
			}			
		}
	}
	
	protected abstract void writeCsvResult(String poolId, RunnerRaceData runnerData,
			String rankOrStatus, String timeOrStatus, boolean showPenalties, CsvWriter writer)
		throws IOException;

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