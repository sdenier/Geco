/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results.context;

import net.geco.model.Result;

/**
 * @author Simon Denier
 * @since May 13, 2013
 *
 */
public class ResultContext extends GenericContext {

	private Result result;

	public ResultContext(Result result, boolean isSingleCourseResult) {
		this.result = result;
		put("geco_ResultName", result.getIdentifier());
		put("geco_NbFinishedRunners", result.nbFinishedRunners());
		put("geco_NbPresentRunners", result.nbPresentRunners());
		if( isSingleCourseResult ) {
			put("geco_CourseLength", result.anyCourse().getLength());
			put("geco_CourseClimb", result.anyCourse().getClimb());
		}
	}
	
	public ContextList createRankedRunnersCollection() {
		return createContextList("geco_RankedRunners", result.getRanking().size());
	}
	
	public ContextList createUnrankedRunnersCollection() {
		return createContextList("geco_UnrankedRunners", result.getUnrankedRunners().size());
	}
	
}
