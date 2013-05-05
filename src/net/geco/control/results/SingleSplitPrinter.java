/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import net.geco.basics.Announcer.CardListener;
import net.geco.basics.Announcer.StageListener;
import net.geco.basics.Html;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.results.AResultExporter.OutputType;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;


/**
 * @author Simon Denier
 * @since Nov 25, 2010
 *
 */
public class SingleSplitPrinter extends Control implements StageListener, CardListener {
	
	public static enum SplitFormat { MultiColumns, Ticket }
	
	private PrintService splitPrinter;
	private boolean autoPrint;
	private SplitFormat splitFormat = SplitFormat.MultiColumns;
	private boolean prototypeMode;
	private MediaSizeName[] splitMedia;
	private String headerMessage;
	private String footerMessage;
	
	private final ResultBuilder builder;
	private final SplitExporter exporter;
	
	public SingleSplitPrinter(GecoControl gecoControl) {
		super(SingleSplitPrinter.class, gecoControl);
		builder = getService(ResultBuilder.class);
		exporter = getService(SplitExporter.class);
		geco().announcer().registerStageListener(this);
		geco().announcer().registerCardListener(this);
		changed(null, stage());
	}
	
	public String printSingleSplits(RunnerRaceData data) {
		if( getSplitPrinter()!=null ) {
			Html html = new Html();
			if( splitFormat==SplitFormat.Ticket ) {
				exporter.includeHeader(html, "ticket.css", OutputType.PRINTER); //$NON-NLS-1$
				printSingleSplitsInLine(data, html);
			} else {
				exporter.includeHeader(html, "result.css", OutputType.PRINTER); //$NON-NLS-1$
				printSingleSplitsInColumns(data, html);
			}
		
			final JTextPane ticket = new JTextPane(); 
			ticket.setContentType("text/html"); //$NON-NLS-1$
			String content = html.close();
			ticket.setText(content);
			
			final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
			if( splitFormat==SplitFormat.Ticket ) {
				computeMediaForTicket(ticket, attributes);
			}
			
			Callable<Boolean> callable = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return ticket.print(null, null, false, getSplitPrinter(), attributes, false);
				}
			};
			
			if( ! prototypeMode ) {
				ExecutorService pool = Executors.newCachedThreadPool();
				pool.submit(callable);
			} else {
				JFrame jFrame = new JFrame();
				jFrame.add(ticket);
				jFrame.pack();
				jFrame.setVisible(true);
				try {
					Writer writer = new BufferedWriter(new FileWriter(stage().filepath("runner_splits.html")));
					writer.write(content);
					writer.close();
				} catch (IOException e) {
					geco().logger().debug(e);
				}
			}
			
			return content;
		}
		return ""; //$NON-NLS-1$
	}


	private void printSingleSplitsInColumns(RunnerRaceData data, Html html) {
		appendMessage(getHeaderMessage(), html);

		Runner runner = data.getRunner();
		Course course = data.getCourse();
		String pace = ""; //$NON-NLS-1$
		if( data.getResult().is(Status.OK) && course.hasDistance() ) {
			pace = data.formatPace() + " min/km"; //$NON-NLS-1$
		}
		html.br()
			.open("table") //$NON-NLS-1$
			.openTr("runner") //$NON-NLS-1$
			.th(runner.getName(), "colspan=\"2\"") //$NON-NLS-1$
			.th(course.getName())
			.th(runner.getCategory().getName())
			.closeTr()
			.openTr("runner") //$NON-NLS-1$
			.td(data.getResult().shortFormat(), "class=\"time\"") //$NON-NLS-1$
			.td(pace, "class=\"center\"") //$NON-NLS-1$
			.td(course.formatDistanceClimb(), "class=\"center\"") //$NON-NLS-1$
			.td(runner.getClub().getName(), "class=\"center\"") //$NON-NLS-1$
			.closeTr()
			.close("table") //$NON-NLS-1$
			.br();

		html.open("table"); //$NON-NLS-1$
		exporter.appendHtmlSplitsInColumns(
				builder.buildNormalSplits(data, null),
				new SplitTime[0],
				exporter.nbColumns(),
				html);
		html.close("table") //$NON-NLS-1$
			.br();
		appendMessage(getFooterMessage(), html);
	}


	private void printSingleSplitsInLine(RunnerRaceData data, Html html) {
	//		char[] chars = Character.toChars(0x2B15); // control flag char :)
		appendMessage(getHeaderMessage(), html);
		Runner runner = data.getRunner();
		Course course = data.getCourse();
		html.br()
			.open("div", "align=\"center\"") //$NON-NLS-1$ //$NON-NLS-2$
			.b(runner.getName())
			.tag("div", runner.getClub().getName()) //$NON-NLS-1$
			.tag("div", course.getName() + " - " + runner.getCategory().getName()) //$NON-NLS-1$ //$NON-NLS-2$
			.tag("div", course.formatDistanceClimb()) //$NON-NLS-1$
			.br()
			.b(data.getResult().shortFormat());
		if( data.getResult().is(Status.OK) && course.hasDistance() ) {
			html.tag("div", data.formatPace() + " min/km"); //$NON-NLS-1$ //$NON-NLS-2$
		}		
		html.close("div") //$NON-NLS-1$
			.br();
		// don't center table, it wastes too much space for some formats.
		html.open("table", "width=\"75%\""); //$NON-NLS-1$ //$NON-NLS-2$
		exporter.appendHtmlSplitsInLine(builder.buildLinearSplits(data), html);
		html.close("table").br(); //$NON-NLS-1$
		appendMessage(getFooterMessage(), html);
	}

	private void appendMessage(String message, Html html) {
		if( message.length() > 0 ){
			html.tag("div", "align=\"center\"", message); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void computeMediaForTicket(final JTextPane ticket,
			final PrintRequestAttributeSet attributes) {
		Dimension preferredSize = ticket.getPreferredSize();
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		float height = ((float) preferredSize.height) / dpi;
		float width = ((float) preferredSize.width) / dpi;

		if( prototypeMode ){
			System.out.print("Request: "); //$NON-NLS-1$
			System.out.print(height * 25.4);
			System.out.print("x"); //$NON-NLS-1$
			System.out.print(width * 25.4);
			System.out.println(" mm"); //$NON-NLS-1$
		}

		MediaSizeName bestMedia = null;
		float bestFit = Float.MAX_VALUE;
		for (MediaSizeName media : getSplitMedia()) {
			MediaSize mediaSize = MediaSize.getMediaSizeForName(media);
			if( mediaSize!=null ){
				if( prototypeMode ){
					System.out.print(mediaSize.toString(MediaSize.MM, "mm")); //$NON-NLS-1$
					System.out.println(" - " + media); //$NON-NLS-1$
				}
				float dy = mediaSize.getY(MediaSize.INCH) - height;
				float dx = mediaSize.getY(MediaSize.INCH) - width;
				float fit = dy + dx;
				if( dy >= 0 && dx >= 0 && fit <= bestFit ){
					bestFit = fit;
					bestMedia = media;
				}
			}
		}
		if( bestMedia==null ){
			bestMedia = MediaSize.findMedia(width, height, MediaSize.INCH);
			geco().debug(Messages.getString("SingleSplitPrinter.SmallTicketSizeWarning")); //$NON-NLS-1$
			if( prototypeMode ){
				System.out.print("Found: "); //$NON-NLS-1$
			}			
		} else {
			if( prototypeMode ){
				System.out.print("Chosen: "); //$NON-NLS-1$
			}			
		}
		if( bestMedia!=null ){
			attributes.add(bestMedia);
			MediaSize fitSize = MediaSize.getMediaSizeForName(bestMedia);
			if( prototypeMode ){
				System.out.println(fitSize.toString(MediaSize.MM, "mm")); //$NON-NLS-1$
			}
		} else {
			geco().log(Messages.getString("SingleSplitPrinter.NoMatchingTicketSizeWarning")); //$NON-NLS-1$
		}
	}


	private MediaSizeName[] getSplitMedia() {
		if( splitMedia==null ) {
			Vector<MediaSizeName> mediaSizenames = new Vector<MediaSizeName>();
			Media[] media = (Media[]) getSplitPrinter().getSupportedAttributeValues(Media.class, null, null);
			for (Media m : media) {
				if( m!=null && m instanceof MediaSizeName ){
					mediaSizenames.add((MediaSizeName) m);
				}
			}
			splitMedia = mediaSizenames.toArray(new MediaSizeName[0]);
		}
		return splitMedia;
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
		splitMedia = null; // reset cache
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

	public String getHeaderMessage() {
		return headerMessage;
	}

	public void setHeaderMessage(String headerMessage) {
		this.headerMessage = headerMessage;
	}

	public String getFooterMessage() {
		return footerMessage;
	}

	public void setFooterMessage(String footerMessage) {
		this.footerMessage = footerMessage;
	}

	public void enableFormatPrototyping(boolean flag) {
		prototypeMode = flag;
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
	public void rentedCard(String siIdent) {	}
	@Override
	public void registeredCard(String ecard) {	}

	@Override
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		setSplitPrinterName(props.getProperty(splitPrinterProperty()));
		String format = props.getProperty(splitFormatProperty());
		if( format!=null ) {
			setSplitFormat(SplitFormat.valueOf(format));
		}
		setHeaderMessage( props.getProperty(splitHeaderMessageProperty(), stage().getName()) );
		setFooterMessage( props.getProperty(splitFooterMessageProperty(), "Geco - http://sdenier.github.com/Geco") ); //$NON-NLS-1$
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitPrinterProperty(), getSplitPrinterName());
		properties.setProperty(splitFormatProperty(), getSplitFormat().name());
		properties.setProperty(splitHeaderMessageProperty(), getHeaderMessage());
		properties.setProperty(splitFooterMessageProperty(), getFooterMessage());
	}
	@Override
	public void closing(Stage stage) {	}

	public static String splitPrinterProperty() {
		return "SplitPrinter"; //$NON-NLS-1$
	}
	public static String splitFormatProperty() {
		return "SplitFormat"; //$NON-NLS-1$
	}

	public static String splitFooterMessageProperty() {
		return "SplitHeaderMessage"; //$NON-NLS-1$
	}

	public static String splitHeaderMessageProperty() {
		return "SplitFooterMessage"; //$NON-NLS-1$
	}
	
}
