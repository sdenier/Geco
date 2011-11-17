/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.framework;



/**
 * @author Simon Denier
 * @since Jul 24, 2011
 *
 */
public interface IStageLaunch extends Cloneable {
	
	public String getStageName();
	
	public void setStageName(String name);
	
	public String getStageDir();
	
	public void setStageDir(String path);
	
	public String getAppBuilderName();
	
	public void setAppBuilderName(String name);

	public void initDirWithTemplateFiles();

	public IStageLaunch loadFromFileSystem(String dir);

	public void copyFrom(IStageLaunch selectedValue);

	public Object clone() throws CloneNotSupportedException;

}
