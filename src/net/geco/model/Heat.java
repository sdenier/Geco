/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.List;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface Heat {

	public void addQualifiedRunner(Runner runner);

	public String getHeatSetName();

	public String getName();

	public List<Runner> getQualifiedRunners();

	public void setName(String name);

	public void setQualifiedRunners(List<Runner> qualifiedRunners);
	
	public void setHeatSetName(String name);

}