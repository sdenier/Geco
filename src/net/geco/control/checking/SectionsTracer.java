/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.ArrayList;
import java.util.List;

import net.geco.control.BasicControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Section;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Jul 23, 2013
 *
 */
public class SectionsTracer extends BasicControl implements Tracer {

	public static class SectionPunches {
		
		private Trace[] punchTrace;

		private int firstOkPunchIndex;
		
		private int lastOkPunchIndex;
		
		public SectionPunches(TraceData data) {
			punchTrace = data.getPunchTrace();
			findFirstLastIndices();
		}
		
		private void findFirstLastIndices() {
			firstOkPunchIndex = -1;
			lastOkPunchIndex = -1;
			for (int i = 0; i < punchTrace.length; i++) {
				if( punchTrace[i].isOK() ) {
					firstOkPunchIndex = i;
					break;
				}
			}
			if( firstOkPunchIndex != -1 ) {
				for (int i = punchTrace.length - 1; i >= 0; i--) {
					if( punchTrace[i].isOK() ) {
						lastOkPunchIndex = i;
						break;
					}
				}				
			}
		}

		public int firstOkPunchIndex() {
			return firstOkPunchIndex;
		}
		
		public int lastOkPunchIndex() {
			return lastOkPunchIndex;
		}
		
		public boolean prevailsOver(SectionPunches nextSection) {
			if( overlaps(nextSection) ) {
				int selfCount = countPunches(nextSection.firstOkPunchIndex, lastOkPunchIndex);
				int nextCount = nextSection.countPunches(nextSection.firstOkPunchIndex, lastOkPunchIndex);
				return selfCount >= nextCount;
			} else {
				return false;
			}
		}

		private boolean overlaps(SectionPunches nextSection) {
			return lastOkPunchIndex >= nextSection.firstOkPunchIndex;
		}

		private int countPunches(int start, int end) {
			int count = 0;
			for (int i = start; i <= end && i < punchTrace.length; i++) {
				if( punchTrace[i].isOK() ){ count++; }
			}
			return count;
		}
	}
	
	public SectionsTracer(Factory factory) {
		super(factory);
	}

	@Override
	public TraceData computeTrace(int[] codes, Punch[] punches) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * The goal is to compute markers so that sections follow each other in the course sequence,
	 * without holes (i.e. covering all punches), each section encompassing most of its detected punches.
	 * Then punches can be split between sections using adjacent markers.
	 * 
	 * Nominal case: full ok section traces already adjacent (no added punch in-between)
	 * Flawed: "holes" of added punches in-between sections, or at start/end of punches
	 * Conflict cases: overlapping punches (markers) between two "adjacent sections",
	 * overlapping sections with outliers on both ends (case: butterflys?)
	 * Corner cases: missing sections, two adjacent missing sections, missing sections at start/end
	 */
	public List<SectionPunches> refineSectionMarkers(List<SectionPunches> sections) {
		SectionPunches previousSection;
		SectionPunches nextSection;
		for (int i = 1; i < sections.size(); i++) {
			previousSection = sections.get(i - 1);
			nextSection = sections.get(i);
			if( previousSection.prevailsOver(nextSection) ) {
				nextSection.firstOkPunchIndex = previousSection.lastOkPunchIndex + 1;
			} else {
				previousSection.lastOkPunchIndex = nextSection.firstOkPunchIndex - 1;
			}
		}
		sections.get(0).firstOkPunchIndex = 0;
		SectionPunches lastSection = sections.get(sections.size() - 1);
		lastSection.lastOkPunchIndex = lastSection.punchTrace.length - 1;
		return sections;
	}
	
	public List<SectionPunches> computeSectionsTrace(List<Section> sections, Punch[] punches) {
		ArrayList<SectionPunches> sectionTraces = new ArrayList<SectionPunches>();
		for (Section section : sections) {
			sectionTraces.add(new SectionPunches(getTracer(section).computeTrace(section.getCodes(), punches)));
		}
		return sectionTraces;
	}

	private Tracer getTracer(Section section) {
		Tracer t = null;
		switch (section.getType()) {
		case INLINE:
			t = new InlineTracer(factory());
			break;
		case FREEORDER:
			t = new FreeOrderTracer(factory());
			break;
		}
		return t;
	}
	
}
