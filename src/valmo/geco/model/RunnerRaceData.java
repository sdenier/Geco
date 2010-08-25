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
public interface RunnerRaceData extends Cloneable {

	public Runner getRunner();

	public void setRunner(Runner runner);

	public Date getStarttime();

	public void setStarttime(Date starttime);

	public Date getFinishtime();

	public void setFinishtime(Date finishtime);

	public Date getErasetime();

	public void setErasetime(Date erasetime);

	public Date getControltime();

	public void setControltime(Date controltime);

	public Date getReadtime();

	public void setReadtime(Date readtime);
	
	public Date stampReadtime();

	public Punch[] getPunches();

	public void setPunches(Punch[] punches);

	public Course getCourse();
	
	public boolean hasResult();

	public boolean hasManualStatus();
	
	public RunnerResult getResult();

	public void setResult(RunnerResult result);
	
	public boolean isRunning();

	public long realRaceTime();

	public String punchSummary(int sumLength);
	
	public RunnerRaceData clone();

	public void copyFrom(RunnerRaceData newData);

	public String infoString();

}