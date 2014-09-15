/**
 * Copyright (c) 2014 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.checking.SectionsTracer.SectionPunches;
import net.geco.model.RunnerRaceData;
import net.geco.model.Section;
import net.geco.model.SectionTraceData;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Sep 3, 2014
 *
 */
public class SectionsManualChecker extends Control {

	public SectionsManualChecker(GecoControl gecoControl) {
		super(SectionsManualChecker.class, gecoControl);
	}

	/*
	 * trigger popup on ok/subst/added punch only? cant on mp
	 * 
	 * move section to new index
	 * - clone treemap
	 * - remove from old index (reverse search)
	 * - put at new index
	 * 
	 * rebuild section punches from section trace
	 * - compute start/end index from current trace/punches (filter mp to recompute punch indices?)
	 * - exclude missing section
	 * 
	 * refresh updated trace data
	 * - compute refined section trace (SectionsTracer)
	 * - merge section trace (SectionsTracer)
	 * - update result (SectionsChecker)
	 */

	public void refreshTraceWithUpdatedSection(RunnerRaceData raceData, Section sectionToMove, int newStartIndex) {
		SectionTraceData currentSectionTrace = (SectionTraceData) raceData.getTraceData();
		SectionTraceData reorderedSectionTrace = rebuildSectionOrder(currentSectionTrace, sectionToMove, newStartIndex);
		List<SectionPunches> reorderedSectionPunches = rebuildSectionPunches(currentSectionTrace, reorderedSectionTrace);
		SectionTraceData newSectionTrace = rebuildSectionTrace(raceData, reorderedSectionPunches);
		updateResult(raceData, newSectionTrace);
	}
	
	public SectionTraceData rebuildSectionOrder(SectionTraceData traceData, Section sectionToMove, int newStartIndex) {
		Set<Entry<Integer,Section>> sections = traceData.getSectionData();
		SectionTraceData editedTrace = (SectionTraceData) factory().createTraceData();
		for (Entry<Integer, Section> entry : sections) {
			Section section = entry.getValue();
			int index = section.equals(sectionToMove) ? newStartIndex : entry.getKey();
			editedTrace.putSectionAt(section, index);
		}
		return editedTrace;
	}
	
	public List<SectionPunches> rebuildSectionPunches(SectionTraceData currentSectionTrace, SectionTraceData orderedSections) {
		Trace[] currentTrace = currentSectionTrace.getTrace();
		List<SectionPunches> sections = new ArrayList<SectionPunches>();

		Iterator<Entry<Integer, Section>> eachSection = orderedSections.getSectionData().iterator();
		Entry<Integer, Section> currentSection = eachSection.next();
		Entry<Integer, Section> nextSection = eachSection.next();
		
		int traceIndex = 0;
		int punchIndex = 0;
		int sectionStartIndex = 0;
		do {
			Trace trace = currentTrace[traceIndex];

			if ( traceIndex == nextSection.getKey() ) {
				sections.add(new SectionPunches(currentSection.getValue(), sectionStartIndex, punchIndex - 1));
				sectionStartIndex = punchIndex;
				currentSection = nextSection;
				if ( eachSection.hasNext() ) {
					nextSection = eachSection.next();					
				} else {
					break;
				}
			}
			if ( trace.isTruePunch() ) {
				punchIndex++;
			}
			traceIndex++;
		} while ( traceIndex < currentTrace.length );
		sections.add(new SectionPunches(currentSection.getValue(), sectionStartIndex, currentSectionTrace.getPunchTrace().length - 1));

		return sections;
	}

	public SectionTraceData rebuildSectionTrace(RunnerRaceData raceData, List<SectionPunches> reorderedSectionPunches) {
		SectionsTracer tracer = new SectionsTracer(factory());
		List<TraceData> refinedSectionsTrace = tracer.computeRefinedSectionsTrace(reorderedSectionPunches, raceData.getPunches());
		return tracer.mergeSectionsTrace(raceData.getCourse().getSections(), refinedSectionsTrace);
	}

	public void updateResult(RunnerRaceData raceData, SectionTraceData newTrace) {
		raceData.setTraceData(newTrace);
		geco().checker().setResult(raceData);
	}
	
}
