/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JTextPane;

import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Announcer.CardListener;
import valmo.geco.core.Announcer.StageListener;
import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Trace;
import valmo.geco.model.iocsv.CsvWriter;

/**
 * @author Simon Denier
 * @since Oct 15, 2010
 *
 */
public class SplitBuilder extends Control implements IResultBuilder, StageListener, CardListener {
	
	public static class SplitTime {
		public SplitTime(String seq, Trace trace, long time, long split) {
			this.seq = seq;
			this.trace = trace;
			this.time = time;
			this.split = split;
		}
		private String seq;
		private Trace trace;
		private long time;
		private long split;
		
	}
	
	public static enum SplitFormat { MultiColumns, Ticket }
	
	
	private PrintService splitPrinter;
	
	private boolean autoPrint;
	
	private int nbColumns = 10;

	private SplitFormat splitFormat = SplitFormat.MultiColumns;

	
	/**
	 * @param gecoControl
	 */
	public SplitBuilder(GecoControl gecoControl) {
		super(gecoControl);
		geco().announcer().registerStageListener(this);
		geco().announcer().registerCardListener(this);
		geco().registerService(SplitBuilder.class, this);
		changed(null, stage());
	}
	
	
	public SplitTime[] buildNormalSplits(RunnerRaceData data) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		ArrayList<SplitTime> added = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in normal mode, added splits appear after normal splits
		buildSplits(data, splits, added, true);
		splits.addAll(added);
		return splits.toArray(new SplitTime[0]);
	}	
	
	public SplitTime[] buildLinearSplits(RunnerRaceData data) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in linear mode, added splits are kept in place with others
		buildSplits(data, splits, splits, false);
		return splits.toArray(new SplitTime[0]);
	}

	private void buildSplits(RunnerRaceData data, List<SplitTime> splits, List<SplitTime> added, boolean cutSubst) {
		long startTime = data.getStarttime().getTime();
		long previousTime = startTime;
		int control = 1;
		for (Trace trace : data.getResult().getTrace()) {
			long time = trace.getTime().getTime();
			if( trace.isOK() ) {
				splits.add(createSplit(Integer.toString(control), trace, startTime, previousTime, time));
				previousTime = time;
				control++;
			} else if( trace.isSubst() ) {
				if( cutSubst ) {
					String code = trace.getCode();
					int cut = code.indexOf("+"); //$NON-NLS-1$
					Trace mpTrace = factory().createTrace(code.substring(0, cut), TimeManager.NO_TIME);
					splits.add(createSplit(Integer.toString(control), mpTrace, startTime, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l));
					Trace addedTrace = factory().createTrace(code.substring(cut), trace.getTime());
					added.add(createSplit("", addedTrace, startTime, TimeManager.NO_TIME_l, time)); //$NON-NLS-1$
				} else {
					splits.add(createSplit(Integer.toString(control), trace, startTime, TimeManager.NO_TIME_l, time));
				}
				control++;
			} else if( trace.isMP() ) {
				splits.add(createSplit(Integer.toString(control), trace, startTime, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l));
				control++;
			} else { // added trace
				added.add(createSplit("", trace, startTime, TimeManager.NO_TIME_l, time)); //$NON-NLS-1$
			}
		} // TODO: remove the null value and consequent checks
		splits.add(createSplit("F", null, startTime, previousTime, data.getFinishtime().getTime())); //$NON-NLS-1$
	}

	private SplitTime createSplit(String seq, Trace trace, long startTime, long previousTime, long time) {
		return new SplitTime(seq, trace, computeSplit(startTime, time), computeSplit(previousTime, time));
	}
	private long computeSplit(long baseTime, long time) {
		if( baseTime==TimeManager.NO_TIME_l || time==TimeManager.NO_TIME_l ) {
			return TimeManager.NO_TIME_l;
		} else {
			if( baseTime < time ) {
				return time - baseTime;
			} else {
				return TimeManager.NO_TIME_l;
			}
		}		
	}

	
	
	private ResultBuilder resultBuilder() {
		return geco().getService(ResultBuilder.class);
	}
	

	@Override
	public void exportFile(String filename, String format, ResultConfig config, int refreshInterval)
			throws IOException {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format; //$NON-NLS-1$
		}
		if( format.equals("html") ) { //$NON-NLS-1$
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(generateHtmlResults(config, refreshInterval));
			writer.close();
		}
		if( format.equals("csv") ) { //$NON-NLS-1$
			CsvWriter writer = new CsvWriter(";", filename); //$NON-NLS-1$
			generateCsvResult(config, writer);
			writer.close();
		}
		if( format.equals("cn.csv") ) { // delegate //$NON-NLS-1$
			resultBuilder().exportFile(filename, format, config, refreshInterval);
		}
	}
	
	public void generateCsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		Vector<Result> results = resultBuilder().buildResults(config);
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendCsvResult(result, config, writer);
			}
		}
	}
	
	private void appendCsvResult(Result result, ResultConfig config, CsvWriter writer) throws IOException {
		String id = result.getIdentifier();

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

	private void writeCsvResult(String id, RunnerRaceData runnerData, String rankOrStatus, String timeOrStatus,
			boolean showPenalties, CsvWriter writer) throws IOException {
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
					TimeManager.fullTime(runnerData.getStarttime()),
					TimeManager.fullTime(runnerData.getFinishtime()),
					Integer.toString(runner.getCourse().nbControls())
				}));
		
		for (SplitTime split: buildNormalSplits(runnerData)) {
			if( split.trace!=null ) { // finish split handled above
				csvData.add(split.trace.getBasicCode());
				csvData.add(TimeManager.fullTime(split.time));
			}
		}
		
		writer.writeRecord(csvData.toArray(new String[0]));
	}


	
	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval) {
		Vector<Result> results = resultBuilder().buildResults(config);
		Html html = new Html();
		if( refreshInterval>0 ) {
			html.open("head"); //$NON-NLS-1$
			html.contents("<meta http-equiv=\"refresh\" content=\"" + refreshInterval + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
			html.close("head"); //$NON-NLS-1$
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty() ) {
				appendHtmlResultsWithSplits(result, config, html);
			}
		}
		return html.close();
	}

	private void appendHtmlResultsWithSplits(Result result, ResultConfig config, Html html) {
		html.tag("h1", result.getIdentifier()); //$NON-NLS-1$
		html.open("table"); //$NON-NLS-1$
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			generateHtmlSplitsFor(data, Integer.toString(runner.getRank()), data.getResult().formatRacetime(), config, html);
			html.openTr().closeTr();
		}
		html.openTr().closeTr();
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			if( ! runnerData.getRunner().isNC() ) {
				generateHtmlSplitsFor(runnerData, "", runnerData.getResult().formatStatus(), config, html); //$NON-NLS-1$
			} else if( config.showNC ) {
				generateHtmlSplitsFor(runnerData, "NC", runnerData.getResult().shortFormat(), config, html); //$NON-NLS-1$
			}
			html.openTr().closeTr();
		}
		if( config.showOthers ) {
			html.openTr().closeTr();
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				generateHtmlSplitsFor(runnerData, "", runnerData.getResult().formatStatus(), config, html); //$NON-NLS-1$
				html.openTr().closeTr();
			}			
		}
		html.close("table"); //$NON-NLS-1$
	}

	public void generateHtmlSplitsFor(RunnerRaceData data, String rank, String statusTime, ResultConfig config, Html html) {
		html.openTr();
		html.th(rank);
		html.th(data.getRunner().getName(), "align=\"left\" colspan=\"3\""); //$NON-NLS-1$
		html.th(statusTime);
		html.closeTr();
		appendHtmlSplitsInColumns(buildNormalSplits(data), nbColumns, html);
	}
	
	/**
	 * @param buildNormalSplits
	 * @param html
	 */
	private void appendHtmlSplitsInColumns(SplitTime[] splits, int nbColumns, Html html) {
		int nbRows = (splits.length / nbColumns) + 1;
		int rowStart = 0;
		for (int i = 0; i < nbRows; i++) {
			// if last line, take the last remaining splits, not a full row
			int limit = ( i==nbRows-1 ) ? (splits.length % nbColumns) : nbColumns;
			
			// first line with seq and control number/code
			html.openTr().td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				if( split.trace != null ) {
					String label = split.seq + " (" + split.trace.getBasicCode() +")"; //$NON-NLS-1$ //$NON-NLS-2$
					html.th(label, "align=\"right\""); //$NON-NLS-1$
				} else {
					html.td(split.seq, "align=\"right\""); //$NON-NLS-1$
				}
			}
			html.closeTr();
			// second line is cumulative split since start
			html.openTr().td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				String label = TimeManager.time(split.time);
//				if( split.trace!=null && ! split.trace.isOK() ) {
//					label = Html.tag("i", label, new StringBuffer()).toString();
//				}
				html.td(label, "align=\"right\""); //$NON-NLS-1$
			}
			html.closeTr();
			// third line is partial split since previous ok punch
			html.openTr().td(""); //$NON-NLS-1$
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				String label = TimeManager.time(split.split);
				if( split.trace!=null && ! split.trace.isOK() ) {
					label = "&nbsp;"; //$NON-NLS-1$
//					label = Html.tag("i", label, new StringBuffer()).toString();
				}
				html.td(label, "align=\"right\""); //$NON-NLS-1$
			}
			html.closeTr();
			rowStart += nbColumns;
		}
	}
	
	public String printSingleSplits(RunnerRaceData data) {
		if( getSplitPrinter()!=null ) {
			Html html = new Html();
			if( splitFormat==SplitFormat.Ticket ) {
				printSingleSplitsInLine(data, html);
			} else {
				printSingleSplitsInColumns(data, html);
			}
		
			JTextPane ticket = new JTextPane(); 
			ticket.setContentType("text/html"); //$NON-NLS-1$
			String content = html.close();
			ticket.setText(content);
			try {
				ticket.print(null, null, false, getSplitPrinter(), null, true);
			} catch (PrinterException e) {
				geco().debug(e.getLocalizedMessage());
			}
			return content;
		}
		return ""; //$NON-NLS-1$
	}

	private void printSingleSplitsInColumns(RunnerRaceData data, Html html) {
		html.tag("h2", "align=\"center\"", geco().stage().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		html.b(data.getRunner().getName() + " - " //$NON-NLS-1$
				+ geco().stage().getName() + " - " //$NON-NLS-1$
				+ data.getCourse().getName() + " - " //$NON-NLS-1$
				+ data.getResult().shortFormat());
		html.open("table"); //$NON-NLS-1$
		appendHtmlSplitsInColumns(buildNormalSplits(data), nbColumns, html);
		html.close("table"); //$NON-NLS-1$
		html.tag("div", //$NON-NLS-1$
				"align=\"center\"", //$NON-NLS-1$
				"Geco for orienteering - http://bitbucket.org/sdenier/geco"); //$NON-NLS-1$
	}

	private void printSingleSplitsInLine(RunnerRaceData data, Html html) {
		html.tag("p", "align=\"center\"", geco().stage().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		html.open("p", "align=\"center\"").b(data.getRunner().getName()).close("p"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		html.b(data.getCourse().getName() + " - " //$NON-NLS-1$
				+ data.getResult().shortFormat());
		html.open("table"); //$NON-NLS-1$
		appendHtmlSplitsInLine(buildLinearSplits(data), html);
		html.close("table"); //$NON-NLS-1$
		html.tag("div", "align=\"center\"", "Geco for orienteering"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		html.tag("div",	"align=\"center\"",	"http://bitbucket.org/sdenier/geco"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void appendHtmlSplitsInLine(SplitTime[] linearSplits, Html html) {
		for (SplitTime splitTime : linearSplits) {
			html.openTr();
			Trace trace = splitTime.trace;
			String time = TimeManager.time(splitTime.time);
			if( trace!=null ) {
				html.td(splitTime.seq);
				html.td(splitTime.trace.getCode());
				if( trace.isOK() ) {
					html.th(time, "align=\"right\""); //$NON-NLS-1$
				} else {
					if( trace.isAdded() || trace.isSubst() ) {
						time = Html.tag("i", time, new StringBuffer()).toString(); //$NON-NLS-1$
					}
					html.td(time, "align=\"right\""); //$NON-NLS-1$
				}
				html.td(TimeManager.time(splitTime.split), "align=\"right\""); //$NON-NLS-1$
			} else {
				html.td(splitTime.seq);
				html.td(""); //$NON-NLS-1$
				html.th(time, "align=\"right\""); //$NON-NLS-1$
				html.td(TimeManager.time(splitTime.split), "align=\"right\"");				 //$NON-NLS-1$
			}
			html.closeTr();
		}
	}


	public Vector<String> listPrinterNames() {
		Vector<String> printerNames = new Vector<String>();
		for (PrintService printer : PrinterJob.lookupPrintServices()) {
			printerNames.add(printer.getName());
		}
		return printerNames;
	}
	
	protected PrintService getSplitPrinter() {
		if( splitPrinter==null ) {
			splitPrinter = PrintServiceLookup.lookupDefaultPrintService();
		}
		return splitPrinter;
	}
	
	public String getSplitPrinterName() {
		return ( getSplitPrinter()==null ) ? "" : getSplitPrinter().getName(); //$NON-NLS-1$
	}
	
	public String getDefaultPrinterName() {
		PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		return ( defaultService==null ) ? "" : defaultService.getName(); //$NON-NLS-1$
	}
	
	public boolean setSplitPrinterName(String name) {
		for (PrintService printer : PrinterJob.lookupPrintServices()) {
			if( printer.getName().equals(name) ) {
				splitPrinter = printer;
				return true;
			}
		}
		splitPrinter = null;
		return false;
	}

	
	public void enableAutoprint() {
		this.autoPrint = true;
	}
	
	public void disableAutoprint() {
		this.autoPrint = false;
	}
	
	public SplitFormat getSplitFormat() {
		return this.splitFormat;
	}
	
	public void setSplitFormat(SplitFormat format) {
		this.splitFormat = format;
	}

	@Override
	public void cardRead(String chip) {
		if( autoPrint ) {
			printSingleSplits(registry().findRunnerData(chip));
		}
	}
	@Override
	public void unknownCardRead(String chip) {	}
	@Override
	public void cardReadAgain(String chip) {	}


	@Override
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		setSplitPrinterName(props.getProperty(splitPrinterProperty()));
		String nbCol = props.getProperty(splitNbColumnsProperty());
		if( nbCol!=null ){
			nbColumns = Integer.parseInt(nbCol);
		}
		String format = props.getProperty(splitFormatProperty());
		if( format!=null ) {
			setSplitFormat(SplitFormat.valueOf(format));
		}
	}
	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitPrinterProperty(), getSplitPrinterName());
		properties.setProperty(splitNbColumnsProperty(), Integer.toString(nbColumns));
		properties.setProperty(splitFormatProperty(), getSplitFormat().name());
	}
	@Override
	public void closing(Stage stage) {	}

	public static String splitPrinterProperty() {
		return "SplitPrinter"; //$NON-NLS-1$
	}
	public static String splitNbColumnsProperty() {
		return "SplitNbColumns"; //$NON-NLS-1$
	}
	public static String splitFormatProperty() {
		return "SplitFormat"; //$NON-NLS-1$
	}

}
