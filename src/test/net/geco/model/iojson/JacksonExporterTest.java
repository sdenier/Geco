/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.Date;

import net.geco.model.iojson.JacksonExporter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Beware that Mockito can't verify call on final methods. It will verify some subcall with partial arguments. 
 * 
 * @author Simon Denier
 * @since Jan 5, 2013
 *
 */
public class JacksonExporterTest {

	private JacksonExporter subject;

	@Mock
	private JsonGenerator gen;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.subject = new JacksonExporter(gen);
	}
	
	@Test
	public void key() throws IOException{
		subject.key("k1");
		verify(gen).writeFieldName("k1");
	}

	@Test
	public void stringValue() throws IOException{
		subject.value("value1");
		verify(gen).writeString("value1");
	}

	@Test
	public void dateValue() throws IOException{
		Date date = new Date();
		subject.value(date);
		verify(gen).writeNumber(date.getTime());
	}

	@Test
	public void intValue() throws IOException{
		subject.value(12);
		verify(gen).writeNumber(12);
	}

	@Test
	public void longValue() throws IOException{
		subject.value(100L);
		verify(gen).writeNumber(100L);
	}

	@Test
	public void booleanValue() throws IOException{
		subject.value(true);
		verify(gen).writeBoolean(true);
	}

	@Test
	public void startObject() throws IOException {
		subject.startObject();
		verify(gen).writeStartObject();
	}

	@Test
	public void startObjectField() throws IOException {
		subject.startObjectField("o1");
		verify(gen).writeObjectFieldStart("o1");
	}

	@Test
	public void endObject() throws IOException {
		subject.endObject();
		verify(gen).writeEndObject();
	}

	@Test
	public void startArray() throws IOException {
		subject.startArray();
		verify(gen).writeStartArray();
	}

	@Test
	public void startArrayField() throws IOException {
		subject.startArrayField("a1");
		verify(gen).writeArrayFieldStart("a1");
	}

	@Test
	public void endArray() throws IOException {
		subject.endArray();
		verify(gen).writeEndArray();
	}

	@Test
	public void stringField() throws IOException {
		subject.field("k1", "val1");
		verify(gen).writeStringField("k1", "val1");
	}

	@Test
	public void dateField() throws IOException {
		Date date = new Date();
		subject.field("k1", date);
		verify(gen).writeNumberField("k1", date.getTime());
	}

	@Test
	public void intField() throws IOException {
		subject.field("k1", 1);
		verify(gen).writeNumberField("k1", 1);
	}

	@Test
	public void longField() throws IOException {
		subject.field("k1", 10L);
		verify(gen).writeNumberField("k1", 10L);
	}

	@Test
	public void booleanField() throws IOException {
		subject.field("k1", false);
		verify(gen).writeBooleanField("k1", false);
	}

	@Test
	public void optField_writeStringRepresentation() throws IOException {
		subject.optField("k", "Non Null");
		verify(gen).writeStringField("k", "Non Null");
	}

	@Test
	public void optField_writeNothingIfNull() throws IOException {
		subject.optField("k", null);
		verifyZeroInteractions(gen);
	}

	@Test
	public void optField_writeNumberRepresentation() throws IOException {
		subject.optField("k", Integer.valueOf(1));
		verify(gen).writeNumberField("k", 1);
	}

	@Test
	public void optField_writeNothingIfNullNumber() throws IOException {
		subject.optField("k", (Integer) null);
		verifyZeroInteractions(gen);
	}
	
	@Test
	public void optField_writeFieldIfTrue() throws IOException {
		subject.optField("flag", true);
		verify(gen).writeBooleanField("flag", true);
	}

	@Test
	public void optField_writeNothingIfFalse() throws IOException {
		subject.optField("flag", false);
		verifyZeroInteractions(gen);
	}
	
	@Test
	public void id() throws IOException {
		Object o1 = new Object();
		subject.id("id", o1);
		subject.id("id2", new Object());
		subject.id("id", o1);
		verify(gen, times(2)).writeNumberField("id", 1);
		verify(gen).writeNumberField("id2", 2);
	}

	@Test
	public void ref() throws IOException {
		Object o1 = new Object();
		subject.id("id", o1);
		subject.ref("ref", o1);
		verify(gen).writeNumberField("ref", 1);
	}

	@Test
	public void optRef_writeObjectId() throws IOException {
		Object o1 = new Object();
		subject.id("id", o1);
		subject.optRef("ref", o1);
		verify(gen).writeNumberField("ref", 1);
	}
	
	@Test
	public void optRef_writeNothingIfNull() throws IOException {
		subject.optRef("ref", null);
		verifyZeroInteractions(gen);
	}

	@Test
	public void idMax() throws IOException {
		subject.id("id", new Object());
		subject.idMax("max");
		verify(gen).writeNumberField("max", 1);
	}

	
}
