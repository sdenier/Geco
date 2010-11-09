/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public interface ArchiveRunner extends IRunner {

	public Integer getArchiveId();
	
	public void setArchiveId(Integer id);
	
	public String getBirthYear();
	
	public void setBirthYear(String year);
	
	public String getSex();
	
	public void setSex(String sex);
	
}
