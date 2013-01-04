/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

/**
 * @author Simon Denier
 * @since Jan 4, 2013
 *
 */
public class JacksonExporter implements JSONExporter {

	private JsonGenerator gen;

	public JacksonExporter(Writer writer, boolean debug) throws IOException {
		JsonFactory jsonFactory = new JsonFactory();
		gen = jsonFactory.createGenerator(writer);
		if( debug ){
			gen.useDefaultPrettyPrinter();
		} else {
			gen.configure(Feature.QUOTE_FIELD_NAMES, false);
		}
	}

	public JacksonExporter(JsonGenerator gen) {
		this.gen = gen;
	}
	
	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#key(java.lang.String)
	 */
	@Override
	public JSONExporter key(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#value(java.lang.String)
	 */
	@Override
	public JSONExporter value(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#value(java.util.Date)
	 */
	@Override
	public JSONExporter value(Date value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#value(long)
	 */
	@Override
	public JSONExporter value(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#value(double)
	 */
	@Override
	public JSONExporter value(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#value(boolean)
	 */
	@Override
	public JSONExporter value(boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#startObject()
	 */
	@Override
	public JSONExporter startObject() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#startObjectField(java.lang.String)
	 */
	@Override
	public JSONExporter startObjectField(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#endObject()
	 */
	@Override
	public JSONExporter endObject() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#startArray()
	 */
	@Override
	public JSONExporter startArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#startArrayField(java.lang.String)
	 */
	@Override
	public JSONExporter startArrayField(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#endArray()
	 */
	@Override
	public JSONExporter endArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#field(java.lang.String, java.lang.String)
	 */
	@Override
	public JSONExporter field(String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#field(java.lang.String, java.util.Date)
	 */
	@Override
	public JSONExporter field(String key, Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#field(java.lang.String, long)
	 */
	@Override
	public JSONExporter field(String key, long value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#field(java.lang.String, double)
	 */
	@Override
	public JSONExporter field(String key, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#field(java.lang.String, boolean)
	 */
	@Override
	public JSONExporter field(String key, boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#optField(java.lang.String, java.lang.Object)
	 */
	@Override
	public JSONExporter optField(String key, Object nullableValue) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#optField(java.lang.String, boolean)
	 */
	@Override
	public JSONExporter optField(String key, boolean flag) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#id(java.lang.String, java.lang.Object)
	 */
	@Override
	public JSONExporter id(String key, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#ref(java.lang.String, java.lang.Object)
	 */
	@Override
	public JSONExporter ref(String key, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#optRef(java.lang.String, java.lang.Object)
	 */
	@Override
	public JSONExporter optRef(String key, Object nullableObject) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.geco.model.iojson.JSONExporter#idMax(java.lang.String)
	 */
	@Override
	public JSONExporter idMax(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
