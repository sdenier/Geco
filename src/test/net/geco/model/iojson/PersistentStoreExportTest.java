/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.net.geco.testfactory.CourseFactory.createCourse;
import static test.net.geco.testfactory.CourseFactory.createCourseWithMassStartTime;
import static test.net.geco.testfactory.GroupFactory.createCategory;
import static test.net.geco.testfactory.GroupFactory.createCategoryWithCourse;
import static test.net.geco.testfactory.GroupFactory.createClub;
import static test.net.geco.testfactory.GroupFactory.createHeatSet;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Registry;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.impl.POFactory;
import net.geco.model.iojson.JSONSerializer;
import net.geco.model.iojson.PersistentStore;
import net.geco.model.iojson.PersistentStore.K;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import test.net.geco.testfactory.RunnerFactory;

/**
 * @author Simon Denier
 * @since Jan 7, 2013
 *
 */
public class PersistentStoreExportTest {

	private PersistentStore subject;

	private Factory factory;
	
	@Mock
	private JSONSerializer json;
	
	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);
		subject = new PersistentStore();
		factory = new POFactory();
		mockFluentJson();
	}

	@Test
	public void exportDataToJson() throws IOException {
		Stage stage = factory.createStage();
		stage.setRegistry(new Registry());
		subject.exportDataToJson(stage, json);
		verify(json).field(eq(K.VERSION), anyString());
		verify(json).startArrayField(K.COURSES);
		verify(json).startArrayField(K.CATEGORIES);
		verify(json).startArrayField(K.CLUBS);
		verify(json).startArrayField(K.HEATSETS);
		verify(json).startArrayField(K.RUNNERS_DATA);
		verify(json).idMax(K.MAXID);
	}

	@Test
	public void exportCourses() throws IOException {
		Collection<Course> courses = asList(createCourseWithMassStartTime("Course A", new Date(32400000)));
		subject.exportCourses(json, courses);
		verify(json).field(K.NAME, "Course A");
		verify(json).field(K.START, new Date(32400000));
		InOrder inOrder = inOrder(json);
		inOrder.verify(json).startArrayField(K.CODES);
		inOrder.verify(json).value(31);
		inOrder.verify(json).value(32);
		inOrder.verify(json).value(33);
		inOrder.verify(json).endArray();
	}
	
	@Test
	public void exportCategories() throws IOException {
		Course course2 = createCourse("Course 2");
		Collection<Category> categories = asList(createCategory("Cat 1"),
														createCategoryWithCourse("Cat 2", course2));
		subject.exportCategories(json, categories);
		InOrder inOrder = inOrder(json);
		inOrder.verify(json).field(K.NAME, "Cat 1");
		inOrder.verify(json).optRef(K.COURSE, null);
		inOrder.verify(json).field(K.NAME, "Cat 2");
		inOrder.verify(json).optRef(K.COURSE, course2);
	}

	@Test
	public void exportClubs() throws IOException {
		Collection<Club> clubs = asList(createClub("Club C"));
		subject.exportClubs(json, clubs);
		verify(json).field(K.NAME, "Club C");
	}

	@Test
	public void exportHeatSets() throws IOException {
		HeatSet heatset = createHeatSet("Heat 1");
		subject.exportHeatSets(json, asList(heatset));
		verify(json).field(K.NAME, "Heat 1");
		verify(json).field(K.RANK, heatset.getQualifyingRank());
		verify(json).startArrayField(K.HEATS);
		verify(json).value(heatset.getHeatNames()[0]);
		verify(json).field(K.TYPE, heatset.getSetType().name());
		verify(json).startArrayField(K.POOLS);
		verify(json).ref(heatset.getSelectedPools()[0]);
	}
	
	@Test
	public void exportRunnerData() throws IOException {
		Collection<RunnerRaceData> runnerData = asList(RunnerFactory.createWithStatus("1111", Status.MP));;
		subject.exportRunnersData(json, runnerData);
		verify(json, times(3)).startObject();
		verify(json).field(eq(K.START_ID), anyInt());
		verify(json).field(K.ECARD, "1111");
		verify(json).ref(eq(K.COURSE), any(Course.class));
		verify(json).optField(eq(K.ARK), any(Integer.class));
		verify(json).field(K.STATUS, Status.MP.name());
		verify(json).startArrayField(K.PUNCHES);
		verify(json).startArrayField(K.TRACE);
		verify(json).startArrayField(K.NEUTRALIZED);
	}
	
	private void mockFluentJson() throws IOException {
		when(json.startObject()).thenReturn(json);
		when(json.startObjectField(anyString())).thenReturn(json);
		when(json.endObject()).thenReturn(json);
		when(json.startArrayField(anyString())).thenReturn(json);
		when(json.endArray()).thenReturn(json);
		when(json.field(anyString(), anyString())).thenReturn(json);
		when(json.field(anyString(), any(Date.class))).thenReturn(json);
		when(json.field(anyString(), anyInt())).thenReturn(json);
		when(json.field(anyString(), anyLong())).thenReturn(json);
		when(json.optField(anyString(), any(Integer.class))).thenReturn(json);
		when(json.optField(anyString(), anyBoolean())).thenReturn(json);
		when(json.id(anyString(), any())).thenReturn(json);
		when(json.ref(anyString(), any())).thenReturn(json);
		when(json.optRef(anyString(), any())).thenReturn(json);
		when(json.idMax(anyString())).thenReturn(json);
	}
	
}
