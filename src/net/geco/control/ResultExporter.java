/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.ResultBuilder.ResultConfig;
import net.geco.model.Messages;
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
public class ResultExporter extends AResultExporter {
	
	private int refreshInterval;

	public ResultExporter(GecoControl gecoControl) {
		super(ResultExporter.class, gecoControl);
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval, OutputType outputType) {
		Vector<Result> results = buildResults(config);
		this.refreshInterval = refreshInterval;
		Html html = new Html();
		includeHeader(html, "result.css", outputType); //$NON-NLS-1$
		if( outputType != OutputType.DISPLAY ) {
			html.nl().tag("h1", stage().getName() + " - " + "Results");
		}
		String timestamp = null;
		if( outputType == OutputType.PRINTER ) {
			SimpleDateFormat tsFormat = new SimpleDateFormat("H:mm"); //$NON-NLS-1$
			timestamp = tsFormat.format(new Date());
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendHtmlResult(result, config, html, timestamp);
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
	 * @param appendTimestamp 
	 */
	private void appendHtmlResult(Result result, ResultConfig config, Html html, String timestamp) {
		boolean paceComputable = ! result.isEmpty()
								&& ! config.resultType.equals(ResultType.CategoryResult)
								&& result.anyCourse().hasDistance();
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
		if( paceComputable ){			
			resultLabel.append(" - ").append(result.anyRunner().getCourse().formatDistanceClimb());
		}
		html.nl().tag("h2", resultLabel.toString()).nl(); //$NON-NLS-1$
		
		html.open("table").nl(); //$NON-NLS-1$
		html.openTr("runner") //$NON-NLS-1$
			.th("") //$NON-NLS-1$
			.th(Messages.getString("ResultBuilder.NameHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultBuilder.ClubHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultBuilder.CategoryHeader"), "class=\"left\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th(Messages.getString("ResultBuilder.TimeHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
			.th("Diff", "class=\"right\"");
		if( paceComputable ){
			html.th("min/km", "class=\"right\"");
		}
		if( config.showPenalties ){
			html.th(Messages.getString("ResultBuilder.MPHeader"), "class=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.RacetimeHeader"), "class=\"right\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		html.closeTr();
		// Format: rank, first name + last name, club, cat, time/status, diff, pace [, real time, nb mps]
		long bestTime = result.bestTime();
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			writeHtml(
					data,
					Integer.toString(runner.getRank()),
					data.getResult().formatRacetime(),
					runner.formatDiffTime(bestTime),
					(paceComputable ? data.formatPace() : ""), //$NON-NLS-1$
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
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						config.showPenalties,
						html);
			} else if( config.showNC ) {
				writeHtml(
						runnerData,
						"NC", //$NON-NLS-1$
						runnerData.getResult().shortFormat(),
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
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
						"", //$NON-NLS-1$
						"", //$NON-NLS-1$
						config.showPenalties,
						html);
			}			
		}
		html.close("table").nl(); //$NON-NLS-1$
		if( timestamp != null ) {
			html.nl().tag("p", "Last update " + timestamp); //$NON-NLS-1$
		}
	}
	
	private void writeHtml(RunnerRaceData runnerData, String rank, String timeOrStatus, String diffTime,
															String pace, boolean showPenalties, Html html) {
		html.openTr("runner"); //$NON-NLS-1$
		html.td(rank);
		html.td(runnerData.getRunner().getName());
		html.td(runnerData.getRunner().getClub().getName());
		html.td(runnerData.getRunner().getCategory().getName());
		html.td(timeOrStatus, "class=\"time\""); //$NON-NLS-1$
		html.td(diffTime, "class=\"diff\""); //$NON-NLS-1$
		html.td(pace, "class=\"pace\""); //$NON-NLS-1$
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
