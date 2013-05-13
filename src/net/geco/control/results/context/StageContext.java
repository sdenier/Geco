/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results.context;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.geco.control.results.AResultExporter.OutputType;

/**
 * @author Simon Denier
 * @since May 13, 2013
 *
 */
public class StageContext extends GenericContext {

	public StageContext(String stageName, boolean isSingleCourseResult, boolean showPenalties, int refreshInterval,
						OutputType outputType) {
		put("geco_StageTitle", stageName);

		// General layout
		put("geco_SingleCourse?", isSingleCourseResult);
		put("geco_RunnerCategory?", isSingleCourseResult);
		put("geco_Penalties?", showPenalties);

		// Meta info
		put("geco_FileOutput?", outputType == OutputType.FILE);
		put("geco_AutoRefresh?", refreshInterval > 0);
		put("geco_RefreshInterval", refreshInterval);
		put("geco_PrintMode?", outputType == OutputType.PRINTER);
		put("geco_Timestamp", new SimpleDateFormat("H:mm").format(new Date()));
	}

	public ContextList createResultsCollection(int capacity) {
		return createContextList("geco_ResultsCollection", capacity);
	}
	
}
