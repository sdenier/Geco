/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import net.geco.control.BasicControl;
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

	@Override
	public void setTraceData(RunnerRaceData runnerData) {
		runnerData.setTraceData(computeTraceData(runnerData));		
	}

	@Override
	public TraceData computeTraceData(RunnerRaceData runnerData) {
		TraceData traceData = tracer.computeTrace(runnerData.getCourse().getCodes(), runnerData.getPunches());
		return traceData;
	}

	@Override
	public void setResult(RunnerRaceData runnerData) {
		RunnerResult result = factory().createRunnerResult();
		runnerData.setResult(result);
		resetRaceTime(runnerData);
		result.setStatus(computeStatus(runnerData));
	}

	@Override
	public void resetRaceTime(RunnerRaceData runnerData) {
		RunnerResult result = runnerData.getResult();
		result.setRaceTime(computeRaceTime(runnerData));
		result.setTimePenalty(computeTimePenalty(runnerData));
		result.setResultTime(computeResultTime(runnerData));
	}

}
