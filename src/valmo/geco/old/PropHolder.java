/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import valmo.geco.ui.Util;

/**
 * PropHolder holds a list of properties at runtime, which can be saved and loaded from file for persistency.
 * It works like the Properties class in Java.
 * It adds a special feature to handle multiple instances of the same property.
 * Multiple instances have special file format for key: 'xkey-1', 'xkey-2', ... will result in a single
 * property named 'xkey', which holds the list of values named 'xkey-...' 
 * Also string-split each multi-instance.
 * 
 * @author Simon Denier
 * @since Feb 7, 2009
 *
 */
public class PropHolder {
	
	private Properties prop;
	
	private HashMap<String,Vector<String>> propertiesList;
	
	/**
	 * 
	 */
	public PropHolder() {
		reset();
	}
	
	/**
	 * Load properties from Java properties file and convert instances of property as single key to list of instances.
	 * Note that instances appear in the list in the order of their file key. 
	 */
	public PropHolder loadProperties(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		prop = new Properties();
		prop.load(reader);
		Pattern pattern = Pattern.compile("((\\w+)-\\d+)");
//		Pattern pattern = Pattern.compile("(heat-\\d+)");
		Vector<String> itemKeys = new Vector<String>();
		for (Object key : prop.keySet()) {
			Matcher matcher = pattern.matcher((CharSequence) key);
			if( matcher.matches() ) {
				itemKeys.add(matcher.group(1));
			}
		}
		Collections.sort(itemKeys);
		buildListProperty(itemKeys);
		return this;
	}

	/**
	 * Extract the property key (xkey in xkey-1) for each instance key and add a list entry for each instance.
	 * Also string-split each instance.
	 * 
	 * @param itemKeys
	 */
	@SuppressWarnings("unchecked")
	private void buildListProperty(Vector<String> itemKeys) {
		for (String k : itemKeys) {
			String listKey = k.substring(0, k.indexOf('-'));
			if( !prop.containsKey(listKey) ) {
				prop.put(listKey, new Vector<String>());
			}
			Vector<String[]> list = (Vector<String[]>) prop.get(listKey);
			list.add(Util.splitAndTrim(prop.getProperty(k), ","));
		}
	}
	
	public String getProperty(String key) {
		return prop.getProperty(key);
	}
	
	public boolean hasProperty(String key) {
		return prop.containsKey(key);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<String[]> getListProperty(String key) {
		return (Vector<String[]>) prop.get(key);
	}

	public void reset() {
		this.prop = new Properties();
		this.propertiesList = new HashMap<String, Vector<String>>();
	}
	
	public void setProperty(String key, String value) {
		prop.setProperty(key, value);
	}
	
	public void addListItem(String key, String[] values) {
		if( !propertiesList.containsKey(key) ) {
			propertiesList.put(key, new Vector<String>());
		}
		propertiesList.get(key).add(Util.join(values, ",", new StringBuffer()));
	}

	public void saveProperties(File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (String key : propertiesList.keySet()) {
			buildListItems(key, propertiesList.get(key));
		}
		prop.store(writer, "Geco " + new Date(System.currentTimeMillis()).toString());
	}

	/**
	 * @param key
	 * @param list
	 */
	private void buildListItems(String key, Vector<String> list) {
		for (int i = 0; i < list.size(); i++) {
			prop.setProperty(key + "-" + (i + 1), list.get(i));
		}
	}

}
