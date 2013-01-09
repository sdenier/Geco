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

import net.geco.basics.GecoResources;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Pool;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 *
 */
public final class PersistentStore {

	public static final String STORE_FILE = "store.json";

	public static final String JSON_SCHEMA_VERSION = "2.0";
	
	private static final boolean DEBUG = false;
	
	public String getStorePath(String baseDir) {
		return baseDir + GecoResources.sep + STORE_FILE;
	}

	public void saveData(Stage stage) {
		try {
			BufferedWriter writer = GecoResources.getSafeWriterFor(getStorePath(stage.getBaseDir()));
			exportDataToJson(stage, new JacksonSerializer(writer, DEBUG));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void exportDataToJson(Stage stage, JSONSerializer json) throws IOException {
		json.startObject()
			.field(K.VERSION, JSON_SCHEMA_VERSION);
		
		Registry registry = stage.registry();
		exportCourses(json, registry.getCourses());
		exportCategories(json, registry.getCategories());
		exportClubs(json, registry.getClubs());
		exportHeatSets(json, registry.getHeatSets());
		exportRunnersData(json, registry.getRunnersData());
		
		json.idMax(K.MAXID)
			.endObject()
			.close();
	}

	public void exportCourses(JSONSerializer json, Collection<Course> courses) throws IOException {
		json.startArrayField(K.COURSES);
		for (Course course : courses) {
			json.startObject()
				.id(K.ID, course)
				.field(K.NAME, course.getName())
				.field(K.LENGTH, course.getLength())
				.field(K.CLIMB, course.getClimb())
				.startArrayField(K.CODES);
			for (int code : course.getCodes()) { json.value(code); }
			json.endArray()
				.endObject();
		}
		json.endArray();
	}

	public void exportCategories(JSONSerializer json, Collection<Category> categories) throws IOException {
		json.startArrayField(K.CATEGORIES);
		for (Category cat : categories) {
			json.startObject()
				.id(K.ID, cat)
				.field(K.NAME, cat.getShortname())
				.field(K.LONG, cat.getLongname())
				.optRef(K.COURSE, cat.getCourse())
				.endObject();
		}
		json.endArray();		
	}

	public void exportClubs(JSONSerializer json, Collection<Club> clubs) throws IOException {
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

	public void exportHeatSets(JSONSerializer json, Collection<HeatSet> heatsets) throws IOException {
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

	public void exportRunnersData(JSONSerializer json, Collection<RunnerRaceData> runnersData) throws IOException {
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
				.field(K.ERASE, runnerData.getErasetime())
				.field(K.CHECK, runnerData.getControltime())
				.field(K.READ, runnerData.getReadtime())
				.startArrayField(K.PUNCHES);
			for (Punch punch : runnerData.getPunches()) {
				json.value(punch.getCode()).value(punch.getTime());
			}
			json.endArray().endObject();
			
			RunnerResult result = runnerData.getResult();
			json.startObject()
				.field(K.TIME, result.getRacetime())
				.field(K.STATUS, result.getStatus().name())
				.field(K.MPS, result.getNbMPs())
				.field(K.PENALTY, result.getTimePenalty());
			
			Trace[] traceArray = result.getTrace();
			int nbNeut = 0;
			int[] neutralized = new int[traceArray.length];
			json.startArrayField(K.TRACE);
			for (int i = 0; i < traceArray.length; i++) {
				Trace trace = result.getTrace()[i];
				json.value(trace.getCode()).value(trace.getTime());
				if( trace.isNeutralized() ){
					neutralized[nbNeut++] = i;
				}
			}
			json.endArray()
				.startArrayField(K.NEUTRALIZED);
			for (int i = 0; i < nbNeut; i++) {
				json.value(neutralized[i]);
			}
			json.endArray()
				.endObject()
				.endArray();
		}
		json.endArray();		
	}
	

	
	public void loadData(Stage stage, Factory factory) {
		Registry registry = new Registry();
		stage.setRegistry(registry);
		try {
			BufferedReader reader = GecoResources.getSafeReaderFor(getStorePath(stage.getBaseDir()));
			importDataIntoRegistry(new JSONStore(reader, K.MAXID), registry, factory);
			reader.close();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void importDataIntoRegistry(JSONStore store, Registry registry, Factory factory) throws JSONException {
		importCourses(store, registry, factory);
		importCategories(store, registry, factory);
		importClubs(store, registry, factory);
		importHeatSets(store);
		importRunnersData(store, registry, factory);
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
			JSONArray codes = c.getJSONArray(K.CODES);
			int[] codez = new int[codes.length()];
			for (int j = 0; j < codes.length(); j++) {
				codez[j] = codes.getInt(j);
			}
			course.setCodes(codez);
			registry.addCourse(course);
		}
		registry.ensureAutoCourse(factory);
	}
	
	public void importCategories(JSONStore store, Registry registry,
			Factory factory) throws JSONException {
		JSONArray categories = store.getJSONArray(K.CATEGORIES);
		for (int i = 0; i < categories.length(); i++) {
			JSONObject c = categories.getJSONObject(i);
			Category category = store.register(factory.createCategory(), c.getInt(K.ID));
			category.setName(c.getString(K.NAME));
			category.setLongname(c.getString(K.LONG));
			category.setCourse(store.retrieve(c.optInt(K.COURSE, 0), Course.class));
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

	public void importHeatSets(JSONStore store) throws JSONException {
		store.getJSONArray(K.HEATSETS); // TODO
	}

	public void importRunnersData(JSONStore store, Registry registry,
			Factory factory) throws JSONException {
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
			RunnerRaceData ecardData = factory.createRunnerRaceData();
			ecardData.setStarttime(new Date(d.getLong(K.START)));
			ecardData.setFinishtime(new Date(d.getLong(K.FINISH)));
			ecardData.setErasetime(new Date(d.getLong(K.ERASE)));
			ecardData.setControltime(new Date(d.getLong(K.CHECK)));
			ecardData.setReadtime(new Date(d.getLong(K.READ)));
			JSONArray p = d.getJSONArray(K.PUNCHES);
			Punch[] punches = new Punch[p.length() / 2];
			for (int j = 0; j < punches.length; j++) {
				punches[j] = factory.createPunch();
				punches[j].setCode(p.getInt(2 * j));
				punches[j].setTime(new Date(p.getLong(2 * j + 1)));
			}
			ecardData.setPunches(punches);
			ecardData.setRunner(runner);
			registry.addRunnerData(ecardData);
	
			JSONObject r = runnerTuple.getJSONObject(I_RESULT);
			RunnerResult result = factory.createRunnerResult();
			result.setRacetime(r.getLong(K.TIME));
			result.setStatus(Status.valueOf(r.getString(K.STATUS)));
			result.setNbMPs(r.getInt(K.MPS));
			result.setTimePenalty(r.getLong(K.PENALTY));
			JSONArray t = r.getJSONArray(K.TRACE);
			Trace[] trace = new Trace[t.length() / 2];
			for (int j = 0; j < trace.length; j++) {
				trace[j] = factory.createTrace(t.getString(2 * j),
						new Date(t.getLong(2 * j + 1)));
			}
			JSONArray neut = r.getJSONArray(K.NEUTRALIZED);
			for (int j = 0; j < neut.length(); j++) {
				trace[neut.getInt(j)].setNeutralized(true);
			}
			result.setTrace(trace);
			ecardData.setResult(result);
		}
	}

	public static class K {
		
		static {
			if( DEBUG ) {
				START_ID = "startid";
				FIRST = "first";
				LAST = "last";
				ECARD = "ecard";
				CLUB = "club";
				CAT = "cat";
				COURSE = "course";
				ARK = "ark";
				RENT = "rent";
				NC = "nc";

				TIME = "time";
				STATUS = "status";
				MPS = "mps";
				PENALTY = "penalty";
				TRACE = "trace";
				NEUTRALIZED = "neut";
			
				START = "start";
				FINISH = "finish";
				ERASE = "erase";
				READ = "read";
				CHECK = "check";
				PUNCHES = "punches";
			} else {
				START_ID = "i";
				FIRST = "f";
				LAST = "l";
				ECARD = "e";
				CLUB = "u";
				CAT = "t";
				COURSE = "c";
				ARK = "a";
				RENT = "r";
				NC = "n";

				TIME = "t";
				STATUS = "s";
				MPS = "m";
				PENALTY = "p";
				TRACE = "r";
				NEUTRALIZED = "n";
			
				START = "s";
				FINISH = "f";
				ERASE = "e";
				READ = "r";
				CHECK = "c";
				PUNCHES = "p";
			}
		}

		public static final String ID = "id";;
		public static final String MAXID = "maxid";;
		public static final String NAME = "name";
		public static final String VERSION = "version";

		public static final String COURSES = "courses";
		public static final String LENGTH = "length";
		public static final String CLIMB = "climb";
		public static final String CODES = "codes";

		public static final String CATEGORIES = "categories";;
		public static final String LONG = "long";
		public static final String CLUBS = "clubs";
		public static final String SHORT = "short";

		public static final String HEATSETS = "heatsets";
		public static final String RANK = "rank";
		public static final String TYPE = "type";
		public static final String HEATS = "heats";
		public static final String POOLS = "pools";

		public static final String RUNNERS_DATA = "runnersData";

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
		public static final String ERASE;
		public static final String CHECK;
		public static final String READ;
		public static final String PUNCHES;

		public static final String TIME;
		public static final String STATUS;
		public static final String MPS;
		public static final String PENALTY;
		public static final String TRACE;
		public static final String NEUTRALIZED;
	}

}
