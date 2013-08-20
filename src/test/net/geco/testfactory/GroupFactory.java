/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.HeatSet;
import net.geco.model.Pool;
import net.geco.model.ResultType;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since May 24, 2012
 *
 */
public class GroupFactory {

	private static Factory factory = new POFactory();
	
	public static Category createCategory(String name) {
		Category category = factory.createCategory();
		category.setName(name);
		return category;
	}

	public static Category createCategoryWithCourse(String name, Course course) {
		Category category = factory.createCategory();
		category.setName(name);
		category.setCourse(course);
		return category;
	}

	public static Club createClub(String name) {
		Club club = factory.createClub();
		club.setName(name);
		return club;
	}
	
	public static HeatSet createHeatSet(String name) {
		HeatSet heatset = factory.createHeatSet();
		heatset.setName(name);
		heatset.setQualifyingRank(4);
		heatset.setHeatNames(new String[] {"A"});
		heatset.setSetType(ResultType.CourseResult);
		heatset.setSelectedPools(new Pool[]{ CourseFactory.createCourse("Qual")} );
		return heatset;
	}
	
}
