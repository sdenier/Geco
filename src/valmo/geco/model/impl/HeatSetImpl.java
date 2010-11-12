/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;
import valmo.geco.model.ResultType;

public class HeatSetImpl implements HeatSet {
	private String name;
	private Integer qualifyingRank;
	private ResultType type;
	private String[] heatNames;
	private Pool[] selectedPools;
	
	public HeatSetImpl() {
		name = ""; //$NON-NLS-1$
		qualifyingRank = 0;
		type = ResultType.CourseResult;
		heatNames = new String[] { "" }; //$NON-NLS-1$
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
	public ResultType getSetType() {
		return type;
	}
	public void setSetType(ResultType type) {
		this.type = type;
	}
	public boolean isCourseType() {
		return getSetType().equals(ResultType.CourseResult);
	}
	public boolean isCategoryType() {
		return getSetType().equals(ResultType.CategoryResult);
	}
	public boolean isMixedType() {
		return getSetType().equals(ResultType.MixedResult);
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