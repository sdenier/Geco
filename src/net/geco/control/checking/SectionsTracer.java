/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.geco.control.BasicControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Section;
import net.geco.model.Section.SectionType;
import net.geco.model.SectionTraceData;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Jul 23, 2013
 *
 */
public class SectionsTracer extends BasicControl {

	public static class SectionPunches {
		
		private Section targetSection;

		private Trace[] punchTrace;

		private int firstOkPunchIndex;
		
		private int lastOkPunchIndex;
		
		public SectionPunches(Section section, TraceData data) {
			targetSection = section;
			punchTrace = data.getPunchTrace();
			findFirstLastIndices();
		}

		public SectionType getType() {
			return targetSection.getType();
		}

		public int[] getCodes() {
			return targetSection.getCodes();
		}

		public Punch[] collectPunches(Punch[] punches) {
			if( isMissing() ) {
				return new Punch[0];
			} else {
				return Arrays.copyOfRange(punches, firstOkPunchIndex, lastOkPunchIndex + 1);
			}
		}
		
		private void findFirstLastIndices() {
			firstOkPunchIndex = -1;
			lastOkPunchIndex = punchTrace.length;
			foldStartIndex();
			if( ! isMissing() ) {
				foldEndIndex();
			}
		}

		public int firstOkPunchIndex() {
			return firstOkPunchIndex;
		}
		
		public int lastOkPunchIndex() {
			return lastOkPunchIndex;
		}

		public boolean isMissing() {
			return firstOkPunchIndex > lastOkPunchIndex;
		}

		public void foldStartIndex() {
			int i = firstOkPunchIndex + 1;
			for (; i < punchTrace.length; i++) {
				if( punchTrace[i].isOK() ) {
					firstOkPunchIndex = i;
					break;
				}
			}
			if( i == punchTrace.length ) {
				firstOkPunchIndex = -1;
				lastOkPunchIndex = -2;
			}
		}
		
		public void foldEndIndex() {
			int i = lastOkPunchIndex - 1;
			for (; i >= 0; i--) {
				if( punchTrace[i].isOK() ) {
					lastOkPunchIndex = i;
					break;
				}
			}
			if( i < 0) {
				firstOkPunchIndex = -1;
				lastOkPunchIndex = -2;
			}
		}

		public boolean overlaps(SectionPunches nextSection) {
			return lastOkPunchIndex >= nextSection.firstOkPunchIndex && ! nextSection.isMissing();
		}
		
		public boolean prevailsOver(SectionPunches nextSection) {
			if( nextSection.firstOkPunchIndex == lastOkPunchIndex ){
				// central loop control --> prevalence to the next beginning section
				return false;
			} else {
				int selfCount = countPunches(nextSection.firstOkPunchIndex, lastOkPunchIndex);
				int nextCount = nextSection.countPunches(nextSection.firstOkPunchIndex, lastOkPunchIndex);
				return selfCount >= nextCount;
			}
		}

		private int countPunches(int start, int end) {
			int count = 0;
			for (int i = start; i <= end && i < punchTrace.length; i++) {
				if( punchTrace[i].isOK() ){ count++; }
			}
			return count;
		}

		public String toString() {
			return String.format("[%s:%s]", firstOkPunchIndex, lastOkPunchIndex);
		}

	}
	
	public SectionsTracer(Factory factory) {
		super(factory);
	}

	private Tracer getTracer(SectionType type) {
		Tracer t = null;
		switch (type) {
		case INLINE:
			t = new InlineTracer(factory());
			break;
		case FREEORDER:
			t = new FreeOrderTracer(factory());
			break;
		}
		return t;
	}

	public SectionTraceData computeTrace(List<Section> sections, Punch[] punches) {
		List<SectionPunches> sectionsPunches = computeSectionsTrace(sections, punches);
		refineSectionMarkers(sectionsPunches);
		List<TraceData> sectionsTrace = computeRefinedSectionsTrace(sectionsPunches, punches);
		return mergeSectionsTrace(sections, sectionsTrace);
	}

	public List<SectionPunches> computeSectionsTrace(List<Section> sections, Punch[] punches) {
		ArrayList<SectionPunches> sectionTraces = new ArrayList<SectionPunches>();
		for (Section section : sections) {
			sectionTraces.add(
				new SectionPunches(section, computeTrace(section.getType(), section.getCodes(), punches)));
		}
		return sectionTraces;
	}

	public List<TraceData> computeRefinedSectionsTrace(List<SectionPunches> sections, Punch[] punches) {
		ArrayList<TraceData> sectionTraces = new ArrayList<TraceData>();
		for (SectionPunches section : sections) {
			sectionTraces.add(
				computeTrace(section.getType(), section.getCodes(), section.collectPunches(punches)));
		}
		return sectionTraces;
	}

	private TraceData computeTrace(SectionType type, int[] codes, Punch[] punches) {
		return getTracer(type).computeTrace(codes, punches);
	}

	public SectionTraceData mergeSectionsTrace(List<Section> sections, List<TraceData> sectionsTrace) {
		SectionTraceData mergedTrace = (SectionTraceData) factory().createTraceData();
		int nbMPs = 0;
		List<Trace> trace = new ArrayList<Trace>(sectionsTrace.size() * 10);
		for (int i = 0; i < sections.size(); i++) {
			mergedTrace.putSectionAt(sections.get(i), trace.size());
			TraceData data = sectionsTrace.get(i);
			nbMPs += data.getNbMPs();
			Collections.addAll(trace, data.getTrace());
		}
		mergedTrace.setNbMPs(nbMPs);
		mergedTrace.setTrace(trace.toArray(new Trace[0]));
		return mergedTrace;
	}

	/*
	 * The goal is to compute markers so that sections follow each other in the course sequence,
	 * without holes (i.e. covering all punches), each section encompassing most of its detected punches.
	 * Then punches can be split between sections using adjacent markers.
	 *
	 * Nominal: full ok section traces already adjacent (no added punch in-between)
	 * Disjoined: "holes" of added punches in-between sections, or at start/end of punches
	 * Overlapping: overlapping punches (markers) between two consecutive sections (include butterflies)
	 * Missing: missing sections are treated as a special case
	 *
	 * Missing sections are a special case: they do not contain any punch (except for the case where there are
	 * only missing sections, then the first section gets all punches). Note that a section overlapping with other
	 * sections may become "missing" after refinement, if other sections prevail.
	 */
	public List<SectionPunches> refineSectionMarkers(List<SectionPunches> sections) {
		splitSections(sections);
		rejoinSections(sections);
		return sections;
	}

	private List<SectionPunches> selectOkSections(List<SectionPunches> sections) {
		List<SectionPunches> okSections = new ArrayList<SectionPunches>(sections.size());
		for (SectionPunches section : sections) {
			if( ! section.isMissing() ) {
				okSections.add(section);
			}
		}
		return okSections;
	}

	/*
	 * Only operate on consecutive ok sections.
	 * Detect and resolve conflicts (overlapping sections) by shifting start/end index of sections
	 * according to the "prevailing" weight (max number of overlapping punches wins).
	 */
	private void splitSections(List<SectionPunches> allSections) {
		SectionPunches previousSection;
		SectionPunches nextSection;
		List<SectionPunches> sections = selectOkSections(allSections);
		for (int i = 1; i < sections.size(); i++) {
			previousSection = sections.get(i - 1);
			nextSection = sections.get(i);
			while( previousSection.overlaps(nextSection) ) {
				if( previousSection.prevailsOver(nextSection) ) {
					nextSection.foldStartIndex();
				} else {
					previousSection.foldEndIndex();
				}
			}
			// if one goes missing, remove it to replay split with previous/next ok sections
			if( previousSection.isMissing() ) {
				sections.remove(i - 1);
				i = Math.max(i - 2, 0);
			} else if( nextSection.isMissing() ) {
				sections.remove(i);
				i--;
			}
		}
	}

	/*
	 * Only operate on consecutive ok sections.
	 * Make ok sections jointed if necessary (due to added punches around sections)
	 * so that sections punches cover all punches.
	 */
	private void rejoinSections(List<SectionPunches> allSections) {
		SectionPunches previousSection;
		SectionPunches nextSection;
		List<SectionPunches> sections = selectOkSections(allSections);
		for (int i = 1; i < sections.size(); i++) {
			previousSection = sections.get(i - 1);
			nextSection = sections.get(i);
			previousSection.lastOkPunchIndex = nextSection.firstOkPunchIndex - 1;
		}
		if( ! sections.isEmpty() ) {
			sections.get(0).firstOkPunchIndex = 0;
			SectionPunches lastSection = sections.get(sections.size() - 1);
			lastSection.lastOkPunchIndex = lastSection.punchTrace.length - 1;
		} else if( ! allSections.isEmpty() ) {
			SectionPunches surrogateSection = allSections.get(0);
			surrogateSection.firstOkPunchIndex = 0;
			surrogateSection.lastOkPunchIndex = surrogateSection.punchTrace.length - 1;
		}
	}
	
}
