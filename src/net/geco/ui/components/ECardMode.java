/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.geco.control.ECardReadingHandler;
import net.geco.control.ECardTrainingHandler;
import net.geco.control.SIReaderHandler;
import net.geco.ui.basics.GecoIcon;

/**
 * @author Simon Denier
 * @since Mar 19, 2012
 *
 */
public enum ECardMode {
	
	OffMode			("OFF", GecoIcon.EcardOffMode, false) {
		public void execute(SIReaderHandler handler) {
			handler.stop();
		}
		public boolean isActiveMode() {
			return false;
		}
	},
	ReaderMode		("Reading", GecoIcon.EcardReadingMode, true) {
		public void execute(SIReaderHandler handler) {
			handler.selectECardHandler(ECardReadingHandler.class);
			handler.start();
		}
	},
	TrainingMode	("Training", GecoIcon.EcardTrainingMode, true) {
		public void execute(SIReaderHandler handler) {
			handler.selectECardHandler(ECardTrainingHandler.class);
			handler.start();
		}
	},
	RegisterMode	("Register", GecoIcon.EcardRegisterMode, false) {
		public void execute(SIReaderHandler handler) {
			handler.stop(); // TODO: enable
//			handler.selectECardHandler(ECardRegisterHandler.class);
//			handler.start();
		}
	},
	;

	private String title;
	private ImageIcon icon;
	private boolean readMode;

	ECardMode(String title, GecoIcon icon, boolean readMode){
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
