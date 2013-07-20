/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

import java.util.Date;

/**
 * @author Simon Denier
 * @since Jul 20, 2013
 *
 */
public interface ECardData extends Cloneable {

	public Date getStartTime();

	public void setStartTime(Date starttime);

	public Date getFinishTime();

	public void setFinishTime(Date finishtime);

	public Date getClearTime();

	public void setClearTime(Date cleartime);

	public Date getCheckTime();

	public void setCheckTime(Date checktime);

	public Date getReadTime();

	public void setReadTime(Date readtime);
	
	public Date stampReadTime();

	public Punch[] getPunches();

	public void setPunches(Punch[] punches);
	
	public ECardData clone();
	
}
