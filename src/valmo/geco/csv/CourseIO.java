/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.csv;

import java.util.Arrays;

import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class CourseIO extends AbstractIO<Course> {

	public static String orFilename() {
		return "Courses.csv";
	}

	public CourseIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
	}

	@Override
	public Course importTData(String[] record) {		
		Course course = this.factory.createCourse();
		course.setName(record[0]);
		Float meterLength = new Float(record[1]) * 1000;
		course.setLength(meterLength.intValue());
		course.setClimb(new Integer(record[2]));
		int[] codes = new int[record.length - 12];
		for (int i = 0; i < codes.length; i++) {
			codes[i] = new Integer(record[i + 12]);
		};
		course.setCodes(codes);
//		course.save();
		return course;
	}


	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractImporter#register(java.lang.Object, valmo.geco.csv.Registry)
	 */
	@Override
	public void register(Course data, Registry registry) {
		registry.addCourse(data);
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#exportTData(java.lang.Object)
	 */
	@Override
	public String[] exportTData(Course c) {
		/*
		 * course name, length, climb, type, start code, finish code,
		 * time limit, time penalty, default start time, spare, spare, spare
		 * control codes....
		 */
		String[] record = new String[] {
				c.getName(),
				new Float(c.getLength() / 1000.0).toString(),
				new Integer(c.getClimb()).toString(),
				"Cross Country", "1", "1001", "60", "10", "36000000", "", "", ""
		};
		int offset = record.length;
		int[] codes = c.getCodes();
		record = Arrays.copyOf(record, offset + codes.length);
		for (int i = 0; i < codes.length; i++) {
			record[i+offset] = new Integer(codes[i]).toString();
		}
		return record;
	}

}
