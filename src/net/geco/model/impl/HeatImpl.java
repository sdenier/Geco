/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.List;
import java.util.Vector;

import net.geco.model.Heat;
import net.geco.model.Runner;



public class HeatImpl implements Heat {

	private String name;
	private List<Runner> qualifiedRunners;
	private String heatsetName;

	public HeatImpl() {
		this(""); //$NON-NLS-1$
	}
	
	public HeatImpl(String name) {
		this(name, new Vector<Runner>());
	}
	
	public HeatImpl(String name, List<Runner> qualifiedRunners) {
		this.name = name;
		this.qualifiedRunners = qualifiedRunners;
	}
	
	public void addQualifiedRunner(Runner runner) {
		this.qualifiedRunners.add(runner);
	}
	
	public String getName() {
		return name;
	}
	
	public List<Runner> getQualifiedRunners() {
		return qualifiedRunners;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQualifiedRunners(List<Runner> qualifiedRunners) {
		this.qualifiedRunners = qualifiedRunners;
	}

	@Override
	public String getHeatSetName() {
		return heatsetName;
	}

	@Override
	public void setHeatSetName(String name) {
		this.heatsetName = name;
	}
}