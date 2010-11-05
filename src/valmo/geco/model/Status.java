/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

import java.awt.Color;

public enum Status {
	
	OK {
		public Color color() {
			return new Color(0.6f, 1, 0.6f);
		}
		public boolean isEditable() {
			return true;
		}
	},
	MP {
		public Color color() {
			return new Color(0.75f, 0.5f, 0.75f);
		}
		public boolean isEditable() {
			return true;
		}
	},
	DNS {
		public boolean isTraceable() {
			return false;
		}
	},
	DNF, DSQ,
	NDA { 
		public Color color() {
			return new Color(1, 1, 0.5f);
		}
		public String toString() {
			return "No Data";
		}
		public boolean  hasNoData() {
			return true;
		}
		public boolean isUnresolved() {
			return true;
		}
		public boolean isTraceable() {
			return false;
		}
		public boolean isEditable() {
			return true;
		}
	},
	UNK { 
		public Color color() {
			return new Color(1.0f, 0.5f, 0.5f);
		}
		public String toString() {
			return "Unknown";
		}
		public boolean isUnresolved() {
			return true;
		}
	},
	DUP { 
		public Color color() {
			return new Color(1.0f, 0.5f, 0.5f);
		}
		public String toString() {
			return "Duplicate";
		}
		public boolean isUnresolved() {
			return true;
		}
	};
	
	public Color color() {
		return Color.white;
	}
	
	public boolean  hasNoData() {
		return false;
	}
	
	public boolean isUnresolved() {
		return false;
	}
	
	public boolean isResolved() {
		return ! isUnresolved();
	}
	
	public boolean isTraceable() {
		return true;
	}
	
	public boolean isEditable() {
		return false;
	}
	
}