/**
 * Copyright (c) 2010 Simon Denier
 */
package net.geco.model;

import java.util.Date;


public interface Trace extends Cloneable {
	
	public Trace clone();
	
	public String getCode();
	
	public String getBasicCode();

	public String getAddedCode();
	
	public Date getTime();
	
	public boolean isOK();
	
	public boolean isMP();
	
	public boolean isAdded();
	
	public boolean isSubst();

	public boolean isTruePunch();
	
	public boolean isNeutralized();
	
	public void setNeutralized(boolean flag);
}