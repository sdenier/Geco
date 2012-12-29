/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 * 
 */
public class JacksonStore {

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

	public JacksonStore() {
		id = 0;
		idMap = new HashMap<Object, Integer>();
	}

	public int idFor(Object o) {
		if (!idMap.containsKey(o)) {
			idMap.put(o, ++id);
		}
		return idMap.get(o);
	}

	public int refFor(Object o) {
		return idFor(o);
	}

	public void storeData(Stage stage) {
		String datafile = "store.json";

		try {
			BufferedWriter writer = GecoResources.getSafeWriterFor(stage.getBaseDir() + GecoResources.sep + datafile);
			JsonFactory jsonFactory = new JsonFactory();
			jsonFactory.configure(Feature.QUOTE_FIELD_NAMES, false);
			JsonGenerator json = jsonFactory.createGenerator(writer);
//			json.useDefaultPrettyPrinter();
			
			json.writeStartObject();
			json.writeStringField(VERSION, "2.0");

			json.writeObjectFieldStart(STAGE);
			json.writeStringField(NAME, stage.getName());
			json.writeStringField(BASEDIR, stage.getBaseDir());
			json.writeNumberField(ZEROHOUR, stage.getZeroHour());
			json.writeEndObject();

			json.writeObjectFieldStart(PROPERTIES);
			json.writeEndObject();

			json.writeArrayFieldStart(COURSES);
			for (Course c : stage.registry().getCourses()) {
				json.writeStartObject();
				json.writeStringField(NAME, c.getName());
				json.writeNumberField(ID, idFor(c));
				json.writeNumberField("length", c.getLength());
				json.writeNumberField("climb", c.getClimb());
				json.writeArrayFieldStart("codes");
				for (int code : c.getCodes()) {
					json.writeNumber(code);
				}
				json.writeEndArray();
				json.writeEndObject();
			}
			json.writeEndArray();

			json.writeArrayFieldStart("categories");
			for (Category c : stage.registry().getCategories()) {
				json.writeStartObject();
				json.writeStringField(NAME, c.getShortname());
				json.writeNumberField(ID, idFor(c));
				json.writeStringField("long", c.getLongname());
				if (c.getCourse() != null) {
					json.writeNumberField(COURSE, refFor(c.getCourse()));
				}
				json.writeEndObject();
			}
			json.writeEndArray();

			json.writeArrayFieldStart("clubs");
			for (Club c : stage.registry().getClubs()) {
				json.writeStartObject();
				json.writeStringField(NAME, c.getName());
				json.writeNumberField(ID, idFor(c));
				json.writeStringField("short", c.getShortname());
				json.writeEndObject();
			}
			json.writeEndArray();

			json.writeArrayFieldStart("heatsets");
			for (HeatSet h : stage.registry().getHeatSets()) {
				json.writeStartObject();
				json.writeEndObject();
			}
			json.writeEndArray();

			json.writeArrayFieldStart("runnersData");
			for (RunnerRaceData runnerData : stage.registry().getRunnersData()) {
				json.writeStartArray();
				Runner runner = runnerData.getRunner();
				json.writeStartObject();
				json.writeNumberField(START_ID, runner.getStartId());
				json.writeStringField(FIRST, runner.getFirstname());
				json.writeStringField(LAST, runner.getLastname());
				json.writeStringField(ECARD, runner.getEcard());
				json.writeNumberField(CLUB, refFor(runner.getClub()));
				json.writeNumberField(CAT, refFor(runner.getCategory()));
				json.writeNumberField(COURSE, refFor(runner.getCourse()));
				json.writeNumberField(START, runner.getRegisteredStarttime()
						.getTime());
				if (runner.getArchiveId() != null) {
					json.writeNumberField(ARK, runner.getArchiveId());
				}
				if (runner.rentedEcard()) {
					json.writeBooleanField(RENT, true);
				}
				if (runner.isNC()) {
					json.writeBooleanField(NC, true);
				}
				json.writeEndObject();
				
				json.writeStartObject();
				json.writeNumberField(START, runnerData.getStarttime()
						.getTime());
				json.writeNumberField(FINISH, runnerData.getFinishtime()
						.getTime());
				json.writeNumberField(ERASE, runnerData.getErasetime()
						.getTime());
				json.writeNumberField(CHECK, runnerData.getControltime()
						.getTime());
				json.writeNumberField(READ, runnerData.getReadtime().getTime());
				json.writeArrayFieldStart(PUNCHES);
				for (Punch punch : runnerData.getPunches()) {
					json.writeNumber(punch.getCode());
					json.writeNumber(punch.getTime().getTime());
				}
				json.writeEndArray();
				json.writeEndObject();
				
				RunnerResult result = runnerData.getResult();
				json.writeStartObject();
				json.writeNumberField(TIME, result.getRacetime());
				json.writeStringField(STATUS, result.getStatus().toString());
				json.writeNumberField(MPS, result.getNbMPs());
				json.writeNumberField(PENALTY, result.getTimePenalty());
				json.writeArrayFieldStart(TRACE);
				for (Trace trace : result.getTrace()) {
					json.writeString(trace.getCode());
					json.writeNumber(trace.getTime().getTime());
				}
				json.writeEndArray();
				json.writeArrayFieldStart(NEUTRALIZED);
				for (int i = 0; i < result.getTrace().length; i++) {
					Trace trace = result.getTrace()[i];
					if (trace.isNeutralized()) {
						json.writeNumber(i);
					}
				}
				json.writeEndArray();
				json.writeEndObject();
				json.writeEndArray();
			}
			json.writeEndArray();
			
			json.writeNumberField(MAXID, id);
			json.writeEndObject();
			json.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		backupData(stage.getBaseDir(), datafile, "store.zip");
	}

	public void backupData(String basedir, String datafile, String backupname) {
		try {
			ZipOutputStream zipStream = new ZipOutputStream(
					new FileOutputStream(basedir + GecoResources.sep
							+ backupname));
			writeZipEntry(zipStream, datafile, basedir);
			zipStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeZipEntry(ZipOutputStream zipStream, String filename,
			String basedir) throws IOException, FileNotFoundException {
		ZipEntry zipEntry = new ZipEntry(filename);
		zipStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[4096];
		BufferedInputStream inputStream = new BufferedInputStream(
				new FileInputStream(basedir + GecoResources.sep + filename));
		int len;
		while ((len = inputStream.read(buffer)) != -1) {
			zipStream.write(buffer, 0, len);
		}
		inputStream.close();
		zipStream.closeEntry();
	}

	public Stage loadData(String testDir, Factory factory, Checker checker) {
		Stage newStage = factory.createStage();

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			JsonNode store = mapper.readTree(new File(testDir
					+ GecoResources.sep + "store.json"));
			// loadStageProperties(newStage, baseDir);
			importDataIntoRegistry(store, newStage, factory);
			// checker.postInitialize(newStage); // post initialization
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newStage;
	}

	private void importDataIntoRegistry(JsonNode store, Stage newStage,
			Factory factory) {
		Registry registry = new Registry();
		newStage.setRegistry(registry);

		Object[] refMap = new Object[store.get(MAXID).intValue() + 1];

		for (JsonNode c : store.get(COURSES)) {
			Course course = factory.createCourse();
			course.setName(c.get(NAME).textValue());
			course.setLength(c.get("length").intValue());
			course.setClimb(c.get("climb").intValue());
			JsonNode codes = c.get("codes");
			int[] codez = new int[codes.size()];
			for (int j = 0; j < codes.size(); j++) {
				codez[j] = codes.get(j).intValue();
			}
			course.setCodes(codez);
			refMap[c.get(ID).intValue()] = course;
			registry.addCourse(course);
		}
		registry.ensureAutoCourse(factory);

		for (JsonNode c : store.get("categories")) {
			Category category = factory.createCategory();
			category.setName(c.get(NAME).textValue());
			category.setLongname(c.get("long").textValue());
			category.setCourse((Course) refMap[c.path(COURSE).intValue()]); // ref[0]
																			// =
																			// null
			refMap[c.get(ID).intValue()] = category;
			registry.addCategory(category);
		}

		for (JsonNode c : store.get("clubs")) {
			Club club = factory.createClub();
			club.setName(c.get(NAME).textValue());
			club.setShortname(c.get("short").textValue());
			refMap[c.get(ID).intValue()] = club;
			registry.addClub(club);
		}

		store.get("heatsets");

		for (JsonNode runnerData : store.get("runnersData")) {
			JsonNode r = runnerData.get(0);
			Runner runner = factory.createRunner();
			runner.setStartId(r.get(START_ID).intValue());
			runner.setFirstname(r.get(FIRST).textValue());
			runner.setLastname(r.get(LAST).textValue());
			runner.setEcard(r.get(ECARD).textValue());
			runner.setClub((Club) refMap[r.get(CLUB).intValue()]);
			runner.setCategory((Category) refMap[r.get(CAT).intValue()]);
			runner.setCourse((Course) refMap[r.get(COURSE).intValue()]);
			runner.setRegisteredStarttime(new Date(r.get(START).longValue()));
			if (r.has(ARK)) {
				runner.setArchiveId(r.get(ARK).intValue());
			}
			registry.addRunner(runner);

			JsonNode d = runnerData.get(1);
			RunnerRaceData ecardData = factory.createRunnerRaceData();
			ecardData.setRunner(runner);
			ecardData.setStarttime(new Date(d.get(START).longValue()));
			ecardData.setFinishtime(new Date(d.get(FINISH).longValue()));
			ecardData.setErasetime(new Date(d.get(ERASE).longValue()));
			ecardData.setControltime(new Date(d.get(CHECK).longValue()));
			ecardData.setReadtime(new Date(d.get(READ).longValue()));
			JsonNode p = d.get(PUNCHES);
			Punch[] punches = new Punch[p.size() / 2];
			ecardData.setPunches(punches);
			for (int j = 0; j < punches.length; j++) {
				punches[j] = factory.createPunch();
				punches[j].setCode(p.get(2 * j).intValue());
				punches[j].setTime(new Date(p.get(2 * j + 1).longValue()));
			}
			registry.addRunnerData(ecardData);

			JsonNode res = runnerData.get(2);
			RunnerResult result = factory.createRunnerResult();
			result.setRacetime(res.get(TIME).longValue());
			result.setStatus(Status.valueOf(res.get(STATUS).textValue()));
			result.setNbMPs(res.get(MPS).intValue());
			result.setTimePenalty(res.get(PENALTY).longValue());
			JsonNode t = res.get(TRACE);
			Trace[] trace = new Trace[t.size() / 2];
			result.setTrace(trace);
			for (int j = 0; j < trace.length; j++) {
				trace[j] = factory.createTrace(t.get(2 * j).textValue(),
						new Date(t.get(2 * j + 1).longValue()));
			}
			for (JsonNode trace_index : res.get(NEUTRALIZED)) {
				trace[trace_index.intValue()].setNeutralized(true);
			}
			ecardData.setResult(result);
		}

	}

}
