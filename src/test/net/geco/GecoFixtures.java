/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco;

import java.util.Date;

import net.geco.app.AppBuilder;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.control.StageBuilder;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

import org.mockito.Mockito;

/**
 * @author Simon Denier
 * @since Aug 27, 2011
 *
 */
public class GecoFixtures {
	
	private static Factory factory = new POFactory();

	public static GecoControl mockGecoControl() {
		return Mockito.mock(GecoControl.class);
	}
	
	public static GecoControl mockGecoControlWithRegistry(Registry registry) {
		Stage mockStage = Mockito.mock(Stage.class);
		Mockito.when(mockStage.registry()).thenReturn(registry);
		GecoControl mockGecoControl = Mockito.mock(GecoControl.class);
		Mockito.when(mockGecoControl.stage()).thenReturn(mockStage);
		return mockGecoControl;
	}
	
	public static Stage loadStageFrom(String baseDir, AppBuilder builder, GecoControl gecoControl) {
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
		return new StageBuilder(builder.getFactory()).loadStage(baseDir, builder.createChecker(gecoControl));
	}
	
	public static GecoControl loadFixtures(String baseDir, AppBuilder builder) {
		Factory factory = builder.getFactory();
		GecoControl gecoControl = mockGecoControl();
		Announcer announcer = Mockito.mock(Announcer.class);
		Mockito.when(gecoControl.factory()).thenReturn(factory);
		Mockito.when(gecoControl.announcer()).thenReturn(announcer);
		Stage stage = loadStageFrom(baseDir, builder, gecoControl);
		Mockito.when(gecoControl.stage()).thenReturn(stage);
		Mockito.when(gecoControl.registry()).thenReturn(stage.registry());
		return gecoControl;
	}

	public static Course createCourse(String name, int... codes) {
		Course course = factory.createCourse();
		course.setName(name);
		course.setCodes(codes);
		return course;
	}

	public static Punch punch(Date time, int code) {
		Punch punch = factory.createPunch();
		punch.setTime(time);
		punch.setCode(code);
		return punch;
	}
	
	public static Punch punch(int code) {
		return punch(new Date(), code);
	}
	
	public static RunnerRaceData createRunnerData(Course course, Category cat) {
		Runner runner = factory.createRunner();
		runner.setCourse(course);
		runner.setCategory(cat);
		RunnerRaceData data = factory.createRunnerRaceData();
		data.setRunner(runner);
		return data;
	}

}
