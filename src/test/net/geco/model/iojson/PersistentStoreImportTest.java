/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.FileNotFoundException;

import net.geco.basics.GecoResources;
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

	
	@Before
	public void setUp() throws JSONException, FileNotFoundException {
		subject = new PersistentStore();
		BufferedReader reader = GecoResources.getSafeReaderFor("testData/valid/sample_import.json");
		testStore = new JSONStore(reader, K.MAXID);
	}
	
	@Test
	public void importDataIntoRegistry() throws JSONException {
		Registry registry = mock(Registry.class);
		POFactory factory = new POFactory();
		subject.importDataIntoRegistry(testStore, registry, factory);
		verify(registry, times(4)).addCourse(any(Course.class));
		verify(registry).ensureAutoCourse(factory);
		verify(registry, times(3)).addCategory(any(Category.class));
		verify(registry, times(2)).addClub(any(Club.class));
		verify(registry, never()).addHeatSet(any(HeatSet.class));
		verify(registry, times(3)).addRunner(any(Runner.class));
		verify(registry, times(3)).addRunnerData(any(RunnerRaceData.class));
	}
	
}
