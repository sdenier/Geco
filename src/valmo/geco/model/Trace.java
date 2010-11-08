/**
 * Copyright (c) 2010 Simon Denier
 */
package valmo.geco.model;

import java.util.Date;


public interface Trace extends Cloneable {
	
	public Trace clone();
	
	public String getCode();
	
	public String getBasicCode();
	
	public Date getTime();
	
	public boolean isOK();
	
	public boolean isMP();
	
	public boolean isAdded();
	
	public boolean isSubst();
}