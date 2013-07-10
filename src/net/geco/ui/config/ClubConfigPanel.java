/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
public class ClubConfigPanel extends JPanel implements ConfigPanel {

	@Override
	public String getLabel() {
		return Messages.uiGet("ClubConfigPanel.Title"); //$NON-NLS-1$
	}

	public ClubConfigPanel(final IGeco geco, final JFrame frame) {
		final ConfigTablePanel<Club> clubPanel = new ConfigTablePanel<Club>();
		final ConfigTableModel<Club> tableModel = new ConfigTableModel<Club>(new String[] {
										Messages.uiGet("ClubConfigPanel.ClubShortNameHeader"), //$NON-NLS-1$
										Messages.uiGet("ClubConfigPanel.ClubLongNameHeader")}) { //$NON-NLS-1$
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
				Club club = clubPanel.getSelectedData();
				if( club!=null ) {
					boolean removed = geco.stageControl().removeClub(club);
					if( !removed ) {
						JOptionPane.showMessageDialog(frame,
							    Messages.uiGet("ClubConfigPanel.ClubNoDeletionWarning"), //$NON-NLS-1$
							    Messages.uiGet("ConfigTablePanel.ActionCancelledTitle"), //$NON-NLS-1$
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		clubPanel.initialize(
				Messages.uiGet("ClubConfigPanel.Title"), //$NON-NLS-1$
				tableModel,
				addAction,
				removeAction);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(clubPanel);
	}

	@Override
	public Component build() {
		return this;
	}
	
}
