/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.Date;

import net.geco.basics.GecoResources;
import net.geco.basics.TimeManager;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Registry;
import net.geco.model.ResultType;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
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

	private Registry registry;

	private Factory factory;

	
	@Before
	public void setUp() throws JSONException, FileNotFoundException {
		subject = new PersistentStore();
		BufferedReader reader = GecoResources.getSafeReaderFor("testData/valid/sample_import.json");
		testStore = new JSONStore(reader, K.MAXID);
		registry = new Registry();
		factory = new POFactory();
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
		verify(registry).addHeatSet(any(HeatSet.class));
		verify(registry, times(3)).addRunner(any(Runner.class));
		verify(registry, times(3)).addRunnerData(any(RunnerRaceData.class));
	}
	
	@Test
	public void importCourses() throws JSONException {
		subject.importCourses(testStore, registry, factory);
		assertThat(registry.getCourses().size(), equalTo(4));
		assertThat(registry.findCourse("Circuit C").getLength(), equalTo(3010));
		assertThat(registry.findCourse("Circuit D").getCodes()[0], equalTo(43));
		assertThat(registry.findCourse("[Auto]").getCodes().length, equalTo(0));
	}
	
	@Test
	public void importCategories() throws JSONException {
		subject.importCourses(testStore, registry, factory);
		Course courseD = registry.findCourse("Circuit D");
		subject.importCategories(testStore, registry, factory);
		assertThat(registry.getCategories().size(), equalTo(3));
		assertThat(registry.findCategory("D20").getLongname(), equalTo("D20"));
		assertThat(registry.findCategory("H60+").getCourse(), equalTo(courseD));
	}
	
	@Test
	public void importClubs() throws JSONException {
		subject.importClubs(testStore, registry, factory);
		assertThat(registry.getClubs().size(), equalTo(2));
		assertThat(registry.findClub("LOUP").getShortname(), equalTo("2606RA"));
	}
	
	@Test
	public void importHeatSets() throws JSONException {
		subject.importCourses(testStore, registry, factory);
		Course courseC = registry.findCourse("Circuit C");
		subject.importHeatSets(testStore, registry, factory);
		assertThat(registry.getHeatSets().size(), equalTo(1));
		HeatSet heatset = registry.getHeatSets().iterator().next();
		assertThat(heatset.getName(), equalTo("A Series"));
		assertThat(heatset.getQualifyingRank(), equalTo(2));
		assertThat(heatset.getSetType(), equalTo(ResultType.CourseResult));
		assertThat(heatset.getHeatNames(), equalTo(new String[]{"A1", "A2"}));
		assertThat(heatset.getSelectedPools().length, equalTo(3));
		assertThat((Course) heatset.getSelectedPools()[0], equalTo(courseC));
	}
	
	@Test
	public void importOKRunner() throws JSONException {
		subject.importDataIntoRegistry(testStore, registry, factory);
		assertThat(registry.getRunnersData().size(), equalTo(3));

		RunnerRaceData runner136 = registry.findRunnerData(136);
		assertThat(runner136.getRunner().getEcard(), equalTo("304260"));
		assertThat(runner136.getRunner().getArchiveId(), equalTo(10684));
		assertThat(runner136.getRunner().getRegisteredStarttime(), equalTo(new Date(40560000)));
		assertThat(runner136.getCourse(), equalTo(registry.findCourse("Circuit C")));
		assertThat(runner136.getPunches().length, equalTo(15));
		assertThat(runner136.getResult().getStatus(), equalTo(Status.OK));
		assertThat(runner136.getResult().getRacetime(), equalTo(4186000L));
		assertThat(runner136.getResult().getTrace()[2].isNeutralized(), equalTo(true));
		assertThat(runner136.getResult().getTrace()[3].isNeutralized(), equalTo(false));
		assertThat(runner136.getResult().getTrace()[5].isNeutralized(), equalTo(true));
	}

	@Test
	public void importDNSRunner() throws JSONException {
		subject.importDataIntoRegistry(testStore, registry, factory);
		RunnerRaceData runner109 = registry.findRunnerData(109);
		assertThat(runner109.getRunner().getEcard(), equalTo("1221292a"));
		assertThat(runner109.getRunner().getCategory(), equalTo(registry.findCategory("D20")));
		assertThat(runner109.getFinishtime(), equalTo(TimeManager.NO_TIME));
		assertThat(runner109.getPunches().length, equalTo(0));
		assertThat(runner109.getResult().getStatus(), equalTo(Status.DNS));
	}

	@Test
	public void importNCRunner() throws JSONException {
		subject.importDataIntoRegistry(testStore, registry, factory);
		RunnerRaceData runner224 = registry.findRunnerData(224);
		assertThat(runner224.getRunner().getEcard(), equalTo("34293"));
		assertThat(runner224.getRunner().getClub(), equalTo(registry.findClub("CSMR")));
		assertThat(runner224.getRunner().isNC(), equalTo(true));
		assertThat(runner224.getResult().getStatus(), equalTo(Status.MP));
		assertThat(runner224.getTraceData().getNbMPs(), equalTo(2));
		assertThat(runner224.getResult().getTimePenalty(), equalTo(60000L));
		assertThat(runner224.getResult().getTrace()[6].getCode(), equalTo("-167"));
	}
		
}
