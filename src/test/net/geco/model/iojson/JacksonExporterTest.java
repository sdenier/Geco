/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import net.geco.model.iojson.JacksonExporter;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonGenerator;

/**
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
	
}
