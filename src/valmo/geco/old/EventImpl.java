/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.model;

import java.util.Date;

/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public class EventImpl implements Event {

	private String name;
	
	private Date startdate;
	
	private Date enddate;

	/* (non-Javadoc)
	 * @see valmo.geco.model.Event#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.model.Event#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.model.Event#getStartdate()
	 */
	public Date getStartdate() {
		return startdate;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.model.Event#setStartdate(java.util.Date)
	 */
	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.model.Event#getEnddate()
	 */
	public Date getEnddate() {
		return enddate;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.model.Event#setEnddate(java.util.Date)
	 */
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	
}
