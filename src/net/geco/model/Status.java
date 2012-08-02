/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.awt.Color;


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
		public String iofFormat() {
			return "OK"; //$NON-NLS-1$
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
		public String iofFormat() {
			return "MisPunch"; //$NON-NLS-1$
		}
	},
	DNS {
		public String toString() {
			return Messages.getString("Status.DNSLabel"); //$NON-NLS-1$
		}
		public boolean isTraceable() {
			return false;
		}
		public String iofFormat() {
			return "DidNotStart"; //$NON-NLS-1$
		}		
	},
	DNF {
		public String toString() {
			return Messages.getString("Status.DNFLabel"); //$NON-NLS-1$
		}
		public String iofFormat() {
			return "DidNotFinish"; //$NON-NLS-1$
		}		
	},
	DSQ {
		public String toString() {
			return Messages.getString("Status.DSQLabel"); //$NON-NLS-1$
		}
		public String iofFormat() {
			return "Disqualified"; //$NON-NLS-1$
		}		
	},
	OOT {
		public String toString() {
			return Messages.getString("Status.OOTLabel"); //$NON-NLS-1$
		}
		public String iofFormat() {
			return "OverTime"; //$NON-NLS-1$
		}
	},
	NOS { 
		public Color color() {
			return new Color(1, 1, 0.5f);
		}
		public String toString() {
			return Messages.getString("Status.NotStartedLabel"); //$NON-NLS-1$
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
		public String iofFormat() {
			return "Inactive"; //$NON-NLS-1$
		}		
	},
	RUN { 
		public Color color() {
			return new Color(1, 1, 0.5f);
		}
		public String toString() {
			return Messages.getString("Status.RunningLabel"); //$NON-NLS-1$
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
		public String iofFormat() {
			return "Active"; //$NON-NLS-1$
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
		public String iofFormat() {
			return "Finished"; //$NON-NLS-1$
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
		public String iofFormat() {
			return "Finished"; //$NON-NLS-1$
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

	public String iofFormat() {
		return null;
	}
	
}