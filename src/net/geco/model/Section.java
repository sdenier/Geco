/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

/**
 * @author Simon Denier
 * @since Jul 15, 2013
 *
 */
public interface Section {

	public static enum SectionType {
		INLINE {
			public String toString() {
				return "Inline";
			}
		},
		FREEORDER {
			public String toString() {
				return "Free Order";
			}
		}
	}

	Section NULL_SECTION = new Section() {
		public void setType(SectionType type) {}
		public void setStartIndex(int index) {}
		public void setName(String name) {}
		public SectionType getType() {return SectionType.INLINE;}
		public int getStartIndex() {return 0;}
		public String getName() {return "";}
		public int[] getCodes(int[] allCodes, int endIndex) {return new int[0];}
		public String displayString() {return "";}
	};

	public int getStartIndex();
	
	public void setStartIndex(int index);
	
	public String getName();
	
	public void setName(String name);
	
	public SectionType getType();
	
	public void setType(SectionType type);
	
	public int[] getCodes(int[] allCodes, int endIndex);

	public String displayString();
	
}
