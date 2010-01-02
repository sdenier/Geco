/**
 * Copyright (c) 2010 Simon Denier
 */
package valmo.geco.model;

import java.util.Date;


public interface Trace extends Cloneable {
	
	public Trace clone();
	
	public String getCode();
	
	public Date getTime();
}