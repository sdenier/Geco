/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.iocsv.CsvReader;

/**
 * @author Simon Denier
 * @since Sep 4, 2011
 *
 */
public class CompositeChecker extends PenaltyChecker {

	private Map<Course,MultiCourse> multis = new HashMap<Course, MultiCourse>();

	public CompositeChecker(Factory factory, CompositeTracer tracer) {
		super(factory, tracer);
	}
	
	public CompositeChecker(Factory factory) {
		super(factory, new CompositeTracer(factory));
	}

	public CompositeChecker(GecoControl gecoControl) {
		super(gecoControl, new CompositeTracer(gecoControl.factory()));
	}

	protected CompositeTracer tracer() {
		return (CompositeTracer) tracer;
	}
	
	@Override
	public Status computeStatus(RunnerRaceData data) {
		Course course = data.getCourse();
		MultiCourse multiCourse = multis.get(course);
		if( multiCourse==null ){
			System.err.println("Missing multi course for " + course.getName());
			multiCourse = registerMultiCourse(createMultiCourse(course, course.getCodes()[0]));
		}
		tracer().setMultiCourse(multiCourse);
		return super.computeStatus(data);
	}
	
	@Override
	public void postInitialize(Stage stage) {
		super.postInitialize(stage);
		importMultiCourses(stage);
	}

	public void importMultiCourses(Stage stage) {
		CsvReader reader = new CsvReader().initialize(stage.getBaseDir(), "Multicourses.csv");
		try {
			new File(reader.filePath()).createNewFile(); // create file if necessary
			reader.open();
			while( true ){
				String[] record = reader.readRecord();
				if( record==null ){
					break;
				}
				MultiCourse multiCourse = importMultiCourse(record, stage.registry());
				if( multiCourse!=null ){
					registerMultiCourse(multiCourse);
				} else {
					System.err.println("Unknown course for multi " + record[0]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MultiCourse importMultiCourse(String[] record, Registry registry) {
		Course course = registry.findCourse(record[0]);
		if( course!=null ){
			return createMultiCourse(course, Integer.parseInt(record[1]));
		} else {
			return null;
		}
	}

	public MultiCourse createMultiCourse(Course course, int joinCode) {
		MultiCourse multiCourse = new MultiCourse(course);
		multiCourse.startWith(new FreeOrderTracer(factory()));
		try {
			multiCourse.joinRight(joinCode, new InlineTracer(factory()));
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
		return multiCourse;
	}

	public MultiCourse registerMultiCourse(MultiCourse multiCourse) {
		multis.put(multiCourse.getCourse(), multiCourse);
		return multiCourse;
	}

}
