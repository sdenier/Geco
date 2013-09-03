/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.context.RunnerContext;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Nov 25, 2010
 *
 */
public class RunnerSplitPrinter extends Control implements StageListener, CardListener {
	
	public static enum SplitFormat { MultiColumns, Ticket }
	
	private SplitFormat splitFormat = SplitFormat.MultiColumns;
	private PrintService splitPrinter;
	private MediaSizeName[] splitMedia;

	private int nbColumns;
	private File columnTemplate;
	private File ticketTemplate;
	
	private boolean autoPrint;
	private boolean prototypeMode;
	
	private final ResultBuilder builder;
	private final SplitExporter exporter;
	
	public RunnerSplitPrinter(GecoControl gecoControl) {
		super(RunnerSplitPrinter.class, gecoControl);
		builder = getService(ResultBuilder.class);
		exporter = getService(SplitExporter.class);
		geco().announcer().registerStageListener(this);
		geco().announcer().registerCardListener(this);
		changed(null, stage());
	}
	
	public void printSingleSplits(RunnerRaceData data) {
		if( getSplitPrinter()!=null ) {
			try {
				StringWriter out = new StringWriter();
				if( splitFormat==SplitFormat.Ticket ) {
					generateSingleSplitsInLine(data, out);
				} else {
					generateSingleSplitsInColumns(data, out);
				}
				final JTextPane ticket = new JTextPane(); 
				ticket.setContentType("text/html"); //$NON-NLS-1$
				ticket.setText(out.toString());
				
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
						Writer writer = new BufferedWriter(new FileWriter(stage().filepath("runner_splits.html"))); //$NON-NLS-1$
						writer.write(out.toString());
						writer.close();
					} catch (IOException e) {
						geco().logger().debug(e);
					}
				}
			} catch (IOException e) {
				geco().info(e.toString(), true); 
			}
		}
	}


	private void generateSingleSplitsInColumns(RunnerRaceData data, Writer out) throws IOException {
		RunnerContext runnerCtx = buildRunnerSplitContext(data);
		exporter.mergeCustomStageProperties(runnerCtx);
		exporter.createRunnerSplitsRowsAndColumns(runnerCtx,
												  builder.buildNormalSplits(data, true),
												  new SplitTime[0],
												  nbColumns());
		
		exporter.getExternalTemplate(getColumnTemplatePath()).execute(runnerCtx, out);
	}

	private void generateSingleSplitsInLine(RunnerRaceData data, Writer out) throws IOException {
		RunnerContext runnerCtx = buildRunnerSplitContext(data);
		exporter.mergeCustomStageProperties(runnerCtx);
		exporter.createRunnerSplitsInline(runnerCtx,
												 builder.buildLinearSplits(data));
		
		exporter.getExternalTemplate(getTicketTemplatePath()).execute(runnerCtx, out);
	}

	protected RunnerContext buildRunnerSplitContext(RunnerRaceData data) {
		Course course = data.getCourse();
		RunnerContext runnerCtx = RunnerContext.createUnrankedRunner(data);
		runnerCtx.put("geco_StageTitle", stage().getName()); //$NON-NLS-1$
		runnerCtx.put("geco_RunnerCourse", course.getName()); //$NON-NLS-1$
		runnerCtx.put("geco_CourseLength", course.getLength()); //$NON-NLS-1$
		runnerCtx.put("geco_CourseClimb", course.getClimb()); //$NON-NLS-1$
		return runnerCtx;
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

	public void enableFormatPrototyping(boolean flag) {
		prototypeMode = flag;
	}

	public int nbColumns() {
		return nbColumns;
	}

	public void setNbColumns(int nb) {
		nbColumns = nb;
	}
	
	public File getColumnTemplate() {
		return columnTemplate;
	}

	protected String getColumnTemplatePath() {
		return getColumnTemplate().getAbsolutePath();
	}

	public void setColumnTemplate(File selectedFile) {
		if( getColumnTemplate() != null ) {
			exporter.resetTemplate(getColumnTemplatePath());
		}
		columnTemplate = selectedFile;
	}

	public File getTicketTemplate() {
		return ticketTemplate;
	}

	protected String getTicketTemplatePath() {
		return getTicketTemplate().getAbsolutePath();
	}

	public void setTicketTemplate(File selectedFile) {
		if( getTicketTemplate() != null ) {
			exporter.resetTemplate(getTicketTemplatePath());
		}
		ticketTemplate = selectedFile;
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
		String format = props.getProperty(splitFormatProperty(), SplitFormat.MultiColumns.name());
		setSplitFormat(SplitFormat.valueOf(format));

		setNbColumns(Integer.parseInt(props.getProperty(splitNbColumnsProperty(), "12"))); //$NON-NLS-1$
		setColumnTemplate(
				new File(stage().getProperties().getProperty(columnTemplateProperty(),
															 "formats/splits_columns.mustache") )); //$NON-NLS-1$
		setTicketTemplate(
				new File(stage().getProperties().getProperty(ticketTemplateProperty(),
															 "formats/splits_ticket.mustache") )); //$NON-NLS-1$
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitPrinterProperty(), getSplitPrinterName());
		properties.setProperty(splitFormatProperty(), getSplitFormat().name());

		properties.setProperty(splitNbColumnsProperty(), Integer.toString(nbColumns()));
		if( getColumnTemplate().exists() ){
			properties.setProperty(columnTemplateProperty(), getColumnTemplatePath());
		}
		if( getTicketTemplate().exists() ){
			properties.setProperty(ticketTemplateProperty(), getTicketTemplatePath());
		}
	}
	@Override
	public void closing(Stage stage) {	}

	public static String splitPrinterProperty() {
		return "SplitPrinter"; //$NON-NLS-1$
	}
	public static String splitFormatProperty() {
		return "SplitFormat"; //$NON-NLS-1$
	}
	public static String splitNbColumnsProperty() {
		return "SplitNbColumns"; //$NON-NLS-1$
	}
	public static String columnTemplateProperty() {
		return "ColumnTemplate"; //$NON-NLS-1$
	}
	public static String ticketTemplateProperty() {
		return "TicketTemplate"; //$NON-NLS-1$
	}
}
