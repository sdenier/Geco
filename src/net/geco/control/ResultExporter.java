/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.IOException;
import java.util.Vector;

import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.ResultBuilder.ResultConfig;
import net.geco.model.Messages;
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
public class ResultExporter extends AResultExporter {
	
	private int refreshInterval;

	public ResultExporter(GecoControl gecoControl) {
		super(ResultExporter.class, gecoControl);
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval, boolean forFileExport) {
		Vector<Result> results = buildResults(config);
		this.refreshInterval = refreshInterval;
		Html html = new Html();
		includeHeader(html, "result.css", forFileExport); //$NON-NLS-1$
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendHtmlResult(result, config, html);	
			}
		}
		return html.close();
	}
	
	protected void generateHtmlHeader(Html html) {
		if( refreshInterval>0 ) {
			html.contents("<meta http-equiv=\"refresh\" content=\"" //$NON-NLS-1$
					+ refreshInterval + "\" />"); //$NON-NLS-1$
		}
	}

	/**
	 * @param result
	 * @param html
	 */
	private void appendHtmlResult(Result result, ResultConfig config, Html html) {
		// compute basic stats
		StringBuilder resultLabel = new StringBuilder(result.getIdentifier());
		int finished = result.getRanking().size() + result.getNRRunners().size();
		int present = finished;
		for (RunnerRaceData other : result.getOtherRunners()) {
			if( other.getResult().getStatus().isUnresolved() ) {
				present++;
			}
		}
		resultLabel.append(" (").append(Integer.toString(finished)).append("/") //$NON-NLS-1$ //$NON-NLS-2$
					.append(Integer.toString(present)).append(")"); //$NON-NLS-1$
		html.nl().tag("h2", "class=\"pool\"", resultLabel.toString()).nl(); //$NON-NLS-1$ //$NON-NLS-2$
		
		html.open("table").nl(); //$NON-NLS-1$
		if( config.showPenalties ){
			html.openTr("runner") //$NON-NLS-1$
				.th("") //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.NameHeader")) //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.ClubHeader")) //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.CategoryHeader")) //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.TimeHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.MPHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.RacetimeHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.closeTr();
		}
		// Format: rank, first name + last name, club [, real time, nb mps], time/status
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			writeHtml(
					data,
					Integer.toString(runner.getRank()),
					data.getResult().formatRacetime(),
					config.showPenalties,
					html);
		}
		emptyTr(html);
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				writeHtml(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						html);
			} else if( config.showNC ) {
				writeHtml(
						runnerData,
						"NC", //$NON-NLS-1$
						runnerData.getResult().shortFormat(),
						config.showPenalties,
						html);
			}
		}
		if( config.showOthers ) {
			emptyTr(html);
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				writeHtml(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						html);
			}			
		}
		html.close("table").nl(); //$NON-NLS-1$
	}
	
	private void writeHtml(RunnerRaceData runnerData, String rank, String timeOrStatus,
																		boolean showPenalties, Html html) {
		html.openTr("runner"); //$NON-NLS-1$
		html.td(rank);
		html.td(runnerData.getRunner().getName());
		html.td(runnerData.getRunner().getClub().getName());
		html.td(runnerData.getRunner().getCategory().getName());
		html.td(timeOrStatus, "class=\"time\""); //$NON-NLS-1$
		if( showPenalties ){
			html.td(Integer.toString(runnerData.getResult().getNbMPs()), "class=\"right\""); //$NON-NLS-1$
			html.td(TimeManager.time(runnerData.realRaceTime()), "class=\"right\""); //$NON-NLS-1$
		}
		html.closeTr();
	}


	@Override
	protected void writeCsvResult(String id, RunnerRaceData runnerData,
						String rankOrStatus, String timeOrStatus, boolean showPenalties, CsvWriter writer)
			throws IOException {
		Runner runner = runnerData.getRunner();
		if( showPenalties ){
			writer.writeRecord(
				id,
				rankOrStatus,
				runner.getFirstname(),
				runner.getLastname(),
				runner.getClub().getName(),
				timeOrStatus,
				TimeManager.time(runnerData.realRaceTime()),
				Integer.toString(runnerData.getResult().getNbMPs()));
		} else {
			writer.writeRecord(
				id,
				rankOrStatus,
				runner.getFirstname(),
				runner.getLastname(),
				runner.getClub().getName(),
				timeOrStatus);				
		}
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
