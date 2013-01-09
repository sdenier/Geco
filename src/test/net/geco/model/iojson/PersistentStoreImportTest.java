/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.StringReader;

import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.HeatSet;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.impl.POFactory;
import net.geco.model.iojson.JSONStore;
import net.geco.model.iojson.PersistentStore;
import net.geco.model.iojson.PersistentStore.K;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Simon Denier
 * @since Jan 10, 2013
 *
 */
public class PersistentStoreImportTest {

	private PersistentStore subject;
	
	private JSONStore testStore;

	private String jsonString = "{maxid: 1}";
	
	@Before
	public void setUp() throws JSONException {
		subject = new PersistentStore();
		testStore = new JSONStore(new StringReader(jsonString), K.MAXID);
	}
	
	@Test
	public void importDataIntoRegistry() throws JSONException {
		Registry registry = mock(Registry.class);
		POFactory factory = new POFactory();
		subject.importDataIntoRegistry(testStore, registry, factory);
		verify(registry).addCourse(any(Course.class));
		verify(registry).ensureAutoCourse(factory);
		verify(registry).addCategory(any(Category.class));
		verify(registry).addClub(any(Club.class));
		verify(registry).addHeatSet(any(HeatSet.class));
		verify(registry).addRunner(any(Runner.class));
		verify(registry).addRunnerData(any(RunnerRaceData.class));
	}
	
}
