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
public class JacksonSerializer implements JSONSerializer {

	private IdMap idMap;

	private JsonGenerator gen;

	protected JacksonSerializer() {
		idMap = new IdMap();
	}
	
	public JacksonSerializer(Writer writer, boolean debug) throws IOException {
		this();
		JsonFactory jsonFactory = new JsonFactory();
		gen = jsonFactory.createGenerator(writer);
		if( debug ){
			gen.useDefaultPrettyPrinter();
		} else {
			gen.configure(Feature.QUOTE_FIELD_NAMES, false);
		}
	}

	/*
	 * Only for testing purpose
	 */
	public JacksonSerializer(JsonGenerator gen) {
		this();
		this.gen = gen;
	}

	@Override
	public JSONSerializer key(String key) throws IOException {
		gen.writeFieldName(key);
		return this;
	}

	@Override
	public JSONSerializer value(String value) throws IOException {
		gen.writeString(value);
		return this;
	}

	@Override
	public JSONSerializer value(Date value) throws IOException {
		gen.writeNumber(value.getTime());
		return this;
	}

	@Override
	public JSONSerializer value(int value) throws IOException {
		gen.writeNumber(value);
		return this;
	}

	@Override
	public JSONSerializer value(long value) throws IOException {
		gen.writeNumber(value);
		return this;
	}

	@Override
	public JSONSerializer value(boolean value) throws IOException {
		gen.writeBoolean(value);
		return this;
	}

	@Override
	public JSONSerializer startObject() throws IOException {
		gen.writeStartObject();
		return this;
	}

	@Override
	public JSONSerializer startObjectField(String key) throws IOException {
		gen.writeObjectFieldStart(key);
		return this;
	}

	@Override
	public JSONSerializer endObject() throws IOException {
		gen.writeEndObject();
		return this;
	}

	@Override
	public JSONSerializer startArray() throws IOException {
		gen.writeStartArray();
		return this;
	}

	@Override
	public JSONSerializer startArrayField(String key) throws IOException {
		gen.writeArrayFieldStart(key);
		return this;
	}

	@Override
	public JSONSerializer endArray() throws IOException {
		gen.writeEndArray();
		return this;
	}

	@Override
	public JSONSerializer field(String key, String value) throws IOException {
		gen.writeStringField(key, value);
		return this;
	}

	@Override
	public JSONSerializer field(String key, Date date) throws IOException {
		gen.writeNumberField(key, date.getTime());
		return this;
	}

	@Override
	public JSONSerializer field(String key, int value) throws IOException {
		gen.writeNumberField(key, value);
		return this;
	}

	@Override
	public JSONSerializer field(String key, long value) throws IOException {
		gen.writeNumberField(key, value);
		return this;
	}

	@Override
	public JSONSerializer field(String key, boolean value) throws IOException {
		gen.writeBooleanField(key, value);
		return this;
	}

	@Override
	public JSONSerializer optField(String key, Object nullableValue) throws IOException {
		if( nullableValue != null ) {
			gen.writeStringField(key, nullableValue.toString());
		}
		return this;
	}

	@Override
	public JSONSerializer optField(String key, Integer nullableValue) throws IOException {
		if( nullableValue != null ) {
			gen.writeNumberField(key, nullableValue.intValue());
		}
		return this;
	}

	@Override
	public JSONSerializer optField(String key, boolean flag) throws IOException {
		if( flag ) {
			gen.writeBooleanField(key, flag);
		}
		return this;
	}

	@Override
	public JSONSerializer id(String key, Object object) throws IOException {
		gen.writeNumberField(key, idMap.idFor(object));
		return this;
	}

	@Override
	public JSONSerializer ref(String key, Object object) throws IOException {
		gen.writeNumberField(key, idMap.findId(object));
		return this;
	}

	@Override
	public JSONSerializer optRef(String key, Object nullableObject) throws IOException {
		if( nullableObject != null ) {
			gen.writeNumberField(key, idMap.findId(nullableObject));
		}
		return this;
	}

	@Override
	public JSONSerializer idMax(String key) throws IOException {
		gen.writeNumberField(key, idMap.maxId());
		return this;
	}

	@Override
	public void close() throws IOException {
		gen.close();
	}
	
}
