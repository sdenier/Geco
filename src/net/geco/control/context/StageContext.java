/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.context;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.geco.control.results.AResultExporter.OutputType;

/**
 * @author Simon Denier
 * @since May 13, 2013
 *
 */
public class StageContext extends GenericContext {

	public StageContext(String stageName) {
		put("geco_StageTitle", stageName); //$NON-NLS-1$
		put("geco_Timestamp", new SimpleDateFormat("H:mm").format(new Date())); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public StageContext(String stageName, boolean isSingleCourseResult, boolean showPenalties, int refreshInterval,
						OutputType outputType) {
		this(stageName);

		// General layout
		put("geco_SingleCourse?", isSingleCourseResult); //$NON-NLS-1$
		put("geco_RunnerCategory?", isSingleCourseResult); //$NON-NLS-1$
		put("geco_Penalties?", showPenalties); //$NON-NLS-1$

		// Meta info
		put("geco_FileOutput?", outputType == OutputType.FILE); //$NON-NLS-1$
		put("geco_AutoRefresh?", refreshInterval > 0); //$NON-NLS-1$
		put("geco_RefreshInterval", refreshInterval); //$NON-NLS-1$
		put("geco_PrintMode?", outputType == OutputType.PRINTER); //$NON-NLS-1$
	}

	public ContextList createResultsCollection(int capacity) {
		return createContextList("geco_ResultsCollection", capacity); //$NON-NLS-1$
	}
	
}
