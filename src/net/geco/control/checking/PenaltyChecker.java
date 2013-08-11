/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.model.Factory;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;


/**
 * @author Simon Denier
 * @since Dec 7, 2008
 *
 */
public class PenaltyChecker extends AbstractChecker implements Checker, StageListener {
	
	protected long MPPenalty;

	protected int MPLimit;

	protected boolean noMPLimit;
	
	
	public PenaltyChecker(Factory factory, Tracer tracer) {
		super(factory, tracer);
		setMPLimit(defaultMPLimit());
		setMPPenalty(defaultMPPenalty());
		noMPLimit = false;
	}
	
	public PenaltyChecker(Factory factory) {
		this(factory, new InlineTracer(factory));
	}
	
	public PenaltyChecker(GecoControl gecoControl, Tracer tracer) {
		this(gecoControl.factory(), tracer);
		gecoControl.announcer().registerStageListener(this);
	}
	
	@Override
	public void postInitialize(Stage stage) {
		setNewProperties(stage);
	}

	@Override
	public long computeTimePenalty(RunnerRaceData raceData) {
		return timePenalty(raceData.getTraceData().getNbMPs());
	}

	public long timePenalty(int nbMPs) {
		return nbMPs * getMPPenalty();
	}
	
	@Override
	public long computeRaceTime(RunnerRaceData data) {
		long runningTime = data.computeRunningTime();
		if( runningTime==TimeManager.NO_TIME_l ) {
			return runningTime;
		}
		return runningTime + data.getResult().getTimePenalty();
	}

	@Override
	public Status computeStatus(RunnerRaceData data) {
		Status status = (noMPLimit || data.getTraceData().getNbMPs() <= getMPLimit()) ? Status.OK : Status.MP;
		if( data.getResult().getRacetime()==TimeManager.NO_TIME_l ) {
			status = Status.MP;
		}
		return status;
	}

	public int defaultMPLimit() {
		return 0;
	}

	public long defaultMPPenalty() {
		return 0;
	}

	public long getMPPenalty() {
		return MPPenalty;
	}

	public void setMPPenalty(long penalty) {
		MPPenalty = penalty;
	}

	public int getMPLimit() {
		return MPLimit;
	}

	public void setMPLimit(int limit) {
		MPLimit = limit;
	}

	public boolean noMPLimit() {
		return noMPLimit;
	}

	public void disableMPLimit() {
		noMPLimit = true;
	}

	public void enableMPLimit() {
		noMPLimit = false;
	}

	public void useMPLimit(boolean b) {
		noMPLimit = ! b;
	}

	protected void setNewProperties(Stage stage) {
		String limit = stage.getProperties().getProperty(mpLimitProperty());
		if( limit!=null ) {
			try {
				setMPLimit(new Integer(limit));
				if( getMPLimit()==-1 ){
					disableMPLimit();
					setMPLimit(defaultMPLimit());
				} else {
					enableMPLimit();
				}
			} catch (NumberFormatException e) {
				setMPLimit(defaultMPLimit());
				System.err.println(e);
			}
		} else {
			setMPLimit(defaultMPLimit());
		}
		String penalty = stage.getProperties().getProperty(mpPenaltyProperty());
		if( penalty!=null ) {
			try {
				setMPPenalty(new Long(penalty));				
			} catch (NumberFormatException e) {
				setMPPenalty(defaultMPPenalty());
				System.err.println(e);
			}
		} else {
			setMPPenalty(defaultMPPenalty());
		}		
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		setNewProperties(next);
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		if( noMPLimit ){
			properties.setProperty(mpLimitProperty(), "-1"); //$NON-NLS-1$
		} else {
			properties.setProperty(mpLimitProperty(), new Integer(getMPLimit()).toString());
		}
		properties.setProperty(mpPenaltyProperty(), new Long(getMPPenalty()).toString());
	}

	public static String mpLimitProperty() {
		return "MPLimit"; //$NON-NLS-1$
	}

	public static String mpPenaltyProperty() {
		return "MPPenalty"; //$NON-NLS-1$
	}

	@Override
	public void closing(Stage stage) {	}
	
}
