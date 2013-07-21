/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import net.geco.control.BasicControl;
import net.geco.control.checking.Checker;
import net.geco.control.checking.Tracer;
import net.geco.model.Factory;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Jul 21, 2013
 *
 */
public abstract class AbstractChecker extends BasicControl implements Checker {

	protected Tracer tracer;
	
	public AbstractChecker(Factory factory, Tracer tracer) {
		super(factory);
		this.tracer = tracer;
	}

	@Override
	public void check(RunnerRaceData runnerData) {
		setTraceData(runnerData);
		setResult(runnerData);
	}

	protected void setTraceData(RunnerRaceData runnerData) {
		runnerData.setTraceData(computeTraceData(runnerData));		
	}

	public TraceData computeTraceData(RunnerRaceData runnerData) {
		tracer.computeTrace(runnerData.getCourse().getCodes(), runnerData.getPunches());
		TraceData traceData = factory().createTraceData();
		traceData.setNbMPs(tracer.getNbMPs());
		traceData.setTrace(tracer.getTrace());
		traceData.setRunningTime(runnerData.computeRunningTime());
		return traceData;
	}

	protected void setResult(RunnerRaceData runnerData) {
		RunnerResult result = factory().createRunnerResult();
		runnerData.setResult(result);
		result.setTimePenalty(computeTimePenalty(runnerData));
		result.setRacetime(computeRaceTime(runnerData));
		result.setStatus(computeStatus(runnerData));
	}

	@Override
	public void resetRaceTime(RunnerRaceData runnerData) {
		runnerData.getResult().setTimePenalty(computeTimePenalty(runnerData));
		runnerData.getResult().setRacetime(computeRaceTime(runnerData));
	}

}
