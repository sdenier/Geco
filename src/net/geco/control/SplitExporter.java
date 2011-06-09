/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.ResultBuilder.ResultConfig;
import net.geco.control.ResultBuilder.SplitTime;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;
import net.geco.model.iocsv.CsvWriter;


/**
 * @author Simon Denier
 * @since Oct 15, 2010
 *
 */
public class SplitExporter extends AResultExporter implements StageListener {
	
	private int nbColumns = 12;
	private int refreshInterval = 0;

	
	public SplitExporter(GecoControl gecoControl) {
		super(SplitExporter.class, gecoControl);
		geco().announcer().registerStageListener(this);
	}
	
	public int nbColumns() {
		return nbColumns;
	}
	
	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval) {
		Vector<Result> results = buildResults(config);
		this.refreshInterval = refreshInterval;
		Html html = new Html();
		includeHeader(html, "result.css"); //$NON-NLS-1$
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty() ) {
				Map<RunnerRaceData, SplitTime[]> allSplits = new HashMap<RunnerRaceData, SplitTime[]>(); 
				SplitTime[] bestSplit = resultBuilder.buildAllNormalSplits(result, config, allSplits);
				appendHtmlResultsWithSplits(result, allSplits, bestSplit, config, html);
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

	private void appendHtmlResultsWithSplits(Result result, Map<RunnerRaceData, SplitTime[]> allSplits,
													SplitTime[] bestSplit, ResultConfig config, Html html) {
		html.nl().tag("h2", "class=\"pool\"", result.getIdentifier()).nl(); //$NON-NLS-1$ //$NON-NLS-2$
		html.open("table").nl(); //$NON-NLS-1$
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			generateHtmlSplitsFor(
					data,
					Integer.toString(runner.getRank()),
					data.getResult().formatRacetime(),
					allSplits.get(data),
					bestSplit,
					html);
		}
		emptyTr(html);
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			if( ! runnerData.getRunner().isNC() ) {
				generateHtmlSplitsFor(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						allSplits.get(runnerData),
						bestSplit,
						html);
			} else if( config.showNC ) {
				generateHtmlSplitsFor(
						runnerData,
						Messages.getString("SplitExporter.NCLabel"), //$NON-NLS-1$
						runnerData.getResult().shortFormat(),
						allSplits.get(runnerData),
						bestSplit,
						html);
			}
		}
		if( config.showOthers ) {
			emptyTr(html);
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				generateHtmlSplitsFor(
						runnerData,
						"", //$NON-NLS-1$
						runnerData.getResult().formatStatus(),
						resultBuilder.buildNormalSplits(runnerData, null),
						bestSplit,
						html);
			}			
		}
		html.close("table").nl(); //$NON-NLS-1$
	}

	public void generateHtmlSplitsFor(RunnerRaceData data, String rank, String statusTime,
													SplitTime[] splits, SplitTime[] bestSplits, Html html) {
		html.openTr("rsplit"); //$NON-NLS-1$
		html.td(rank);
		html.td(data.getRunner().getName(), "colspan=\"4\""); //$NON-NLS-1$
		html.td(statusTime);
		html.closeTr();
		appendHtmlSplitsInColumns(splits, bestSplits, nbColumns(), html);
	}
	
	/**
	 * @param buildNormalSplits
	 * @param html
	 */
	protected void appendHtmlSplitsInColumns(SplitTime[] splits, SplitTime[] bestSplits, int nbColumns,
																								Html html) {
		int nbRows = (splits.length / nbColumns) + 1;
		int rowStart = 0;
		for (int i = 0; i < nbRows; i++) {
			// if last line, take the last remaining splits, not a full row
			int limit = ( i==nbRows-1 ) ? (splits.length % nbColumns) : nbColumns;
			
			if( limit==0 )
				break; // in case we have splits.length a multiple of nbColumns, we can stop now
			
			String trClass = ( i % 2 == 0 ) ? "col0" : "col1"; //$NON-NLS-1$ //$NON-NLS-2$
			// first line with seq and control number/code
			html.openTr(trClass).td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				String label = split.seq;
				if( split.trace != null ){
					label += " (" + split.trace.getBasicCode() +")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				html.td(label);
			}
			html.closeTr();
			// second line is cumulative split since start
			html.openTr(trClass).td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				int k = j + rowStart;
				SplitTime split = splits[k];
				String label = TimeManager.time(split.time);
				long best = 0;
				if( k < bestSplits.length ){
					best = bestSplits[k].time; 
				}
				showWithBestSplit(label, split.time, best, html);
			}
			html.closeTr();
			// third line is partial split since previous ok punch
			html.openTr(trClass).td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				int k = j + rowStart;
				SplitTime split = splits[k];
				String label = TimeManager.time(split.split);
				if( split.trace!=null && ! split.trace.isOK() ) {
					label = "&nbsp;"; //$NON-NLS-1$
				}
				long best = 0;
				if( k < bestSplits.length ){
					best = bestSplits[k].split;
				}
				showWithBestSplit(label, split.split, best, html);
			}
			html.closeTr();
			rowStart += nbColumns;
		}
	}
	
	private void showWithBestSplit(String label, long split, long best, Html html) {
		if( split==best ){
			html.td(label, "class=\"best\""); //$NON-NLS-1$
		} else {
			html.td(label);
		}
	}
	
	protected void appendHtmlSplitsInLine(SplitTime[] linearSplits, Html html) {
		for (SplitTime splitTime : linearSplits) {
			html.openTr("lin"); //$NON-NLS-1$
			Trace trace = splitTime.trace;
			String time = TimeManager.time(splitTime.time);
			if( trace!=null ) {
				html.td(splitTime.seq);
				html.td(splitTime.trace.getCode());
				String timeClass = "class=\""; //$NON-NLS-1$
				if( trace.isOK() ) {
					timeClass += "time"; //$NON-NLS-1$
				} else {
					if( trace.isAdded() || trace.isSubst() ) {
						timeClass += "add"; //$NON-NLS-1$
					} else {
						timeClass += "miss"; //$NON-NLS-1$
					}
				}
				html.td(time, timeClass + "\""); //$NON-NLS-1$
				html.td(TimeManager.time(splitTime.split), "class=\"sp\""); //$NON-NLS-1$
			} else {
				html.td(splitTime.seq);
				html.td(""); //$NON-NLS-1$
				html.td(time, "class=\"time\""); //$NON-NLS-1$
				html.td(TimeManager.time(splitTime.split), "class=\"sp\""); //$NON-NLS-1$
			}
			html.closeTr();
		}
	}
	
	@Override
	protected void writeCsvResult(String id, RunnerRaceData runnerData, String rankOrStatus,
			String timeOrStatus, boolean showPenalties, CsvWriter writer) throws IOException {
		Runner runner = runnerData.getRunner();
		Vector<String> csvData = new Vector<String>(
				Arrays.asList(new String[] {
					id,
					rankOrStatus,
					runner.getFirstname(),
					runner.getLastname(),
					runner.getClub().getName(),
					timeOrStatus,
					( showPenalties) ? TimeManager.time(runnerData.realRaceTime()) : "", //$NON-NLS-1$
					( showPenalties) ? Integer.toString(runnerData.getResult().getNbMPs()) : "", //$NON-NLS-1$
					TimeManager.fullTime(runnerData.getOfficialStarttime()),
					TimeManager.fullTime(runnerData.getFinishtime()),
					Integer.toString(runner.getCourse().nbControls())
				}));
		
		for (SplitTime split: resultBuilder.buildNormalSplits(runnerData, null)) {
			if( split.trace!=null ) { // finish split handled above
				csvData.add(split.trace.getBasicCode());
				csvData.add(TimeManager.fullTime(split.time));
			}
		}
		
		writer.writeRecord(csvData);
	}

	
	@Override
	public void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		generateOECsvResult(config, true, writer);
	}
	
	public void generateOECsvResult(ResultConfig config, boolean withSplits, CsvWriter writer)
																						throws IOException {
		writer.write("N° dép.;Puce;Ident. base de données;Nom;Prénom;Né;S;Plage;nc;Départ;Arrivée;Temps;"); //$NON-NLS-1$
		writer.write("Evaluation;N° club;Nom;Ville;Nat;N° cat.;Court;Long;Num1;Num2;Num3;Text1;Text2;Text3;"); //$NON-NLS-1$
		writer.write("Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée;Engagement;Payé;"); //$NON-NLS-1$
		writer.write("Circuit N°;Circuit;km;m;Postes du circuit;Pl"); //$NON-NLS-1$
		writer.write("\n"); //$NON-NLS-1$
		
		List<String> clubnames = registry().getClubNames();
		List<String> categorynames = registry().getCategoryNames();
		List<String> coursenames = registry().getCourseNames();
		
		for (RunnerRaceData runnerData : registry().getRunnersData()) {
			Runner runner = runnerData.getRunner();
			if( runnerData.hasResult() ) {
				Club club = runner.getClub();
				Category category = runner.getCategory();
				Course course = runner.getCourse();
				
				Collection<String> record = saveRecord(
						runner.getStartId().toString(),
						runner.getEcard(),
						( runner.getArchiveId()!=null )? runner.getArchiveId().toString() : "", //$NON-NLS-1$
						runner.getLastname(),
						runner.getFirstname(),
						"", //$NON-NLS-1$ // ark.getBirthYear(),
						"", //$NON-NLS-1$ // ark.getSex(),
						"", //$NON-NLS-1$
						( runner.isNC() ) ? "X" : "0", //$NON-NLS-1$ //$NON-NLS-2$
						oeTime(runnerData.getOfficialStarttime()),
						oeTime(runnerData.getFinishtime()),
						oeTime(new Date(runnerData.getResult().getRacetime())),
						oeEvaluationCode(runnerData.getStatus()),
						Integer.toString(clubnames.indexOf(club.getName())),
						club.getShortname(),
						club.getName(),
						"", //$NON-NLS-1$
						Integer.toString(categorynames.indexOf(category.getName())),
						category.getShortname(),
						category.getLongname(),
						"", "", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"", "", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"", "", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"", "", "",  	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"0", //$NON-NLS-1$
						"0", //$NON-NLS-1$
						"0", //$NON-NLS-1$
						Integer.toString(coursenames.indexOf(course.getName())),
						course.getName(),
						Integer.toString(course.getLength()),
						Integer.toString(course.getClimb())
						);
				if( withSplits ) {
					addSplits(runnerData, record);
				} else {
					record.add("1"); //$NON-NLS-1$
					record.add("1"); //$NON-NLS-1$
				}
				writer.writeRecord(record);
			}
		}
	}
	
	private void addSplits(RunnerRaceData runnerData, Collection<String> record) {
		record.add(Integer.toString(runnerData.getCourse().nbControls()));
		record.add("1"); //$NON-NLS-1$
		record.add(oeTime(runnerData.getOfficialStarttime()));
		record.add(oeTime(runnerData.getFinishtime()));
		SplitTime[] splits = resultBuilder.buildNormalSplits(runnerData, null);
		for (SplitTime split : splits) {
			if( split.trace!=null ) { // finish split handled above
				record.add(split.trace.getBasicCode());
				record.add(oeSplit(split.time));
			}
		}
	}

	private Collection<String> saveRecord(String... records) {
		ArrayList<String> record = new ArrayList<String>(44);
		for (String r : records) {
			record.add(r);
		}
		return record;
	}
	
	private String oeTime(Date time) {
		if( time.equals(TimeManager.NO_TIME) ) {
			return ""; //$NON-NLS-1$
		} else {
			return TimeManager.fullTime(time);
		}
	}

	private String oeSplit(long time) {
		if( time==TimeManager.NO_TIME_l ) {
			return "-----"; //$NON-NLS-1$
		} else {
			return TimeManager.fullTime(time);
		}
	}

	private String oeEvaluationCode(Status status) {
		if( status==Status.OK ) {
			return "0"; //$NON-NLS-1$
		} else {
			return "1"; //$NON-NLS-1$
		}
	}
	

	@Override
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		String nbCol = props.getProperty(splitNbColumnsProperty());
		if( nbCol!=null ){
			nbColumns = Integer.parseInt(nbCol);
		}
	}
	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitNbColumnsProperty(), Integer.toString(nbColumns));
	}
	@Override
	public void closing(Stage stage) {	}

	public static String splitNbColumnsProperty() {
		return "SplitNbColumns"; //$NON-NLS-1$
	}

}
