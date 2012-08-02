/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.geco.control.SIReaderHandler;
import net.geco.control.ecardmodes.ECardRacingMode;
import net.geco.control.ecardmodes.ECardRegisterMode;
import net.geco.control.ecardmodes.ECardTrainingMode;
import net.geco.model.Messages;
import net.geco.ui.basics.GecoIcon;

/**
 * @author Simon Denier
 * @since Mar 19, 2012
 *
 */
public enum ECardModeUI {
	
	OffMode			(Messages.uiGet("ECardModeUI.ReaderButtonOFF"), GecoIcon.EcardOffMode, false) { //$NON-NLS-1$
		public void select(SIReaderHandler handler) {}
		public boolean isActiveMode() {
			return false;
		}
	},
	RaceMode		(Messages.uiGet("ECardModeUI.ReaderButtonRacing"), GecoIcon.EcardRacingMode, true) { //$NON-NLS-1$
		public void select(SIReaderHandler handler) {
			handler.selectECardMode(ECardRacingMode.class);
		}
	},
	TrainingMode	(Messages.uiGet("ECardModeUI.ReaderButtonTraining"), GecoIcon.EcardTrainingMode, true) { //$NON-NLS-1$
		public void select(SIReaderHandler handler) {
			handler.selectECardMode(ECardTrainingMode.class);
		}
	},
	RegisterMode	(Messages.uiGet("ECardModeUI.ReaderButtonRegister"), GecoIcon.EcardRegisterMode, false) { //$NON-NLS-1$
		public void select(SIReaderHandler handler) {
			handler.selectECardMode(ECardRegisterMode.class);
		}
	},
	;

	private String title;
	private ImageIcon icon;
	private boolean readMode;

	ECardModeUI(String title, GecoIcon icon, boolean readMode){
		this.title = title;
		this.icon = GecoIcon.createIcon(icon);
		this.readMode = readMode;
	}

	public String getTitle(){
		return title;
	}
	
	public Icon getIcon(){
		return icon;
	}
	
	public boolean isReadMode() {
		return readMode;
	}

	public boolean isActiveMode() {
		return true;
	}

	public abstract void select(SIReaderHandler handler);

}
