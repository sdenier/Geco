/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

/**
 * A HeatSet describes the parameters to generate heats from pools of runners.
 * 
 * @author Simon Denier
 * @since Jun 23, 2009
 *
 */
public interface HeatSet {

	public String getName();

	public void setName(String name);

	public Integer getNbHeats();

	public String[] getHeatNames();

	public void setHeatNames(String[] heatNames);

	public Integer getQualifyingRank();

	public void setQualifyingRank(Integer qualifyingRank);

	public String getSetType();

	public void setSetType(String type);

	public boolean isCourseType();

	public boolean isCategoryType();

	public Object[] getSelectedSets();

	public void setSelectedSets(Object[] selectedSets);

}