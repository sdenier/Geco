/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;

import net.geco.basics.GecoResources;
import net.geco.basics.TimeManager;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.CourseSet;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Pool;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Section;
import net.geco.model.Section.SectionType;
import net.geco.model.SectionTraceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;
import net.geco.model.TraceData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 *
 */
public final class PersistentStore {

	public static final String STORE_FILE = "store.json"; //$NON-NLS-1$

	public static final String JSON_SCHEMA_VERSION = "2.2"; //$NON-NLS-1$
	
	private static final boolean DEBUG = false;
	
	public String getStorePath(String baseDir) {
		return baseDir + GecoResources.sep + STORE_FILE;
	}
	
	public void loadData(Stage stage, Factory factory) {
		Registry registry = new Registry();
		stage.setRegistry(registry);
		try {
			BufferedReader reader =
					GecoResources.getSafeReaderFor(getStorePath(stage.getBaseDir()));
			importDataIntoRegistry(new JSONStore(reader, K.MAXID), registry, factory);
			reader.close();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void importDataIntoRegistry(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		importControls(store, registry);
		importCourseSets(store, registry, factory);
		importCourses(store, registry, factory);
		importCategories(store, registry, factory);
		importClubs(store, registry, factory);
		importHeatSets(store, registry, factory);
		importRunnersData(store, registry, factory);
	}

	public void importControls(JSONStore store, Registry registry) throws JSONException {
		if( store.has(K.CONTROLS) ) { // MIRG 2.x -> 2.2
			JSONArray controls = store.getJSONArray(K.CONTROLS);
			for (int i = 0; i < controls.length(); i++) {
				JSONArray codeData = controls.getJSONArray(i);
				registry.setControlPenalty(codeData.getInt(0), new Date(codeData.getLong(1)));
			}
		}
	}

	public void importCourseSets(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		if( store.has(K.COURSESETS) ) { // MIGR v2.x -> v2.2
			JSONArray coursesets = store.getJSONArray(K.COURSESETS);
			for (int i = 0; i < coursesets.length(); i++) {
				JSONObject c = coursesets.getJSONObject(i);
				CourseSet courseset = store.register(factory.createCourseSet(), c.getInt(K.ID));
				courseset.setName(c.getString(K.NAME));
				registry.addCourseSet(courseset);
			}
		}
	}

	public void importCourses(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		JSONArray courses = store.getJSONArray(K.COURSES);
		for (int i = 0; i < courses.length(); i++) {
			JSONObject c = courses.getJSONObject(i);
			Course course = store.register(factory.createCourse(), c.getInt(K.ID));
			course.setName(c.getString(K.NAME));
			course.setLength(c.getInt(K.LENGTH));
			course.setClimb(c.getInt(K.CLIMB));
			course.setMassStartTime(new Date(c.optLong(K.START, TimeManager.NO_TIME_l))); // MIGR v2.x -> v2.2
			course.setCourseSet(store.retrieve(c.optInt(K.COURSESET, 0), CourseSet.class));
			JSONArray codez = c.getJSONArray(K.CODES);
			int[] codes = new int[codez.length()];
			for (int j = 0; j < codes.length; j++) {
				codes[j] = codez.getInt(j);
			}
			course.setCodes(codes);
			if( c.has(K.SECTIONS) ) {
				JSONArray sectionz = c.getJSONArray(K.SECTIONS);
				for (int j = 0; j < sectionz.length(); j++) {
					JSONObject sectionTuple = sectionz.getJSONObject(j);
					Section section = store.register(factory.createSection(), sectionTuple.getInt(K.ID));
					section.setStartIndex(sectionTuple.getInt(K.START_ID));
					section.setName(sectionTuple.getString(K.NAME));
					section.setType(SectionType.valueOf(sectionTuple.getString(K.TYPE)));
					section.setNeutralized(sectionTuple.optBoolean(K.NEUTRALIZED, false));
					course.putSection(section);
				}
				course.refreshSectionCodes();
			}
			registry.addCourse(course);
		}
		registry.ensureAutoCourse(factory);
	}
	
	public void importCategories(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		JSONArray categories = store.getJSONArray(K.CATEGORIES);
		for (int i = 0; i < categories.length(); i++) {
			JSONObject c = categories.getJSONObject(i);
			Category category = store.register(factory.createCategory(), c.getInt(K.ID));
			category.setName(c.getString(K.NAME));
			category.setLongname(c.getString(K.LONG));
			category.setCourse(store.retrieve(c.optInt(K.COURSE, 0), Course.class));
			category.setCourseSet(store.retrieve(c.optInt(K.COURSESET, 0), CourseSet.class));
			registry.addCategory(category);
		}
	}

	public void importClubs(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		JSONArray clubs = store.getJSONArray(K.CLUBS);
		for (int i = 0; i < clubs.length(); i++) {
			JSONObject c = clubs.getJSONObject(i);
			Club club = store.register(factory.createClub(), c.getInt(K.ID));
			club.setName(c.getString(K.NAME));
			club.setShortname(c.getString(K.SHORT));
			registry.addClub(club);
		}
	}

	public void importHeatSets(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		JSONArray heatsets = store.getJSONArray(K.HEATSETS);
		for (int i = 0; i < heatsets.length(); i++) {
			JSONObject h = heatsets.getJSONObject(i);
			HeatSet heatset = factory.createHeatSet();
			heatset.setName(h.getString(K.NAME));
			heatset.setQualifyingRank(h.getInt(K.RANK));
			heatset.setSetType(ResultType.valueOf(h.getString(K.TYPE)));
			JSONArray heatz = h.getJSONArray(K.HEATS);
			String[] heats = new String[heatz.length()];
			for (int j = 0; j < heats.length; j++) {
				heats[j] = heatz.getString(j);
			}
			heatset.setHeatNames(heats);
			JSONArray poolz = h.getJSONArray(K.POOLS);
			Pool[] pools = new Pool[poolz.length()];
			for (int j = 0; j < pools.length; j++) {
				pools[j] = store.retrieve(poolz.getInt(j), Pool.class);
			}
			heatset.setSelectedPools(pools);
			registry.addHeatSet(heatset);
		}
	}

	public void importRunnersData(JSONStore store, Registry registry, Factory factory)
			throws JSONException {
		final int I_RUNNER = 0;
		final int I_ECARD = 1;
		final int I_RESULT = 2;
		JSONArray runnersData = store.getJSONArray(K.RUNNERS_DATA);
		for (int i = 0; i < runnersData.length(); i++) {
			JSONArray runnerTuple = runnersData.getJSONArray(i);
	
			JSONObject c = runnerTuple.getJSONObject(I_RUNNER);
			Runner runner = factory.createRunner();
			runner.setStartId(c.getInt(K.START_ID));
			runner.setFirstname(c.getString(K.FIRST));
			runner.setLastname(c.getString(K.LAST));
			runner.setEcard(c.getString(K.ECARD));
			runner.setClub(store.retrieve(c.getInt(K.CLUB), Club.class));
			runner.setCategory(store.retrieve(c.getInt(K.CAT), Category.class));
			runner.setCourse(store.retrieve(c.getInt(K.COURSE), Course.class));
			runner.setRegisteredStarttime(new Date(c.getLong(K.START)));
			runner.setArchiveId((Integer) c.opt(K.ARK));
			runner.setRentedEcard(c.optBoolean(K.RENT));
			runner.setNC(c.optBoolean(K.NC));
			registry.addRunner(runner);
	
			JSONObject d = runnerTuple.getJSONObject(I_ECARD);
			RunnerRaceData raceData = factory.createRunnerRaceData();
			raceData.setStarttime(new Date(d.getLong(K.START)));
			raceData.setFinishtime(new Date(d.getLong(K.FINISH)));
			raceData.setControltime(new Date(d.getLong(K.CHECK)));
			raceData.setReadtime(new Date(d.getLong(K.READ)));
			JSONArray p = d.getJSONArray(K.PUNCHES);
			Punch[] punches = new Punch[p.length() / 2];
			for (int j = 0; j < punches.length; j++) {
				punches[j] = factory.createPunch();
				punches[j].setCode(p.getInt(2 * j));
				punches[j].setTime(new Date(p.getLong(2 * j + 1)));
			}
			raceData.setPunches(punches);
			raceData.setRunner(runner);
			registry.addRunnerData(raceData);
	
			JSONObject r = runnerTuple.getJSONObject(I_RESULT);
			TraceData traceData = factory.createTraceData();
			traceData.setNbMPs(r.getInt(K.MPS));
			traceData.setNbExtraneous(r.optInt(K.EXTRA)); // MIGR v2.x -> v2.3
			JSONArray t = r.getJSONArray(K.TRACE);
			Trace[] trace = new Trace[t.length() / 2];
			for (int j = 0; j < trace.length; j++) {
				trace[j] = factory.createTrace(t.getString(2 * j),
						new Date(t.getLong(2 * j + 1)));
			}
			if( r.has(K.SECTION_DATA) ) {
				SectionTraceData sectionData = (SectionTraceData) traceData;
				JSONArray sections = r.getJSONArray(K.SECTION_DATA);
				for (int j = 0; j < sections.length(); j++) {
					JSONArray section = sections.getJSONArray(j);
					sectionData.putSectionAt(store.retrieve(section.getInt(0), Section.class),
											 section.getInt(1));
				}
			}
			JSONArray neut = r.getJSONArray(K.NEUTRALIZED);
			for (int j = 0; j < neut.length(); j++) {
				trace[neut.getInt(j)].setNeutralized(true);
			}
			traceData.setTrace(trace);
			raceData.setTraceData(traceData);
			
			RunnerResult result = factory.createRunnerResult();
			result.setRaceTime(r.optLong(K.RACE_TIME, TimeManager.NO_TIME_l)); // MIGR v2.x -> v2.2
			result.setResultTime(r.getLong(K.TIME));
			result.setStatus(Status.valueOf(r.getString(K.STATUS)));
			result.setTimePenalty(r.getLong(K.PENALTY));
			result.setManualTimePenalty(r.optLong(K.MANUAL_PENALTY, 0)); // MIGR v2.x -> v2.3
			raceData.setResult(result);
		}
	}
	
	
	public void saveData(Stage stage) {
		try {
			BufferedWriter writer =
					GecoResources.getSafeWriterFor(getStorePath(stage.getBaseDir()));
			exportDataToJson(stage, new JacksonSerializer(writer, DEBUG));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exportDataToJson(Stage stage, JSONSerializer json)
			throws IOException {
		json.startObject()
			.field(K.VERSION, JSON_SCHEMA_VERSION);
		Registry registry = stage.registry();
		exportControls(json, registry);
		exportCourseSets(json, registry.getCourseSets());
		exportCourses(json, registry.getCourses());
		exportCategories(json, registry.getCategories());
		exportClubs(json, registry.getClubs());
		exportHeatSets(json, registry.getHeatSets());
		exportRunnersData(json, registry.getRunnersData());
		json.idMax(K.MAXID)
			.endObject()
			.close();
	}

	public void exportControls(JSONSerializer json, Registry registry) throws IOException {
		json.startArrayField(K.CONTROLS);
		for (Integer code : registry.getControls()) {
			json.startArray().value(code).value(registry.getControlPenalty(code)).endArray();
		}
		json.endArray();
	}

	public void exportCourseSets(JSONSerializer json, Collection<CourseSet> coursesets) throws IOException {
		json.startArrayField(K.COURSESETS);
		for (CourseSet courseset : coursesets) {
			json.startObject()
				.id(K.ID, courseset)
				.field(K.NAME, courseset.getName())
				.endObject();
		}
		json.endArray();
	}

	public void exportCourses(JSONSerializer json, Collection<Course> courses)
			throws IOException {
		json.startArrayField(K.COURSES);
		for (Course course : courses) {
			json.startObject()
				.id(K.ID, course)
				.field(K.NAME, course.getName())
				.field(K.LENGTH, course.getLength())
				.field(K.CLIMB, course.getClimb())
				.field(K.START, course.getMassStartTime())
				.optRef(K.COURSESET, course.getCourseSet())
				.startArrayField(K.CODES);
			for (int code : course.getCodes()) { json.value(code); }
			json.endArray().startArrayField(K.SECTIONS);
			for (Section section : course.getSections()) {
				json.startObject()
					.id(K.ID, section)
					.field(K.START_ID, section.getStartIndex())
					.field(K.NAME, section.getName())
					.field(K.TYPE, section.getType().name())
					.optField(K.NEUTRALIZED, section.neutralized())
					.endObject();
			}
			json.endArray()
				.endObject();
		}
		json.endArray();
	}

	public void exportCategories(JSONSerializer json, Collection<Category> categories)
			throws IOException {
		json.startArrayField(K.CATEGORIES);
		for (Category cat : categories) {
			json.startObject()
				.id(K.ID, cat)
				.field(K.NAME, cat.getShortname())
				.field(K.LONG, cat.getLongname())
				.optRef(K.COURSE, cat.getCourse())
				.optRef(K.COURSESET, cat.getCourseSet())
				.endObject();
		}
		json.endArray();		
	}

	public void exportClubs(JSONSerializer json, Collection<Club> clubs)
			throws IOException {
		json.startArrayField(K.CLUBS);
		for (Club club : clubs) {
			json.startObject()
				.id(K.ID, club)
				.field(K.NAME, club.getName())
				.field(K.SHORT, club.getShortname())
				.endObject();
		}
		json.endArray();
	}

	public void exportHeatSets(JSONSerializer json, Collection<HeatSet> heatsets)
			throws IOException {
		json.startArrayField(K.HEATSETS);
		for (HeatSet heatset : heatsets) {
			json.startObject()
				.field(K.NAME, heatset.getName())
				.field(K.RANK, heatset.getQualifyingRank())
				.field(K.TYPE, heatset.getSetType().name())
				.startArrayField(K.HEATS);
			for (String heatname : heatset.getHeatNames()) { json.value(heatname); }
			json.endArray()
				.startArrayField(K.POOLS);
			for (Pool pool : heatset.getSelectedPools()) { json.ref(pool); }
			json.endArray()
				.endObject();
		}
		json.endArray();		
	}

	public void exportRunnersData(JSONSerializer json, Collection<RunnerRaceData> runnersData)
			throws IOException {
		json.startArrayField(K.RUNNERS_DATA);
		for (RunnerRaceData runnerData : runnersData) {
			json.startArray();
			Runner runner = runnerData.getRunner();
			json.startObject()
				.field(K.START_ID, runner.getStartId())
				.field(K.FIRST, runner.getFirstname())
				.field(K.LAST, runner.getLastname())
				.field(K.ECARD, runner.getEcard())
				.ref(K.CLUB, runner.getClub())
				.ref(K.CAT, runner.getCategory())
				.ref(K.COURSE, runner.getCourse())
				.field(K.START, runner.getRegisteredStarttime())
				.optField(K.ARK, runner.getArchiveId())
				.optField(K.RENT, runner.rentedEcard())
				.optField(K.NC, runner.isNC())
				.endObject();
			
			json.startObject()
				.field(K.START, runnerData.getStarttime())
				.field(K.FINISH, runnerData.getFinishtime())
				.field(K.CHECK, runnerData.getControltime())
				.field(K.READ, runnerData.getReadtime())
				.startArrayField(K.PUNCHES);
			for (Punch punch : runnerData.getPunches()) {
				json.value(punch.getCode()).value(punch.getTime());
			}
			json.endArray().endObject();
			
			TraceData traceData = runnerData.getTraceData();
			RunnerResult result = runnerData.getResult();
			json.startObject()
				.field(K.TIME, result.getResultTime())
				.field(K.STATUS, result.getStatus().name())
				.field(K.MPS, traceData.getNbMPs())
				.field(K.EXTRA, traceData.getNbExtraneous())
				.field(K.PENALTY, result.getTimePenalty())
				.field(K.MANUAL_PENALTY, result.getManualTimePenalty())
				.field(K.RACE_TIME, result.getRaceTime());
			
			Trace[] traceArray = traceData.getTrace();
			int nbNeut = 0;
			int[] neutralized = new int[traceArray.length];
			json.startArrayField(K.TRACE);
			for (int i = 0; i < traceArray.length; i++) {
				Trace trace = traceData.getTrace()[i];
				json.value(trace.getCode()).value(trace.getTime());
				if( trace.isNeutralized() ){
					neutralized[nbNeut++] = i;
				}
			}
			json.endArray();
			if( traceData.hasSectionData() ) {
				json.startArrayField(K.SECTION_DATA);
				for (Entry<Integer, Section> section : ((SectionTraceData) traceData).getSectionData() ) {
					json.startArray()
						.ref(section.getValue())
						.value(section.getKey())
						.endArray();
				}
				json.endArray();
			}
			json.startArrayField(K.NEUTRALIZED);
			for (int i = 0; i < nbNeut; i++) {
				json.value(neutralized[i]);
			}
			json.endArray()
				.endObject()
				.endArray();
		}
		json.endArray();		
	}

	public static class K {

		public static final String ID = "id";; //$NON-NLS-1$
		public static final String MAXID = "maxid";; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String VERSION = "version"; //$NON-NLS-1$

		public static final String CONTROLS = "controls"; //$NON-NLS-1$
		public static final String COURSES = "courses"; //$NON-NLS-1$
		public static final String LENGTH = "length"; //$NON-NLS-1$
		public static final String CLIMB = "climb"; //$NON-NLS-1$
		public static final String CODES = "codes"; //$NON-NLS-1$
		public static final String SECTIONS = "sections"; //$NON-NLS-1$
		public static final String COURSESETS = "coursesets"; //$NON-NLS-1$
		public static final String COURSESET = "courseset"; //$NON-NLS-1$

		public static final String CATEGORIES = "categories";; //$NON-NLS-1$
		public static final String LONG = "long"; //$NON-NLS-1$
		public static final String CLUBS = "clubs"; //$NON-NLS-1$
		public static final String SHORT = "short"; //$NON-NLS-1$

		public static final String HEATSETS = "heatsets"; //$NON-NLS-1$
		public static final String RANK = "rank"; //$NON-NLS-1$
		public static final String TYPE = "type"; //$NON-NLS-1$
		public static final String HEATS = "heats"; //$NON-NLS-1$
		public static final String POOLS = "pools"; //$NON-NLS-1$

		public static final String RUNNERS_DATA = "runnersData"; //$NON-NLS-1$

		public static final String START_ID;
		public static final String FIRST;
		public static final String LAST;
		public static final String ECARD;
		public static final String CLUB;
		public static final String CAT;
		public static final String COURSE;
		public static final String ARK;
		public static final String NC;
		public static final String RENT;

		public static final String START;
		public static final String FINISH;
		public static final String CHECK;
		public static final String READ;
		public static final String PUNCHES;

		public static final String TIME;
		public static final String STATUS;
		public static final String MPS;
		public static final String EXTRA;
		public static final String PENALTY;
		public static final String MANUAL_PENALTY;
		public static final String RACE_TIME;
		public static final String TRACE;
		public static final String SECTION_DATA;
		public static final String NEUTRALIZED;
		
		static {
			if( DEBUG ) {
				START_ID = "startid"; //$NON-NLS-1$
				FIRST = "first"; //$NON-NLS-1$
				LAST = "last"; //$NON-NLS-1$
				ECARD = "ecard"; //$NON-NLS-1$
				CLUB = "club"; //$NON-NLS-1$
				CAT = "cat"; //$NON-NLS-1$
				COURSE = "course"; //$NON-NLS-1$
				ARK = "ark"; //$NON-NLS-1$
				RENT = "rent"; //$NON-NLS-1$
				NC = "nc"; //$NON-NLS-1$
			
				START = "start"; //$NON-NLS-1$
				FINISH = "finish"; //$NON-NLS-1$
				READ = "read"; //$NON-NLS-1$
				CHECK = "check"; //$NON-NLS-1$
				PUNCHES = "punches"; //$NON-NLS-1$

				TIME = "time"; //$NON-NLS-1$
				STATUS = "status"; //$NON-NLS-1$
				MPS = "mps"; //$NON-NLS-1$
				EXTRA = "extra"; //$NON-NLS-1$
				PENALTY = "penalty"; //$NON-NLS-1$
				MANUAL_PENALTY = "manual"; //$NON-NLS-1$
				RACE_TIME = "running"; //$NON-NLS-1$
				TRACE = "trace"; //$NON-NLS-1$
				SECTION_DATA = "section_data"; //$NON-NLS-1$
				NEUTRALIZED = "neut"; //$NON-NLS-1$
			} else {
				START_ID = "i"; //$NON-NLS-1$
				FIRST = "f"; //$NON-NLS-1$
				LAST = "l"; //$NON-NLS-1$
				ECARD = "e"; //$NON-NLS-1$
				CLUB = "u"; //$NON-NLS-1$
				CAT = "t"; //$NON-NLS-1$
				COURSE = "c"; //$NON-NLS-1$
				ARK = "a"; //$NON-NLS-1$
				RENT = "r"; //$NON-NLS-1$
				NC = "n"; //$NON-NLS-1$
			
				START = "s"; //$NON-NLS-1$
				FINISH = "f"; //$NON-NLS-1$
				READ = "r"; //$NON-NLS-1$
				CHECK = "c"; //$NON-NLS-1$
				PUNCHES = "p"; //$NON-NLS-1$

				TIME = "t"; //$NON-NLS-1$
				STATUS = "s"; //$NON-NLS-1$
				MPS = "m"; //$NON-NLS-1$
				EXTRA = "x"; //$NON-NLS-1$
				PENALTY = "p"; //$NON-NLS-1$
				MANUAL_PENALTY = "b"; //$NON-NLS-1$
				RACE_TIME = "u"; //$NON-NLS-1$
				TRACE = "r"; //$NON-NLS-1$
				SECTION_DATA = "o"; //$NON-NLS-1$
				NEUTRALIZED = "n"; //$NON-NLS-1$
			}
		}
	}

}
