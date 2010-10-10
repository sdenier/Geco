/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model;

public enum ResultType { 
	
	CourseResult() {
		@Override public String toString() { return "Courses"; }},
		
	CategoryResult() {
		@Override public String toString() { return "Categories"; }},
		
	MixedResult() {
		@Override public String toString() { return "Category/Courses"; }}

}