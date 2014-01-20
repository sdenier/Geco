/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Arrays;

import net.geco.model.Messages;
import net.geco.model.Section;

/**
 * @author Simon Denier
 * @since Jul 15, 2013
 *
 */
public class SectionImpl implements Section {

	private int startIndex;
	
	private String name;
	
	private SectionType type;

	private boolean neutralized;

	private int[] codes;

	public SectionImpl() {
		this.type = SectionType.INLINE;
	}
	
	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getStartControl() {
		if( codes != null && codes.length > 0 ) {
			return codes[0];
		} else {
			return 0;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SectionType getType() {
		return type;
	}

	public void setType(SectionType type) {
		this.type = type;
	}

	public boolean neutralized() {
		return neutralized;
	}

	public void setNeutralized(boolean flag) {
		this.neutralized = flag;
	}

	public int[] getCodes() {
		return codes;
	}

	public void setCodes(int[] allCodes, int endIndex) {
		codes = Arrays.copyOfRange(allCodes, startIndex, endIndex);
	}

	public String displayString() {
		if( neutralized() ) {
			return String.format("%s - %s, %s", getName(), getType().toString(), Messages.getString("Section.NeutralizedLabel")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return String.format("%s - %s", getName(), getType().toString()); //$NON-NLS-1$
		}
	}
	
}
