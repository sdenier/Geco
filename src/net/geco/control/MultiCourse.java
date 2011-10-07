/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;


import java.util.Arrays;

import net.geco.basics.Util;
import net.geco.model.Course;

/**
 * @author Simon Denier
 * @since Sep 16, 2011
 *
 */
public class MultiCourse {
	
	public class Section {
		public final int[] codes;
		public final Tracer tracer;
		
		public Section(int[] codes, Tracer tracer) {
			this.codes = codes;
			this.tracer = tracer;
		}
	}

	private Course course;
	private Section firstSection;
	private Section secondSection;
	private Tracer tracer1;

	public MultiCourse(Course course) {
		this.course = course;
	}

	public Course getCourse() {
		return course;
	}

	public void startWith(Tracer tracer) {
		this.tracer1 = tracer;
	}

	public void joinRight(int startCode, Tracer tracer) throws Exception {
		int[] codes = course.getCodes();
		int joinIndex = Util.firstIndexOf(startCode, codes);
		if( joinIndex==-1 ){
			firstSection = new Section(new int[0], tracer1);
			secondSection = new Section(codes, tracer);
			throw new Exception("Missing join control " + startCode);
		}
		firstSection = new Section(Arrays.copyOfRange(codes, 0, joinIndex), tracer1);
		secondSection = new Section(Arrays.copyOfRange(codes, joinIndex, codes.length), tracer);
	}

	public Section firstSection() {
		return firstSection;
	}
	
	public Section secondSection() {
		return secondSection;
	}

}
