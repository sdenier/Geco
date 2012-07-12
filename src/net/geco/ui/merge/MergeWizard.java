/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.geco.control.MergeControl;
import net.geco.framework.IGeco;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.ui.basics.PunchPanel;

/**
 * @author Simon Denier
 * @since Jul 5, 2012
 *
 */
public class MergeWizard extends JDialog {

	private RunnerRaceData ecardData;

	private IGeco geco;
	private MergeControl mergeControl;

	private ECardBoard ecardBoard;
	private RegistryBoard registryBoard;
	private PunchPanel punchPanel;


	
	public MergeWizard(IGeco geco, JFrame frame, String title) {
		super(frame, title, true);
		this.geco = geco;
		this.mergeControl = geco.mergeControl();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		setResizable(false);
		add(new JLabel("Merge Runner"), BorderLayout.NORTH);
		add(createMergePanel(), BorderLayout.CENTER);
		add(createPunchPanel(), BorderLayout.EAST);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel createMergePanel() {
		JPanel mergePanel = new JPanel(new GridBagLayout());
		ecardBoard = new ECardBoard(this, mergePanel, 0);
		registryBoard = new RegistryBoard(this, mergePanel, 4);
		new ArchiveBoard(this, mergePanel, 9);
		return mergePanel;
	}
	
	private JPanel createPunchPanel() {
		punchPanel = new PunchPanel();
//		punchPanel.setBorder(BorderFactory.createTitledBorder("Trace"));
		return punchPanel;
	}

	public void showMergeDialogFor(RunnerRaceData data, String ecard, Status status) {
		initMockRunner(data, ecard);
		updatePanels();
		setVisible(true);		
	}
	
	public void close() {
//		mergedCard = null;
		setVisible(false);
	}

	private void initMockRunner(RunnerRaceData data, String ecard) {
		this.ecardData = data;
		Runner mockRunner = mergeControl().buildMockRunner();
		mockRunner.setEcard(ecard);
		mockRunner.setCourse(data.getCourse());
		data.setRunner(mockRunner);
	}

	public void updatePanels() {
		ecardBoard.updatePanel(this.ecardData);
		registryBoard.updatePanel();
		punchPanel.refreshPunches(this.ecardData);
	}
	
	public void updateResults() {
		ecardBoard.updateResults();
		punchPanel.refreshPunches(this.ecardData);		
	}

	protected Registry registry() {
		return geco.registry();
	}

	protected MergeControl mergeControl() {
		return mergeControl;
	}
	
}
