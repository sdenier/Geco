/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

import java.util.Date;

/**
 * should there be a unique DB per event? Some entity such as Event should not be put in DB then. 
 * Or should we put all events in the same db, then we need a complete ER model to avoid mismatch
 * between events data
 * 
 * 
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public interface Event {

	public String getName();

	public void setName(String name);

	public Date getStartdate();

	public void setStartdate(Date startdate);

	public Date getEnddate();

	public void setEnddate(Date enddate);

}