/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.junit.Assert.assertEquals;
import net.geco.model.Runner;
import net.geco.model.RunnerRegistry;
import net.geco.model.impl.POFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jun 1, 2011
 *
 */
public class RunnerRegistryTest {
	
	private POFactory factory;

	private RunnerRegistry registry;

	private Runner runner1;

	@Before
	public void setUp(){
		factory = new POFactory();
		registry = new RunnerRegistry();
		runner1 = factory(1);
	}
	
	public Runner factory(int i){
		Runner runner = factory.createRunner();
		runner.setStartId(Integer.valueOf(i));
		return runner;
	}
	
	@Test
	public void createOneRunnerWithId(){
		registry.addRunner(runner1);
		assertEquals(runner1.getStartId(), registry.findRunnerById(Integer.valueOf(1)).getStartId());
		assertEquals(runner1, registry.findRunnerById(1));
	}

	@Test
	public void removeRunnerWithId(){
		createOneRunnerWithId();
		registry.removeRunner(runner1);
		Assert.assertTrue(registry.getRunners().isEmpty());
	}

	@Test
	public void createRunnerWithoutId(){
		Runner runner = factory.createRunner();
		registry.registerRunner(runner);
		assertEquals(runner, registry.findRunnerById(1));
		
		registry.addRunner(factory(2));
		
		runner = factory.createRunner();
		registry.registerRunner(runner);
		assertEquals(runner, registry.findRunnerById(3));
	}
	
	@Test
	public void updateRunnerStartId(){
		createOneRunnerWithId();
		runner1.setStartId(6);
		registry.updateRunnerStartId(1, runner1);
		assertEquals(runner1, registry.findRunnerById(6));
	}
	
	@Test
	public void testMaxStartId(){
		assertEquals(0, registry.maxStartId());
		registry.addRunner(runner1);
		assertEquals(1, registry.maxStartId());
		registry.addRunner(factory(2));
		assertEquals(2, registry.maxStartId());
		Runner runner5 = factory(5);
		registry.addRunner(runner5);
		assertEquals(5, registry.maxStartId());
		registry.addRunner(factory(3));
		
		registry.removeRunner(runner1);
		assertEquals(5, registry.maxStartId());
		registry.removeRunner(runner5);
		assertEquals(3, registry.maxStartId());
		
		runner1.setStartId(6);
		registry.updateRunnerStartId(1, runner1);
		assertEquals(6, registry.maxStartId());
	}

	

}
