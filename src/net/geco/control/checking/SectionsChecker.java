/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.Map.Entry;

import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.model.RunnerRaceData;
import net.geco.model.Section;
import net.geco.model.SectionTraceData;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Aug 6, 2013
 *
 */
public class SectionsChecker extends PenaltyChecker {

	private SectionsTracer sectionsTracer;

	public SectionsChecker(GecoControl gecoControl) {
		super(gecoControl, null);
		sectionsTracer = new SectionsTracer(gecoControl.factory());
	}

	@Override
	public TraceData computeTraceData(RunnerRaceData runnerData) {
		return sectionsTracer.computeTrace(runnerData.getCourse().getSections(), runnerData.getPunches());
	}

	@Override
	public long computeRunningTime(RunnerRaceData runnerData) {
		long runningTime = super.computeRunningTime(runnerData);
		SectionTraceData traceData = (SectionTraceData) runnerData.getTraceData();
		for (Entry<Integer, Section> sectionData : traceData.getSectionData()) {
			if( sectionData.getValue().neutralized() ) {
				long neutralizedTime = traceData.computeSectionTime(sectionData.getKey(),
																	runnerData.getOfficialStarttime(),
																	runnerData.getFinishtime());
				runningTime = TimeManager.subtract(runningTime, neutralizedTime);
			}
		}
		return runningTime;
	}

}
