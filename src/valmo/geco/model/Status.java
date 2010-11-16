/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

import java.awt.Color;

import valmo.geco.core.Messages;

public enum Status {
	
	OK {
		public Color color() {
			return new Color(0.6f, 1, 0.6f);
		}
		public String toString() {
			return Messages.getString("Status.OKLabel"); //$NON-NLS-1$
		}
		public boolean isRecheckable() {
			return true;
		}
	},
	MP {
		public Color color() {
			return new Color(0.75f, 0.5f, 0.75f);
		}
		public String toString() {
			return Messages.getString("Status.MPLabel"); //$NON-NLS-1$
		}
		public boolean isRecheckable() {
			return true;
		}
	},
	DNS {
		public String toString() {
			return Messages.getString("Status.DNSLabel"); //$NON-NLS-1$
		}
		public boolean isTraceable() {
			return false;
		}
	},
	DNF {
		public String toString() {
			return Messages.getString("Status.DNFLabel"); //$NON-NLS-1$
		}
	},
	DSQ {
		public String toString() {
			return Messages.getString("Status.DSQLabel"); //$NON-NLS-1$
		}
	},
	NDA { 
		public Color color() {
			return new Color(1, 1, 0.5f);
		}
		public String toString() {
			return Messages.getString("Status.NoDataLabel"); //$NON-NLS-1$
		}
		public boolean  hasData() {
			return false;
		}
		public boolean isUnresolved() {
			return true;
		}
		public boolean isTraceable() {
			return false;
		}
	},
	UNK { 
		public Color color() {
			return new Color(1.0f, 0.5f, 0.5f);
		}
		public String toString() {
			return Messages.getString("Status.UnknownLabel"); //$NON-NLS-1$
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
			return Messages.getString("Status.DuplicateLabel"); //$NON-NLS-1$
		}
		public boolean isUnresolved() {
			return true;
		}
	};
	
	public Color color() {
		return Color.white;
	}
	
	public boolean hasData() {
		return true;
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
	
	public boolean isRecheckable() {
		return false;
	}
	
}