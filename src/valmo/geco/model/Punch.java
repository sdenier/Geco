/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

import java.util.Date;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface Punch extends Cloneable {

	public int getCode();

	public void setCode(int code);

	public Date getTime();

	public void setTime(Date time);

	public Punch clone();

}