/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.geco.basics.Announcer;
import net.geco.framework.IGeco;
import net.geco.model.Club;
import net.geco.model.Messages;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since May 25, 2011
 *
 */
public class ClubConfigPanel extends ConfigTablePanel<Club> implements ConfigPanel {

	private ConfigTableModel<Club> tableModel;

	@Override
	public String getLabel() {
		return Messages.uiGet("StagePanel.ClubConfigTitle"); //$NON-NLS-1$
	}

	public ClubConfigPanel(final IGeco geco, final JFrame frame) {
		tableModel = new ConfigTableModel<Club>(new String[] {
										Messages.uiGet("StagePanel.ClubShortNameHeader"), //$NON-NLS-1$
										Messages.uiGet("StagePanel.ClubLongNameHeader")}) { //$NON-NLS-1$
			@Override
			public Object getValueIn(Club club, int columnIndex) {
				switch (columnIndex) {
				case 0: return club.getShortname();
				case 1: return club.getName();
				default: return super.getValueIn(club, columnIndex);
				}
			}
			@Override
			public void setValueIn(Club club, Object value, int col) {
				switch (col) {
				case 0: geco.stageControl().updateShortname(club, (String) value); break;
				case 1: geco.stageControl().updateName(club, (String) value); break;
				default: break;
				}
			}
};
		tableModel.setData(geco.registry().getSortedClubs());
		geco.announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
			public void clubsChanged() {
				tableModel.setData(geco.registry().getSortedClubs());
			}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.stageControl().createClub();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Club club = getSelectedData();
				if( club!=null ) {
					boolean removed = geco.stageControl().removeClub(club);
					if( !removed ) {
						JOptionPane.showMessageDialog(frame,
							    Messages.uiGet("StagePanel.ClubNoDeletionWarning"), //$NON-NLS-1$
							    Messages.uiGet("StagePanel.ActionCancelledTitle"), //$NON-NLS-1$
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		initialize(
				Messages.uiGet("StagePanel.ClubConfigTitle"), //$NON-NLS-1$
				tableModel,
				addAction,
				removeAction);
	}

	@Override
	public Component get() {
		return this;
	}
	
}
