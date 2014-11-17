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
import net.geco.model.TraceData;


/**
 * @author Simon Denier
 * @since Dec 7, 2008
 *
 */
public class PenaltyChecker extends AbstractChecker implements StageListener {
	
	protected long MPPenalty;

	protected int MPLimit;

	protected boolean noMPLimit;

	private long extraPenalty;
	
	
	public PenaltyChecker(Factory factory, Tracer tracer) {
		super(factory, tracer);
		setMPLimit(defaultMPLimit());
		setMPPenalty(defaultMPPenalty());
		setExtraPenalty(defaultExtraPenalty());
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
	public long computeRaceTime(RunnerRaceData runnerData) {
		return runnerData.computeRunningTime();
	}

	@Override
	public long computeTimePenalty(RunnerRaceData raceData) {
		TraceData traceData = raceData.getTraceData();
		return mpTimePenalty(traceData.getNbMPs()) + extraTimePenalty(traceData.getNbExtraneous());
	}

	public long mpTimePenalty(int nbMPs) {
		return nbMPs * getMPPenalty();
	}

	public long extraTimePenalty(int nbExtra) {
		return nbExtra * getExtraPenalty();
	}

	@Override
	public long computeResultTime(RunnerRaceData data) {
		long raceTime = data.getResult().getRaceTime();
		if( raceTime==TimeManager.NO_TIME_l ) {
			return raceTime;
		}
		return raceTime + data.getResult().getTimePenalty() + data.getResult().getManualTimePenalty();
	}

	@Override
	public Status computeStatus(RunnerRaceData data) {
		Status status = (noMPLimit || data.getTraceData().getNbMPs() <= getMPLimit()) ? Status.OK : Status.MP;
		if( data.getResult().getResultTime()==TimeManager.NO_TIME_l ) {
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
	
	public long defaultExtraPenalty() {
		return 0;
	}

	public long getMPPenalty() {
		return MPPenalty;
	}

	public void setMPPenalty(long penalty) {
		MPPenalty = penalty;
	}

	public long getExtraPenalty() {
		return extraPenalty;
	}

	public void setExtraPenalty(long penalty) {
		extraPenalty = penalty;
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
		Properties properties = stage.getProperties();
		String limit = properties.getProperty(mpLimitProperty(), Integer.toString(defaultMPLimit()));
		try {
			setMPLimit(Integer.parseInt(limit));
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
		String penalty = properties.getProperty(mpPenaltyProperty(), Long.toString(defaultMPPenalty()));
		try {
			setMPPenalty(Long.parseLong(penalty));				
		} catch (NumberFormatException e) {
			setMPPenalty(defaultMPPenalty());
			System.err.println(e);
		}
		String xPenalty = properties.getProperty(extraPenaltyProperty(), Long.toString(defaultExtraPenalty()));
		try {
			setExtraPenalty(Long.parseLong(xPenalty));				
		} catch (NumberFormatException e) {
			setExtraPenalty(defaultExtraPenalty());
			System.err.println(e);
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
			properties.setProperty(mpLimitProperty(), Integer.toString(getMPLimit()));
		}
		properties.setProperty(mpPenaltyProperty(), Long.toString(getMPPenalty()));
		properties.setProperty(extraPenaltyProperty(), Long.toString(getExtraPenalty()));
	}

	public static String mpLimitProperty() {
		return "MPLimit"; //$NON-NLS-1$
	}

	public static String mpPenaltyProperty() {
		return "MPPenalty"; //$NON-NLS-1$
	}

	public static String extraPenaltyProperty() {
		return "ExtraPenalty"; //$NON-NLS-1$
	}

	@Override
	public void closing(Stage stage) {	}
	
}
