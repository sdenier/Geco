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
import net.geco.ui.basics.GecoIcon;

/**
 * @author Simon Denier
 * @since Mar 19, 2012
 *
 */
public enum ECardModeUI {
	
	OffMode			("OFF", GecoIcon.EcardOffMode, false) {
		public void execute(SIReaderHandler handler) {
			handler.stop();
		}
		public boolean isActiveMode() {
			return false;
		}
	},
	RaceMode		("Racing", GecoIcon.EcardRacingMode, true) {
		public void execute(SIReaderHandler handler) {
			handler.selectECardMode(ECardRacingMode.class);
			handler.start();
		}
	},
	TrainingMode	("Training", GecoIcon.EcardTrainingMode, true) {
		public void execute(SIReaderHandler handler) {
			handler.selectECardMode(ECardTrainingMode.class);
			handler.start();
		}
	},
	RegisterMode	("Register", GecoIcon.EcardRegisterMode, false) {
		public void execute(SIReaderHandler handler) {
			handler.selectECardMode(ECardRegisterMode.class);
			handler.start();
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

	public abstract void execute(SIReaderHandler handler);

}
