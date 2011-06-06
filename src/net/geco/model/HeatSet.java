/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

/**
 * A HeatSet describes the parameters to generate heats from pools of runners.
 * 
 * @author Simon Denier
 * @since Jun 23, 2009
 *
 */
public interface HeatSet extends Group {

	public String getName();

	public void setName(String name);

	public Integer getNbHeats();

	public String[] getHeatNames();

	public void setHeatNames(String[] heatNames);

	public Integer getQualifyingRank();

	public void setQualifyingRank(Integer qualifyingRank);

	public ResultType getSetType();

	public void setSetType(ResultType type);

	public boolean isCourseType();

	public boolean isCategoryType();
	
	public boolean isMixedType();

	public Pool[] getSelectedPools();

	public void setSelectedPools(Pool[] selectedPools);

}