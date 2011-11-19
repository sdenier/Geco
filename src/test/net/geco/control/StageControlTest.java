/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.assertEquals;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.control.StageControl;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.model.Registry;
import net.geco.model.impl.POFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import test.net.geco.GecoFixtures;



/**
 * @author Simon Denier
 * @since Oct 5, 2011
 *
 */
public class StageControlTest {
	
	private StageControl control;
	private Registry registry;
	private POFactory factory;

	@Before
	public void setUp(){
		registry = new Registry();
		GecoControl geco = GecoFixtures.mockGecoControlWithRegistry(registry);
		factory = new POFactory();
		Mockito.when(geco.factory()).thenReturn(factory);
		Mockito.when(geco.announcer()).thenReturn(new Announcer());
		control = new StageControl(geco);
	}
	
	@Test
	public void importCategoryTemplateWithLazyCreation(){
		
		
		try {
			control.importCategoryTemplate("testData/categoryTemplate.txt");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		Course red = registry.findCourse("Red");
		assertEquals("Red", red.getName());
		Category h21 = registry.findCategory("H21");
		assertEquals("M Senior", h21.getLongname());
		assertEquals(red, h21.getCourse());

		Course orange = registry.findCourse("Orange");
		assertEquals("Orange", orange.getName());
		Category d35 = registry.findCategory("D35");
		assertEquals("W Senior", d35.getLongname());
		assertEquals(orange, d35.getCourse());
	}

	@Test
	public void importCategoryTemplateOverwriting(){
		Course red = factory.createCourse();
		red.setName("Red");
		registry.addCourse(red);
		Category d35 = factory.createCategory();
		d35.setName("D35");
		registry.addCategory(d35);
		
		try {
			control.importCategoryTemplate("testData/categoryTemplate.txt");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		assertEquals(red, registry.findCourse("Red"));
		Category h21 = registry.findCategory("H21");
		assertEquals("M Senior", h21.getLongname());
		assertEquals(red, h21.getCourse());

		Course orange = registry.findCourse("Orange");
		assertEquals("Orange", orange.getName());
		assertEquals(d35, registry.findCategory("D35"));
		assertEquals("W Senior", d35.getLongname());
		assertEquals(orange, d35.getCourse());
	}


}
