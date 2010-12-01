/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.IOException;
import java.util.Vector;

import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Messages;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.iocsv.CsvWriter;

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
	public String generateHtmlResults(ResultConfig config, int refreshInterval) {
		Vector<Result> results = buildResults(config);
		Html html = new Html();
		if( refreshInterval>0 ) {
			html.open("head"); //$NON-NLS-1$
			html.contents("<meta http-equiv=\"refresh\" content=\"" //$NON-NLS-1$
							+ refreshInterval + "\" />"); //$NON-NLS-1$
			html.close("head"); //$NON-NLS-1$
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendHtmlResult(result, config, html);	
			}
		}
		return html.close();
	}

	/**
	 * @param result
	 * @param html
	 */
	private void appendHtmlResult(Result result, ResultConfig config, Html html) {
		// compute basic stats
		StringBuffer resultLabel = new StringBuffer(result.getIdentifier());
		int finished = result.getRanking().size() + result.getNRRunners().size();
		int present = finished;
		for (RunnerRaceData other : result.getOtherRunners()) {
			if( other.getResult().getStatus().isUnresolved() ) {
				present++;
			}
		}
		resultLabel.append(" (").append(Integer.toString(finished)).append("/") //$NON-NLS-1$ //$NON-NLS-2$
					.append(Integer.toString(present)).append(")"); //$NON-NLS-1$
		html.tag("h1", resultLabel.toString()); //$NON-NLS-1$
		
		html.open("table"); //$NON-NLS-1$
		if( config.showPenalties ){
			html.open("tr") //$NON-NLS-1$
				.th("") //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.NameHeader")) //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.ClubHeader")) //$NON-NLS-1$
				.th(Messages.getString("ResultBuilder.TimeHeader"), "align=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.MPHeader"), "align=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
				.th(Messages.getString("ResultBuilder.RacetimeHeader"), "align=\"right\"") //$NON-NLS-1$ //$NON-NLS-2$
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
		html.openTr().closeTr(); // jump line
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
			html.openTr().closeTr(); // jump line
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				writeHtml(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						config.showPenalties,
						html);
			}			
		}
		html.close("table"); //$NON-NLS-1$
	}
	
	private void writeHtml(RunnerRaceData runnerData, String rank, String timeOrStatus,
																		boolean showPenalties, Html html) {
		html.openTr();
		html.td(rank);
		html.td(runnerData.getRunner().getName());
		html.td(runnerData.getRunner().getClub().getName());
		html.th(timeOrStatus, "align=\"right\""); //$NON-NLS-1$
		if( showPenalties ){
			html.td(Integer.toString(runnerData.getResult().getNbMPs()), "align=\"right\""); //$NON-NLS-1$
			html.td(TimeManager.time(runnerData.realRaceTime()), "align=\"right\""); //$NON-NLS-1$
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

}
