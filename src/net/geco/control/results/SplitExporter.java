/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.CsvWriter;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.context.ContextList;
import net.geco.control.context.GenericContext;
import net.geco.control.context.ResultContext;
import net.geco.control.context.RunnerContext;
import net.geco.control.context.StageContext;
import net.geco.control.results.ResultBuilder.ResultConfig;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;


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
	
	private File splitsTemplate;

	private boolean withBestSplits;

	private int nbColumns;

	
	public SplitExporter(GecoControl gecoControl) {
		super(SplitExporter.class, gecoControl);
		geco().announcer().registerStageListener(this);
		withBestSplits();
		changed(null, null);
	}
	
	public void withBestSplits() {
		withBestSplits = true;
	}
	
	public void withoutBestSplits() {
		withBestSplits = false;
	}

	@Override
	protected String getInternalTemplatePath() {
		return "/resources/formats/results_splits_internal.mustache"; //$NON-NLS-1$
	}

	@Override
	protected String getExternalTemplatePath() {
		return getSplitsTemplate().getAbsolutePath();
	}

	@Override
	protected GenericContext buildDataContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		return buildDataContext(config, nbColumns(), refreshInterval, outputType);
	}
	
	protected GenericContext buildDataContext(ResultConfig config, int nbColumns, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;
		List<Result> results = buildResults(config);

		StageContext stageCtx = new StageContext(
				stage().getName(), isSingleCourseResult, config.showPenalties, refreshInterval, outputType);
		ContextList resultsCollection = stageCtx.createResultsCollection(results.size());
		mergeI18nProperties(stageCtx);
		mergeCustomStageProperties(stageCtx);

		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();
				SplitTime[] bestSplits = withBestSplits ?
						resultBuilder.initializeBestSplits(result, config.resultType) :
						new SplitTime[0];
				Map<RunnerRaceData, SplitTime[]> allSplits = resultBuilder.buildAllNormalSplits(result, bestSplits);

				ResultContext resultCtx =
						resultsCollection.addContext(new ResultContext(result, isSingleCourseResult));
				ContextList rankingCollection = resultCtx.createRankedRunnersCollection();
				ContextList unrankedCollection = resultCtx.createUnrankedRunnersCollection();

				for (RankedRunner rankedRunner : result.getRanking()) {
					SplitTime[] runnerSplitTimes = allSplits.get(rankedRunner.getRunnerData());
					RunnerContext runnerCtx =
							rankingCollection.addContext(RunnerContext.createRankedRunner(rankedRunner, bestTime));
					createRunnerSplitsRowsAndColumns(runnerCtx, runnerSplitTimes, bestSplits, nbColumns);
				}

				for (RunnerRaceData data : result.getUnrankedRunners()) {
					SplitTime[] runnerSplitTimes = allSplits.get(data);
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							RunnerContext runnerCtx =
									unrankedCollection.addContext(RunnerContext.createNCRunner(data));
							createRunnerSplitsRowsAndColumns(runnerCtx, runnerSplitTimes, bestSplits, nbColumns);
						} // else nothing
					} else {
						RunnerContext runnerCtx =
								unrankedCollection.addContext(RunnerContext.createUnrankedRunner(data));
						createRunnerSplitsRowsAndColumns(runnerCtx, runnerSplitTimes, bestSplits, nbColumns);
					}
				}
			}
		}
		return stageCtx;
	}

	public void createRunnerSplitsRowsAndColumns(RunnerContext runnerCtx, SplitTime[] runnerSplitTimes,
													SplitTime[] bestSplits,	int nbColumns) {
		ContextList splitRowsCollection = runnerCtx.createContextList("geco_SplitRows"); //$NON-NLS-1$

		int nbSplitTimes = runnerSplitTimes.length;
		int nbSplitRows = nbSplitTimes / nbColumns;
		nbSplitRows = nbSplitTimes % nbColumns == 0 ? nbSplitRows : nbSplitRows + 1;
		for (int i = 0; i < nbSplitRows; i++) {
			GenericContext rowCtx =	splitRowsCollection.addContext(new GenericContext());
			ContextList controlRow = rowCtx.createContextList("geco_ControlRow", nbColumns); //$NON-NLS-1$
			ContextList timeRow = rowCtx.createContextList("geco_TimeRow", nbColumns); //$NON-NLS-1$
			ContextList splitRow = rowCtx.createContextList("geco_SplitRow", nbColumns); //$NON-NLS-1$

			int rowStart = i * nbColumns;
			int rowEnd = Math.min(rowStart + nbColumns, nbSplitTimes);
			for (int j = rowStart; j < rowEnd; j++) {
				SplitTime splitTime = runnerSplitTimes[j];
				
				GenericContext controlCtx = controlRow.addContext(new GenericContext());
				controlCtx.put("geco_ControlNum", splitTime.seq); //$NON-NLS-1$
				controlCtx.put("geco_ControlCode", splitTime.getBasicCode()); //$NON-NLS-1$
				
				GenericContext timeCtx = timeRow.addContext(new GenericContext());
				boolean isBestTime = withBestSplits && j < bestSplits.length
										&& splitTime.time == bestSplits[j].time;
				timeCtx.put("geco_BestTime?", isBestTime); //$NON-NLS-1$
				timeCtx.put("geco_ControlTime", TimeManager.time(splitTime.time)); //$NON-NLS-1$
				
				GenericContext splitCtx = splitRow.addContext(new GenericContext());
				boolean isBestSplit = withBestSplits && j < bestSplits.length
										&& splitTime.split == bestSplits[j].split;
				String label = ( splitTime.isOK() ) ? TimeManager.time(splitTime.split) : ""; //$NON-NLS-1$
				splitCtx.put("geco_BestSplit?", isBestSplit); //$NON-NLS-1$
				splitCtx.put("geco_SplitTime", label); //$NON-NLS-1$
			}
		}
	}

	@Override
	protected GenericContext buildCustomContext(ResultConfig config, int refreshInterval, OutputType outputType) {
		boolean isSingleCourseResult = config.resultType != ResultType.CategoryResult;
		List<Result> results = buildResults(config);

		StageContext stageCtx = new StageContext(
				stage().getName(), isSingleCourseResult, config.showPenalties, refreshInterval, outputType);
		ContextList resultsCollection = stageCtx.createResultsCollection(results.size());
		mergeI18nProperties(stageCtx);
		mergeCustomStageProperties(stageCtx);

		for (Result result : results) {
			if( ! result.isEmpty() ) {
				long bestTime = result.bestTime();
				SplitTime[] bestSplits = new SplitTime[0];
				Map<RunnerRaceData, SplitTime[]> allSplits = resultBuilder.buildAllNormalSplits(result, bestSplits);

				ResultContext resultCtx =
						resultsCollection.addContext(new ResultContext(result, isSingleCourseResult));
				ContextList rankingCollection = resultCtx.createRankedRunnersCollection();
				ContextList unrankedCollection = resultCtx.createUnrankedRunnersCollection();

				for (RankedRunner rankedRunner : result.getRanking()) {
					SplitTime[] runnerSplitTimes = allSplits.get(rankedRunner.getRunnerData());
					RunnerContext runnerCtx =
							rankingCollection.addContext(RunnerContext.createRankedRunner(rankedRunner, bestTime));
					createRunnerSplitsInline(runnerCtx, runnerSplitTimes);
				}

				for (RunnerRaceData data : result.getUnrankedRunners()) {
					SplitTime[] runnerSplitTimes = allSplits.get(data);
					Runner runner = data.getRunner();
					if( runner.isNC() ) {
						if( config.showNC ) {
							RunnerContext runnerCtx =
									unrankedCollection.addContext(RunnerContext.createNCRunner(data));
							createRunnerSplitsInline(runnerCtx, runnerSplitTimes);
						} // else nothing
					} else {
						RunnerContext runnerCtx =
								unrankedCollection.addContext(RunnerContext.createUnrankedRunner(data));
						createRunnerSplitsInline(runnerCtx, runnerSplitTimes);
					}
				}
			}
		}
		return stageCtx;
	}

	public void createRunnerSplitsInline(RunnerContext runnerCtx, SplitTime[] linearSplits) {
		ContextList splits = runnerCtx.createContextList("geco_RunnerSplits", linearSplits.length); //$NON-NLS-1$
		for (SplitTime splitTime : linearSplits) {
			GenericContext splitCtx = splits.addContext(new GenericContext());
			Trace trace = splitTime.trace;
			if( trace!=null ) {
				splitCtx.put("geco_ControlTrace", splitTime.trace.getCode()); //$NON-NLS-1$
				if( trace.isOK() ) {
					splitCtx.put("geco_ControlStatus", "time"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					if( trace.isAdded() || trace.isSubst() ) {
						splitCtx.put("geco_ControlStatus", "add"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						splitCtx.put("geco_ControlStatus", "miss"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else {
				splitCtx.put("geco_ControlTrace", ""); //$NON-NLS-1$ //$NON-NLS-2$
				splitCtx.put("geco_ControlStatus", "time"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			splitCtx.put("geco_ControlNum", splitTime.seq); //$NON-NLS-1$
			splitCtx.put("geco_ControlTimeS", (splitTime.time / 1000)); //$NON-NLS-1$
			splitCtx.put("geco_ControlTime", TimeManager.time(splitTime.time)); //$NON-NLS-1$
			splitCtx.put("geco_SplitTimeS", (splitTime.split / 1000)); //$NON-NLS-1$
			splitCtx.put("geco_SplitTime", TimeManager.time(splitTime.split)); //$NON-NLS-1$
		}
	}

	@Override
	protected Collection<String> computeCsvRecord(RunnerRaceData runnerData, String resultId, String rank) {
		// edit collection
		ArrayList<String> csvRecord = new ArrayList<String>(super.computeCsvRecord(runnerData, resultId, rank));
		csvRecord.ensureCapacity(csvRecord.size() + 2 * runnerData.getResult().getTrace().length);
		for (SplitTime split: resultBuilder.buildNormalSplits(runnerData, false)) {
			csvRecord.add(split.trace.getBasicCode());
			csvRecord.add(encodeMPPunch(split));
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
			if( ! result.isEmpty() ){
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
		SplitTime[] splits = resultBuilder.buildNormalSplits(runnerData, false);
		for (SplitTime split : splits) {
			record.add(split.trace.getBasicCode());
			record.add(oeSplit(split.time));
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
	
	public int nbColumns() {
		return nbColumns;
	}

	public void setNbColumns(int nb) {
		nbColumns = nb;
	}
	
	public File getSplitsTemplate() {
		return splitsTemplate;
	}

	public void setSplitsTemplate(File selectedFile) {
		if( getSplitsTemplate() != null ) {
			resetTemplate(getExternalTemplatePath());
		}
		splitsTemplate = selectedFile;
	}

	@Override
	public void changed(Stage previous, Stage current) {
		Properties props = stage().getProperties();
		setNbColumns(Integer.parseInt(props.getProperty(splitsNbColumnsProperty(), "12"))); //$NON-NLS-1$
		setSplitsTemplate(new File( props.getProperty(splitsTemplateProperty(),
													  "formats/results_splits.mustache") )); //$NON-NLS-1$
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(splitsNbColumnsProperty(), Integer.toString(nbColumns()));
		if( getSplitsTemplate().exists() ){
			properties.setProperty(splitsTemplateProperty(), getSplitsTemplate().getAbsolutePath());
		}
	}

	@Override
	public void closing(Stage stage) {	}

	public static String splitsNbColumnsProperty() {
		return "SplitsNbColumns"; //$NON-NLS-1$
	}

	public static String splitsTemplateProperty() {
		return "SplitsTemplate"; //$NON-NLS-1$
	}

}
