/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;

public class HeatSetImpl implements HeatSet {
	private String name;
	private Integer qualifyingRank;
	private String type;
	private String[] heatNames;
	private Pool[] selectedPools;
	
	public HeatSetImpl() {
		name = "";
		qualifyingRank = 0;
		type = "course";
		heatNames = new String[] { "" };
		selectedPools = new Pool[0];
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getNbHeats() {
		return heatNames.length;
	}
	public String[] getHeatNames() {
		return heatNames;
	}
	public void setHeatNames(String[] heatNames) {
		this.heatNames = heatNames;
	}
	public Integer getQualifyingRank() {
		return qualifyingRank;
	}
	public void setQualifyingRank(Integer qualifyingRank) {
		this.qualifyingRank = qualifyingRank;
	}
	public String getSetType() {
		return type;
	}
	public void setSetType(String type) {
		this.type = type;
	}
	public boolean isCourseType() {
		return getSetType().equals("course");
	}

	public boolean isCategoryType() {
		return getSetType().equals("category");
	}
	public Pool[] getSelectedPools() {
		return selectedPools;
	}
	public void setSelectedPools(Pool[] selectedPools) {
		this.selectedPools = selectedPools;
	}
	public String toString() {
		return getName();
	}
}