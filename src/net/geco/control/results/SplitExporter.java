/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.control.results.context.ContextList;
import net.geco.control.results.context.GenericContext;
import net.geco.control.results.context.RunnerContext;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;

import com.samskivert.mustache.Mustache;


/**
 * @author Simon Denier
 * @since Oct 15, 2010
 *
 */
public class SplitExporter extends AResultExporter implements StageListener {
	
	public static class Names {
		protected List<String> clubNames;
		protected List<String> categoryNames;
		protected List<String> courseNames;
		
		public Names(List<String> clubNames, List<String> categoryNames, List<String> courseNames) {
			this.clubNames = clubNames;
			this.categoryNames = categoryNames;
			this.courseNames = courseNames;
		}
		public String clubIndex(String clubName) {
			return Integer.toString(clubNames.indexOf(clubName)); }
		public String categoryIndex(String categoryName) {
			return Integer.toString(categoryNames.indexOf(categoryName)); }
		public String courseIndex(String courseName) {
			return Integer.toString(courseNames.indexOf(courseName)); }
	}
	
	private File splitsTemplate = new File("formats/results_splits.mustache");

	private boolean withBestSplits;

	private int nbColumns = 12;

	private int refreshInterval = 0;

	
	public SplitExporter(GecoControl gecoControl) {
		super(SplitExporter.class, gecoControl);
		geco().announcer().registerStageListener(this);
		withBestSplits();
	}
	
	public void withBestSplits() {
		withBestSplits = true;
	}
	
	public void withoutBestSplits() {
		withBestSplits = false;
	}
	
	public int nbColumns() {
		return nbColumns;
	}

	@Override
	protected void exportHtmlFile(String filename, ResultConfig config, int refreshInterval)
			throws IOException {
		BufferedReader template = GecoResources.getSafeReaderFor(getSplitsTemplate().getAbsolutePath());
		BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
		buildHtmlResults(template, config, refreshInterval, writer, OutputType.FILE);
		writer.close();
		template.close();
	}

	protected void buildHtmlResults(Reader template, ResultConfig config, int refreshInterval,
			Writer out, OutputType outputType) {
		// TODO: lazy cache of template
		Mustache.compiler()
			.defaultValue("N/A")
			.compile(template)
			.execute(buildDataContext(config, refreshInterval, outputType), out);
	}

	protected Object buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;

		GenericContext stageContext = new GenericContext();
		stageContext.put("geco_StageTitle", stage().getName());

		// General layout
		stageContext.put("geco_SingleCourse?", isSingleCourseResult);
		stageContext.put("geco_RunnerCategory?", isSingleCourseResult);
		stageContext.put("geco_Penalties?", config.showPenalties);

		// Meta info
		stageContext.put("geco_FileOutput?", outputType == OutputType.FILE);
		stageContext.put("geco_AutoRefresh?", refreshInterval > 0);
		stageContext.put("geco_RefreshInterval", refreshInterval);
		stageContext.put("geco_PrintMode?", outputType == OutputType.PRINTER);
		stageContext.put("geco_Timestamp", new SimpleDateFormat("H:mm").format(new Date()));
		
		mergeCustomStageProperties(stageContext);
		
		List<Result> results = buildResults(config);
		ContextList resultsCollection = stageContext.createContextList("geco_ResultsCollection", results.size());
		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();

				GenericContext resultContext = new GenericContext();
				resultsCollection.add(resultContext);

				resultContext.put("geco_ResultName", result.getIdentifier());
				resultContext.put("geco_NbFinishedRunners", result.nbFinishedRunners());
				resultContext.put("geco_NbPresentRunners", result.nbPresentRunners());
				if( isSingleCourseResult ) {
					resultContext.put("geco_CourseLength", result.anyCourse().getLength());
					resultContext.put("geco_CourseClimb", result.anyCourse().getClimb());
				}
				
				ContextList runnersCollection =
						resultContext.createContextList("geco_RankedRunners", result.getRanking().size());
				for (RankedRunner rankedRunner : result.getRanking()) {
					runnersCollection.add(RunnerContext.createRankedRunner(rankedRunner, bestTime));
				}

				runnersCollection =
						resultContext.createContextList("geco_UnrankedRunners", result.getUnrankedRunners().size());
				for (RunnerRaceData data : result.getUnrankedRunners()) {
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							runnersCollection.add(RunnerContext.createNCRunner(data));
						} // else nothing
					} else {
						runnersCollection.add(RunnerContext.createUnrankedRunner(data));
					}
				}
			}
		}
		return stageContext;
	}

	protected void mergeCustomStageProperties(GenericContext stageContext) {
		final String customPropertiesPath = stage().filepath("formats.prop");
		if( GecoResources.exists(customPropertiesPath) ) {
			Properties props = new Properties();
			try {
				props.load( GecoResources.getSafeReaderFor(customPropertiesPath) );
				stageContext.mergeProperties(props);
			} catch (IOException e) {
				geco().logger().debug(e);
			}
		}
	}

	@Override
	public String generateHtmlResults(ResultConfig config, int refreshInterval, OutputType outputType) {
		List<Result> results = buildResults(config);
		this.refreshInterval = refreshInterval;
		Html html = new Html();
		includeHeader(html, "result.css", outputType); //$NON-NLS-1$
		if( outputType != OutputType.DISPLAY ) {
			html.nl().tag("h1", stage().getName() //$NON-NLS-1$
								+ " - "			  //$NON-NLS-1$
								+ Messages.getString("SplitExporter.SplitsOutputTitle")); //$NON-NLS-1$
		}
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty() ) {
				SplitTime[] bestSplits = null;
				if( withBestSplits ){
					bestSplits = resultBuilder.initializeBestSplits(result, config.resultType);
				}
				Map<RunnerRaceData, SplitTime[]> allSplits = resultBuilder.buildAllNormalSplits(result, bestSplits);
				bestSplits = bestSplits == null ? new SplitTime[0] : bestSplits;
				appendHtmlResultsWithSplits(result, allSplits, bestSplits, config, html);
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
		html.nl().tag("h2", result.getIdentifier()).nl(); //$NON-NLS-1$
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
		for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
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
			for (RunnerRaceData runnerData : result.getUnresolvedRunners()) {
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
			
			// first line with seq and control number/code
			html.openTr("controls").td(""); //$NON-NLS-1$ //$NON-NLS-2$
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
			html.openTr("times").td(""); //$NON-NLS-1$ //$NON-NLS-2$
			for (int j = 0; j < limit; j++) {
				int k = j + rowStart;
				SplitTime split = splits[k];
				String label = TimeManager.time(split.time);
				long best = 0;
				if( withBestSplits && k < bestSplits.length ){
					best = bestSplits[k].time; 
				}
				showWithBestSplit(label, split.time, best, html);
			}
			html.closeTr();
			// third line is partial split since previous ok punch
			html.openTr("splits").td(""); //$NON-NLS-1$ //$NON-NLS-2$
			for (int j = 0; j < limit; j++) {
				int k = j + rowStart;
				SplitTime split = splits[k];
				String label = TimeManager.time(split.split);
				if( split.trace!=null && ! split.trace.isOK() ) {
					label = "&nbsp;"; //$NON-NLS-1$
				}
				long best = 0;
				if( withBestSplits &&k < bestSplits.length ){
					best = bestSplits[k].split;
				}
				showWithBestSplit(label, split.split, best, html);
			}
			html.closeTr();
			rowStart += nbColumns;
		}
	}
	
	private void showWithBestSplit(String label, long split, long best, Html html) {
		if( withBestSplits && split==best ){
			html.td(label, "class=\"best\""); //$NON-NLS-1$
		} else {
			html.td(label);
		}
	}
	
	protected void appendHtmlSplitsInLine(SplitTime[] linearSplits, Html html) {
		for (SplitTime splitTime : linearSplits) {
			html.openTr(); //$NON-NLS-1$
			Trace trace = splitTime.trace;
			String time = TimeManager.time(splitTime.time);
			if( trace!=null ) {
				html.td(splitTime.seq, "class=\"code\""); //$NON-NLS-1$
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
				html.td(splitTime.seq, "class=\"code\""); //$NON-NLS-1$
				html.td(""); //$NON-NLS-1$
				html.td(time, "class=\"time\""); //$NON-NLS-1$
				html.td(TimeManager.time(splitTime.split), "class=\"sp\""); //$NON-NLS-1$
			}
			html.closeTr();
		}
	}
	
	@Override
	protected Collection<String> computeCsvRecord(RunnerRaceData runnerData, String resultId, String rank) {
		// edit collection
		ArrayList<String> csvRecord = new ArrayList<String>(super.computeCsvRecord(runnerData, resultId, rank));
		csvRecord.ensureCapacity(csvRecord.size() + 2 * runnerData.getResult().getTrace().length);
		for (SplitTime split: resultBuilder.buildNormalSplits(runnerData, null)) {
			if( split.trace!=null ) { // dont handle finish split
				csvRecord.add(split.trace.getBasicCode());
				csvRecord.add(encodeMPPunch(split));
			}
		}
		return csvRecord;
	}

	private String encodeMPPunch(SplitTime split) {
		return split.trace.isMP() ? "##" : TimeManager.fullTime(split.time); //$NON-NLS-1$
	}
	
	@Override
	public void generateOECsvResult(ResultConfig config, CsvWriter writer) throws IOException {
		generateOECsvResult(config, true, writer);
	}
	
	public void generateOECsvResult(ResultConfig config, boolean withSplits, CsvWriter writer)
																						throws IOException {
		writer.write("N° dép.;Puce;Ident. base de données;Nom;Prénom;Né;S;Plage;nc;Départ;Arrivée;Temps;" //$NON-NLS-1$
				+ "Evaluation;N° club;Nom;Ville;Nat;N° cat.;Court;Long;Num1;Num2;Num3;Text1;Text2;Text3;" //$NON-NLS-1$
				+ "Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée;Engagement;Payé;" //$NON-NLS-1$
				+ "Circuit N°;Circuit;km;m;Postes du circuit;Pl"); //$NON-NLS-1$
		if( withSplits ){
			writer.write(";Poinçon de départ;Arrivée (P);Poste1;Poinçon1;Poste2;Poinçon2;Poste3;Poinçon3;" //$NON-NLS-1$
				+ "Poste4;Poinçon4;Poste5;Poinçon5;Poste6;Poinçon6;Poste7;Poinçon7;Poste8;Poinçon8;" //$NON-NLS-1$
				+ "Poste9;Poinçon9;Poste10;Poinçon10;(peut être plus) ..."); //$NON-NLS-1$
		}
		writer.write("\n"); //$NON-NLS-1$
		
		List<Result> results = buildResults(config);
		Names names = new Names(registry().getClubNames(), registry().getCategoryNames(), registry().getCourseNames());
		for (Result result : results) {
			if( config.showEmptySets || !result.isEmpty()) {
				appendOECsvResult(result, names, config, withSplits, writer);
			}
		}
	}

	public void appendOECsvResult(Result result, Names names, ResultConfig config,
			boolean withSplits, CsvWriter writer) throws IOException {
		for (RankedRunner rRunner : result.getRanking()) {
			RunnerRaceData runnerData = rRunner.getRunnerData();
			writeOECsvResult(runnerData, Integer.toString(rRunner.getRank()), names, withSplits, writer);
		}
		for (RunnerRaceData runnerData : result.getUnrankedRunners()) {
			Runner runner = runnerData.getRunner();
			if( !runner.isNC() ) {
				writeOECsvResult(runnerData, "", names, withSplits, writer); //$NON-NLS-1$
			} else if( config.showNC ) {
				writeOECsvResult(runnerData, "", names, withSplits, writer); //$NON-NLS-1$
			}
		}
		if( config.showOthers ) {
			for (RunnerRaceData runnerData : result.getUnresolvedRunners()) {
				writeOECsvResult(runnerData, "", names, withSplits, writer); //$NON-NLS-1$
			}			
		}
	}

	public void writeOECsvResult(RunnerRaceData runnerData, String rank, Names names,
			boolean withSplits, CsvWriter writer) throws IOException {
		Runner runner = runnerData.getRunner();
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
				names.clubIndex(club.getName()),
				club.getShortname(),
				club.getName(),
				"", //$NON-NLS-1$
				names.categoryIndex(category.getName()),
				category.getShortname(),
				category.getLongname(),
				"", "", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"", "", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"", "", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"", "", "",  	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"0", //$NON-NLS-1$
				"0", //$NON-NLS-1$
				"0", //$NON-NLS-1$
				names.courseIndex(course.getName()),
				course.getName(),
				Integer.toString(course.getLength()),
				Integer.toString(course.getClimb())
				);
		record.add(Integer.toString(course.nbControls()));
		record.add(rank);
		if( withSplits ) {
			addSplits(runnerData, record);
		}
		record.add(""); //$NON-NLS-1$ // force semi-colon at end of line otherwise RouteGadget can't parse the line
		writer.writeRecord(record);
	}

	private void addSplits(RunnerRaceData runnerData, Collection<String> record) {
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
		switch (status) {
		case OK:
			return "0"; //$NON-NLS-1$
		case DNS:
		case NOS:
		case DUP:
		case UNK:
			return "1"; //$NON-NLS-1$
		case DNF:
			return "2"; //$NON-NLS-1$
		case MP:
			return "3"; //$NON-NLS-1$
		case DSQ:
			return "4"; //$NON-NLS-1$
		case OOT:
		case RUN:
			return "5"; //$NON-NLS-1$
		}
		return "1"; //$NON-NLS-1$
	}
	
	@Override
	public void generateXMLResult(ResultConfig config, String filename)
			throws Exception {
		new SplitXmlExporter(geco()).generateXMLResult(buildResults(config), filename, true);		
	}

	public File getSplitsTemplate() {
		return splitsTemplate;
	}

	public void setSplitsTemplate(File selectedFile) {
		splitsTemplate = selectedFile;
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
