/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.basics;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * @author Simon Denier
 * @since Mar 19, 2012
 *
 */
public enum GecoIcon {
	
	Open 		("folder_new.png"), //$NON-NLS-1$
// TODO: remove duplicate icon?
	OpenSmall	("folder_small.png"), //$NON-NLS-1$
	Save 		("folder_sent_mail.png"), //$NON-NLS-1$
	RecheckAll	("quick_restart.png"), //$NON-NLS-1$
	// TODO: remove unused icons
//	Manual		("advanced.png"), //$NON-NLS-1$
//	Auto		("restart.png"), //$NON-NLS-1$
	SplitOff	("fileprint.png"), //$NON-NLS-1$
	SplitOn		("filequickprint.png"), //$NON-NLS-1$
	OpenLiveMap	("search.png"), //$NON-NLS-1$
	LiveOff		("irkick.png"), //$NON-NLS-1$
	LiveOn		("irkickflash.png"), //$NON-NLS-1$

	EcardOffMode		("exit.png"), //$NON-NLS-1$
	EcardRacingMode		("cnr.png"), //$NON-NLS-1$
	EcardTrainingMode	("cnrgrey.png"), //$NON-NLS-1$
	EcardRegisterMode	("graphic-design.png"), //$NON-NLS-1$
	
	ImportFile	("fileimport.png"), //$NON-NLS-1$
	OpenArchive	("db.png"), //$NON-NLS-1$
	ResetTime	("history.png"), //$NON-NLS-1$
	SplitPrint	("filequickprint_small.png"), //$NON-NLS-1$

	SelectFiles	("fileopen.png"), //$NON-NLS-1$
	Reset		("cancel.png"), //$NON-NLS-1$
	Help		("documentinfo.png"), //$NON-NLS-1$

	CreateAnon		("edit_add.png"), //$NON-NLS-1$
	Cancel			("button_cancel.png"), //$NON-NLS-1$
	DetectCourse	("restart_small.png"), //$NON-NLS-1$
	MergeRunner		("apply.png"), //$NON-NLS-1$
	Overwrite		("messagebox_warning.png"), //$NON-NLS-1$
	ArchiveAdd		("db_add.png"), //$NON-NLS-1$
	ArchiveSearch	("find_small.png"); //$NON-NLS-1$
	
	private String resourceName;
		
	GecoIcon(String resourceName) {
		this.resourceName = resourceName;
	}
		
	public String resourcePath() {
		return THEME + this.resourceName;
	}

	private static final String THEME = "crystal/"; //$NON-NLS-1$
	
	public static ImageIcon createIcon(GecoIcon icon) {
		return createImageIcon(icon.resourcePath());
	}

	public static ImageIcon createImageIcon(String path) {
		URL url = GecoIcon.class.getResource("/resources/icons/" + path); //$NON-NLS-1$
		return new ImageIcon(url);
	}
	
}
