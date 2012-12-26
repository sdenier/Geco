/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import net.geco.basics.GecoResources;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.HeatSet;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 *
 */
public class PersistentStore {
	
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
			BufferedWriter writer = GecoResources.getSafeWriterFor(stage.getBaseDir() + GecoResources.sep + "store.json");

			JSONWriter json = new JSONWriter(writer);
			json.object()
				.key("version").value("2.0");
			writer.newLine();
			json.key("stage").object();
			writer.newLine();
			json.key("name").value(stage.getName())
				.key("basedir").value(stage.getBaseDir())
				.key("zerohour").value(stage.getZeroHour())
				.endObject();
			writer.newLine();
			json.key("properties").object().endObject();
			writer.newLine();
			json.key("courses").array();
			writer.newLine();
			for (Course c : stage.registry().getCourses()) {
				json.object()
					.key("name").value(c.getName())
					.key("id").value(idFor(c))
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
					.key("name").value(c.getShortname())
					.key("id").value(idFor(c))
					.key("long").value(c.getLongname());
				if( c.getCourse() != null ){
					json.key("course").value(refFor(c.getCourse()));
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
					.key("name").value(c.getName())
					.key("id").value(idFor(c))
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
					.key("name").value(h.getName())
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
				json.object();
				json.key("id").value(runnerData.getRunner().getStartId());
				json.endObject();
				json.object().endObject();
				json.object().endObject();
				json.endArray();
				writer.newLine();
			}
			json.endArray();
			json.endObject();
			writer.newLine();
			writer.close();
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

}
