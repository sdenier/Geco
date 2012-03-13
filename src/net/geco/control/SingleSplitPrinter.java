/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterJob;
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
import net.geco.control.ResultBuilder.SplitTime;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Nov 25, 2010
 *
 */
public class SingleSplitPrinter extends Control implements StageListener, CardListener {
	
	public static enum SplitFormat { MultiColumns, Ticket }

	private static final boolean DEBUGMODE = false;
	
	private PrintService splitPrinter;
	private boolean autoPrint;
	private SplitFormat splitFormat = SplitFormat.MultiColumns;
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
				exporter.includeHeader(html, "ticket.css", false); //$NON-NLS-1$
				printSingleSplitsInLine(data, html);
			} else {
				exporter.includeHeader(html, "result.css", false); //$NON-NLS-1$
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
			
			if( ! DEBUGMODE ) {
				ExecutorService pool = Executors.newCachedThreadPool();
				pool.submit(callable);
			} else {
				JFrame jFrame = new JFrame();
				jFrame.add(ticket);
				jFrame.pack();
				jFrame.setVisible(true);
			}
			
			return content;
		}
		return ""; //$NON-NLS-1$
	}


	private void printSingleSplitsInColumns(RunnerRaceData data, Html html) {
		appendMessage(getHeaderMessage(), html);
		html.b(data.getRunner().getName() + " - " //$NON-NLS-1$
				+ geco().stage().getName() + " - " //$NON-NLS-1$
				+ data.getCourse().getName() + " - " //$NON-NLS-1$
				+ data.getResult().shortFormat());
		html.open("table"); //$NON-NLS-1$
		exporter.appendHtmlSplitsInColumns(
				builder.buildNormalSplits(data, null),
				new SplitTime[0],
				exporter.nbColumns(),
				html);
		html.close("table"); //$NON-NLS-1$
		appendMessage(getFooterMessage(), html);
	}


	private void printSingleSplitsInLine(RunnerRaceData data, Html html) {
	//		char[] chars = Character.toChars(0x2B15); // control flag char :)
		appendMessage(getHeaderMessage(), html);
		html.open("div", "align=\"center\""); //$NON-NLS-1$ //$NON-NLS-2$
		html.contents(geco().stage().getName()).br();
		html.b(data.getRunner().getName()).br();
		html.br();
		html.b(data.getCourse().getName() + " - " //$NON-NLS-1$
				+ data.getResult().shortFormat());
		html.close("div"); // don't center table, it wastes too much space for some formats. //$NON-NLS-1$
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

		if( DEBUGMODE ){
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
				if( DEBUGMODE ){
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
			if( DEBUGMODE ){
				System.out.print("Found: "); //$NON-NLS-1$
			}			
		} else {
			if( DEBUGMODE ){
				System.out.print("Chosen: "); //$NON-NLS-1$
			}			
		}
		if( bestMedia!=null ){
			attributes.add(bestMedia);
			MediaSize fitSize = MediaSize.getMediaSizeForName(bestMedia);
			if( DEBUGMODE ){
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
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		setSplitPrinterName(props.getProperty(splitPrinterProperty()));
		String format = props.getProperty(splitFormatProperty());
		if( format!=null ) {
			setSplitFormat(SplitFormat.valueOf(format));
		}
		setHeaderMessage( props.getProperty(splitHeaderMessageProperty(), "Geco") ); //$NON-NLS-1$
		setFooterMessage( props.getProperty(splitFooterMessageProperty(), "http://geco.webou.net") ); //$NON-NLS-1$
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
