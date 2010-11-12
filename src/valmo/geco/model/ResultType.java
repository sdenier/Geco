/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model;

import valmo.geco.core.Messages;

public enum ResultType { 
	
	CourseResult() {
		@Override public String toString() {
			return Messages.getString("ResultType.CourseLabel"); }}, //$NON-NLS-1$
		
	CategoryResult() {
		@Override public String toString() {
			return Messages.getString("ResultType.CategoryLabel"); }}, //$NON-NLS-1$
		
	MixedResult() {
		@Override public String toString() {
			return Messages.getString("ResultType.MixedLabel"); }} //$NON-NLS-1$

}