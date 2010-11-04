/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Oct 15, 2010
 *
 */
public class SplitBuilder extends ResultBuilder implements Printable {
	
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

	
	/**
	 * @param gecoControl
	 */
	public SplitBuilder(GecoControl gecoControl) {
		super(gecoControl);
		// TODO Auto-generated constructor stub
	}
	
	
	public SplitTime[] buildNormalSplits(RunnerRaceData data) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		ArrayList<SplitTime> added = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in normal mode, added splits appear after normal splits
		buildSplits(data, splits, added);
		splits.addAll(added);
		return splits.toArray(new SplitTime[0]);
	}	
	
	public SplitTime[] buildLinearSplits(RunnerRaceData data) {
		ArrayList<SplitTime> splits = new ArrayList<SplitTime>(data.getResult().getTrace().length);
		// in linear mode, added splits are kept in place with others
		buildSplits(data, splits, splits);
		return splits.toArray(new SplitTime[0]);
	}

	private void buildSplits(RunnerRaceData data, List<SplitTime> splits, List<SplitTime> added) {
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
				splits.add(createSplit(Integer.toString(control), trace, startTime, TimeManager.NO_TIME_l, time));
				control++;
			} else if( trace.isMP() ) {
				splits.add(createSplit(Integer.toString(control), trace, startTime, TimeManager.NO_TIME_l, TimeManager.NO_TIME_l));
				control++;
			} else { // added trace
				added.add(createSplit("", trace, startTime, TimeManager.NO_TIME_l, time));
			}
		}
		splits.add(createSplit("F", null, startTime, previousTime, data.getFinishtime().getTime()));
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

	
	
	public String generateHtmlSplits(ResultConfig config) {
		Vector<Result> results = buildResults(config);
		Html html = new Html();
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty() ) {
				appendHtmlResultsWithSplits(result, config, html);	
			}
		}
		return html.close();
	}
	
	private void appendHtmlResultsWithSplits(Result result, ResultConfig config, Html html) {
		html.tag("h1", result.getIdentifier());
		html.open("table");
		for (RankedRunner runner : result.getRanking()) {
			RunnerRaceData data = runner.getRunnerData();
			html.openTr();
			html.th(Integer.toString(runner.getRank()));
			html.tag("th", "colspan=\"5\"", data.getRunner().getName());
			html.th(data.getResult().formatRacetime());
			html.closeTr();
			appendHtmlSplits(buildNormalSplits(data), html);
			html.openTr().closeTr();
		}
		html.openTr().td("").td("").td("").closeTr();
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				html.openTr();
				html.th("");
				html.tag("th", "colspan=\"5\"", runner.getName());
				html.th(runnerData.getResult().formatStatus());
				html.closeTr();
			} else if( config.showNC ) {
				RunnerResult runnerResult = runnerData.getResult();
				html.openTr();
				html.th("NC");
				html.tag("th", "colspan=\"5\"", runner.getName());
				html.th( runnerResult.shortFormat() );
				html.closeTr();
			}
			appendHtmlSplits(buildNormalSplits(runnerData), html);
			html.openTr().closeTr();
		}
		if( config.showOthers ) {
			html.openTr().td("").td("").td("").closeTr();
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				html.openTr();
				html.th("");
				html.tag("th", "colspan=\"5\"", runnerData.getRunner().getName());
				html.th(runnerData.getResult().formatStatus());
				html.closeTr();
				appendHtmlSplits(buildNormalSplits(runnerData), html);
				html.openTr().closeTr();
			}			
		}
		html.close("table");
	}
	
	/**
	 * @param buildNormalSplits
	 * @param html
	 */
	private void appendHtmlSplits(SplitTime[] splits, Html html) {
		int nbColumns = 9;
		int nbRows = (splits.length / nbColumns) + 1;
		int rowStart = 0;
		for (int i = 0; i < nbRows; i++) {
			// if last line, take the last remaining splits, not a full row
			int limit = ( i==nbRows-1 ) ? (splits.length % nbColumns) : nbColumns;
			html.openTr().td("");
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				if( split.trace != null ) {
					String label = split.seq + " (" + split.trace.getCode() +")";
					html.tag("th", "align=\"right\"", label);
				} else {
					html.tag("td", "align=\"right\"", split.seq);
				}
			}
			html.closeTr();
			html.openTr().td("");
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				String label = TimeManager.time(split.time);
				if( split.trace!=null && ! split.trace.isOK() ) {
					label = Html.tag("i", label, new StringBuffer()).toString();
				}
				html.tag("td", "align=\"right\"", label);
			}
			html.closeTr();
			html.openTr().td("");
			for (int j = 0; j < limit; j++) {
				SplitTime split = splits[j + rowStart];
				String label = TimeManager.time(split.split);
				if( split.trace!=null && ! split.trace.isOK() ) {
					label = Html.tag("i", label, new StringBuffer()).toString();
				}
				html.tag("td", "align=\"right\"", label);
			}
			html.closeTr();
			rowStart += nbColumns;
		}
	}


	public static void main(String[] args) {
		PrintService service = PrintServiceLookup.lookupDefaultPrintService();
		System.out.println(service);
		PrintService[] lookupPrintServices = PrinterJob.lookupPrintServices();
		for (PrintService printService : lookupPrintServices) {
			System.out.println(printService);
		}
		
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(new SplitBuilder(new GecoControl()));
//		boolean ok = job.printDialog();
		boolean ok = true;
		if( ok ){
			try {
				job.print();
			} catch (PrinterException e) {
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if( pageIndex>0 ){
			return NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D) graphics;
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		
		g2.drawString("Hello World", 100, 100);
		g2.setColor(Color.red);
		g2.drawOval(200, 200, 100, 50);
		g2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));
		g2.drawString("Another day in the world", 100, 100 + g2.getFontMetrics().getHeight());
		
		return PAGE_EXISTS;
	}

}
