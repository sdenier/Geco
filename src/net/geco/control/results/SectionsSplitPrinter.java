/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results;

import net.geco.control.GecoControl;
import net.geco.control.context.RunnerContext;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Aug 25, 2013
 *
 */
public class SectionsSplitPrinter extends RunnerSplitPrinter {

	private SectionsExporter sectionsExporter;

	public SectionsSplitPrinter(GecoControl gecoControl) {
		super(gecoControl);
		sectionsExporter = getService(SectionsExporter.class);
	}

	@Override
	protected RunnerContext buildRunnerSplitContext(RunnerRaceData data) {
		RunnerContext runnerCtx = super.buildRunnerSplitContext(data);
		SplitTime[] bestSplits = new SplitTime[0];
		SplitTime[] sectionsSplits = builder.buildSectionSplits(data, bestSplits);
		sectionsExporter.createRunnerSplits(runnerCtx, sectionsSplits, bestSplits);
		sectionsExporter.buildSectionsHeader(data.getCourse().getSections(), runnerCtx);
		sectionsExporter.buildSectionSplitsForTicket(data.getCourse().getSections(), sectionsSplits, bestSplits, runnerCtx);
		return runnerCtx;
	}

}
