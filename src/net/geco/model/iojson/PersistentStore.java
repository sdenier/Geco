/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.geco.basics.GecoResources;
import net.geco.control.Checker;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
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
import org.json.JSONTokener;
import org.json.JSONWriter;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 *
 */
public class PersistentStore {
	
	private static final String MAXID = "maxid";
	
	private static final String NEUTRALIZED = "n";
	private static final String TRACE = "r";
	private static final String PENALTY = "p";
	private static final String MPS = "m";
	private static final String STATUS = "s";
	private static final String TIME = "t";
	
	private static final String PUNCHES = "p";
	private static final String READ = "r";
	private static final String CHECK = "c";
	private static final String ERASE = "e";
	private static final String FINISH = "f";
	
	private static final String NC = "n";
	private static final String RENT = "r";
	private static final String ARK = "a";
	private static final String START = "s";
	private static final String CAT = "t";
	private static final String CLUB = "u";
	private static final String ECARD = "e";
	private static final String LAST = "l";
	private static final String FIRST = "f";
	private static final String COURSE = "c";
	private static final String START_ID = "i";
	
	private static final String ID = "id";
	private static final String COURSES = "courses";
	private static final String PROPERTIES = "properties";
	private static final String VERSION = "version";
	private static final String STAGE = "stage";
	private static final String ZEROHOUR = "zerohour";
	private static final String BASEDIR = "basedir";
	private static final String NAME = "name";

	private int id;
	private HashMap<Object, Integer> idMap;


	public PersistentStore() {
		id = 0;
		idMap = new HashMap<Object, Integer>();
	}
	
	public int idFor(Object o) {
		if( ! idMap.containsKey(o) ) {
			idMap.put(o, ++id);
		}
		return idMap.get(o);
	}
	
	public int refFor(Object o) {
		return idFor(o);
	}

	public void storeData(Stage stage) {
		try {
			String datafile = "store.json";
			BufferedWriter writer = GecoResources.getSafeWriterFor(stage.getBaseDir() + GecoResources.sep + datafile);

			JSONWriter json = new JSONWriter(writer);
			json.object()
				.key(VERSION).value("2.0");
			writer.newLine();
			json.key(STAGE).object();
			writer.newLine();
			json.key(NAME).value(stage.getName())
				.key(BASEDIR).value(stage.getBaseDir())
				.key(ZEROHOUR).value(stage.getZeroHour())
				.endObject();
			writer.newLine();
			json.key(PROPERTIES).object().endObject();
			writer.newLine();
			json.key(COURSES).array();
			writer.newLine();
			for (Course c : stage.registry().getCourses()) {
				json.object()
					.key(NAME).value(c.getName())
					.key(ID).value(idFor(c))
					.key("length").value(c.getLength())
					.key("climb").value(c.getClimb())
					.key("codes").array();
				for (int code : c.getCodes()) {
					json.value(code);
				}
				json.endArray()
					.endObject();
				writer.newLine();
			}
			json.endArray();
			writer.newLine();
			json.key("categories").array();
			writer.newLine();
			for (Category c : stage.registry().getCategories()) {
				json.object()
					.key(NAME).value(c.getShortname())
					.key(ID).value(idFor(c))
					.key("long").value(c.getLongname());
				if( c.getCourse() != null ){
					json.key(COURSE).value(refFor(c.getCourse()));
				}
				json.endObject();
				writer.newLine();				
			}
			json.endArray();
			writer.newLine();
			json.key("clubs").array();
			writer.newLine();
			for (Club c : stage.registry().getClubs()) {
				json.object()
					.key(NAME).value(c.getName())
					.key(ID).value(idFor(c))
					.key("short").value(c.getShortname())
					.endObject();
				writer.newLine();		
			}
			json.endArray();
			writer.newLine();
			json.key("heatsets").array();
			writer.newLine();
			for (HeatSet h : stage.registry().getHeatSets()) {
				json.object()
					.key(NAME).value(h.getName())
					.key("rank").value(h.getQualifyingRank())
					.key("type").value(h.getSetType())
					.key("heats").array().endArray()
					.key("pools").array().endArray()
					.endObject();
				writer.newLine();		
			}
			json.endArray();
			writer.newLine();
			json.key("runnersData").array();
			writer.newLine();
			for (RunnerRaceData runnerData : stage.registry().getRunnersData()) {
				json.array();
				Runner runner = runnerData.getRunner();
				json.object()
					.key(START_ID).value(runner.getStartId())
					.key(FIRST).value(runner.getFirstname())
					.key(LAST).value(runner.getLastname())
					.key(ECARD).value(runner.getEcard())
					.key(CLUB).value(refFor(runner.getClub()))
					.key(CAT).value(refFor(runner.getCategory()))
					.key(COURSE).value(refFor(runner.getCourse()))
					.key(START).value(runner.getRegisteredStarttime().getTime());
				if( runner.getArchiveId() != null ){
					json.key(ARK).value(runner.getArchiveId());
				}
				if( runner.rentedEcard() ){
					json.key(RENT).value(true);
				}
				if( runner.isNC() ){
					json.key(NC).value(true);
				}
				json.endObject();
				writer.newLine();
				json.object()
					.key(START).value(runnerData.getStarttime().getTime())
					.key(FINISH).value(runnerData.getFinishtime().getTime())
					.key(ERASE).value(runnerData.getErasetime().getTime())
					.key(CHECK).value(runnerData.getControltime().getTime())
					.key(READ).value(runnerData.getReadtime().getTime())
					.key(PUNCHES).array();
				for (Punch punch : runnerData.getPunches()) {
					json.value(punch.getCode()).value(punch.getTime().getTime());
				}
				json.endArray().endObject();
				writer.newLine();
				RunnerResult result = runnerData.getResult();
				json.object()
					.key(TIME).value(result.getRacetime())
					.key(STATUS).value(result.getStatus())
					.key(MPS).value(result.getNbMPs())
					.key(PENALTY).value(result.getTimePenalty());
				
				JSONArray jTrace = new JSONArray();
				JSONArray jNeutralized = new JSONArray();
				for (int i = 0; i < result.getTrace().length; i++) {
					Trace trace = result.getTrace()[i];
					jTrace.put(trace.getCode()).put(trace.getTime().getTime());
					if( trace.isNeutralized() ){
						jNeutralized.put(i);
					}
				}
				json.key(TRACE).value(jTrace);
				json.key(NEUTRALIZED).value(jNeutralized);
				json.endObject().endArray();
				writer.newLine();
			}
			json.endArray();
			writer.newLine();
			json.key(MAXID).value(id);
			json.endObject();
			writer.newLine();
			writer.close();
			
			backupData(stage.getBaseDir(), datafile, "store.zip");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void backupData(String basedir, String datafile, String backupname) {
		try {
			ZipOutputStream zipStream = 
								new ZipOutputStream(new FileOutputStream(basedir + GecoResources.sep + backupname));
			writeZipEntry(zipStream, datafile, basedir);	
			zipStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeZipEntry(ZipOutputStream zipStream, String filename, String basedir)
			throws IOException, FileNotFoundException {
		ZipEntry zipEntry = new ZipEntry(filename);
		zipStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[4096];
		BufferedInputStream inputStream =
						new BufferedInputStream(new FileInputStream(basedir + GecoResources.sep + filename));
		int len;
		while( (len = inputStream.read(buffer)) != -1 ) {
			zipStream.write(buffer, 0, len);
		}
		inputStream.close();
		zipStream.closeEntry();
	}

	public Stage loadData(String testDir, Factory factory, Checker checker) {
		Stage newStage = factory.createStage();
		try {
			BufferedReader reader = GecoResources.getSafeReaderFor(testDir + GecoResources.sep + "store.json");
			JSONObject store = new JSONObject(new JSONTokener(reader));
			
//			loadStageProperties(newStage, baseDir);
			importDataIntoRegistry(store, newStage, factory);
//			checker.postInitialize(newStage); // post initialization
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newStage;
	}

	private void importDataIntoRegistry(JSONObject store, Stage newStage, Factory factory) {
		Registry registry = new Registry();
		newStage.setRegistry(registry);
		try {
			Object[] refMap = new Object[store.getInt(MAXID) + 1];
			
			JSONArray courses = store.getJSONArray(COURSES);
			for (int i = 0; i < courses.length(); i++) {
				JSONObject c = courses.getJSONObject(i);
				Course course = factory.createCourse();
				course.setName( c.getString(NAME) );
				course.setLength( c.getInt("length") );
				course.setClimb( c.getInt("climb") );
				JSONArray codes = c.getJSONArray("codes");
				int[] codez = new int[codes.length()];
				for (int j = 0; j < codes.length(); j++) {
					codez[j] = codes.getInt(j);
				}
				course.setCodes(codez);
				refMap[c.getInt(ID)] = course;
				registry.addCourse(course);
			}
			registry.ensureAutoCourse(factory);
			
			JSONArray categories = store.getJSONArray("categories");
			for (int i = 0; i < categories.length(); i++) {
				JSONObject c = categories.getJSONObject(i);
				Category category = factory.createCategory();
				category.setName(c.getString(NAME));
				category.setLongname(c.getString("long"));
				category.setCourse((Course) refMap[c.optInt(COURSE)]); // ref[0] = null
				refMap[c.getInt(ID)] = category;
				registry.addCategory(category);
			}
			
			JSONArray clubs = store.getJSONArray("clubs");
			for (int i = 0; i < clubs.length(); i++) {
				JSONObject c = clubs.getJSONObject(i);
				Club club = factory.createClub();
				club.setName(c.getString(NAME));
				club.setShortname(c.getString("short"));
				refMap[c.getInt(ID)] = club;
				registry.addClub(club);
			}
			
			store.getJSONArray("heatsets");
			
			JSONArray runnersData = store.getJSONArray("runnersData");
			for (int i = 0; i < runnersData.length(); i++) {
				JSONArray runnerData = runnersData.getJSONArray(i);
				
				JSONObject r = runnerData.getJSONObject(0);
				Runner runner = factory.createRunner();
				runner.setStartId(r.getInt(START_ID));
				runner.setFirstname(r.getString(FIRST));
				runner.setLastname(r.getString(LAST));
				runner.setEcard(r.getString(ECARD));
				runner.setClub((Club) refMap[r.getInt(CLUB)]);
				runner.setCategory((Category) refMap[r.getInt(CAT)]);
				runner.setCourse((Course) refMap[r.getInt(COURSE)]);
				runner.setRegisteredStarttime(new Date(r.getLong(START)));
				runner.setArchiveId((Integer) r.opt(ARK));
				// TODO: nc, rented
				registry.addRunner(runner);
				
				JSONObject d = runnerData.getJSONObject(1);
				RunnerRaceData ecardData = factory.createRunnerRaceData();
				ecardData.setRunner(runner);
				ecardData.setStarttime(new Date(d.getLong(START)));
				ecardData.setFinishtime(new Date(d.getLong(FINISH)));
				ecardData.setErasetime(new Date(d.getLong(ERASE)));
				ecardData.setControltime(new Date(d.getLong(CHECK)));
				ecardData.setReadtime(new Date(d.getLong(READ)));
				JSONArray p = d.getJSONArray(PUNCHES);
				Punch[] punches = new Punch[p.length() / 2];
				ecardData.setPunches(punches);
				for (int j = 0; j < punches.length; j++) {
					punches[j] = factory.createPunch();
					punches[j].setCode(p.getInt(2*j));
					punches[j].setTime(new Date(p.getLong(2*j + 1)));
				}
				registry.addRunnerData(ecardData);
				
				JSONObject res = runnerData.getJSONObject(2);
				RunnerResult result = factory.createRunnerResult();
				result.setRacetime(res.getLong(TIME));
				result.setStatus(Status.valueOf(res.getString(STATUS)));
				result.setNbMPs(res.getInt(MPS));
				result.setTimePenalty(res.getLong(PENALTY));
				JSONArray t = res.getJSONArray(TRACE);
				Trace[] trace = new Trace[t.length() / 2];
				result.setTrace(trace);
				for (int j = 0; j < trace.length; j++) {
					trace[j] = factory.createTrace(t.getString(2*j), new Date(t.getLong(2*j + 1)));
				}
				JSONArray neut = res.getJSONArray(NEUTRALIZED);
				for (int j = 0; j < neut.length(); j++) {
					trace[neut.getInt(j)].setNeutralized(true);
				}
				ecardData.setResult(result);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
