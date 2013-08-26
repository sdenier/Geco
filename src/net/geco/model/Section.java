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
		public int getStartControl() { return 0; }
		public int getStartIndex() {return 0;}
		public String getName() {return "";}
		public int[] getCodes() {return new int[0];}
		public void setCodes(int[] allCodes, int endIndex) {}
		public String displayString() {return "";}
		public boolean neutralized() { return false; }
		public void setNeutralized(boolean flag) {}
	};

	public int getStartIndex();
	
	public void setStartIndex(int index);
	
	public int getStartControl();
	
	public String getName();
	
	public void setName(String name);
	
	public SectionType getType();
	
	public void setType(SectionType type);

	public boolean neutralized();

	public void setNeutralized(boolean flag);
	
	public int[] getCodes();

	public void setCodes(int[] allCodes, int endIndex);

	public String displayString();
	
}
