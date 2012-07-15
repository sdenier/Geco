/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.geco.basics.Html;
import net.geco.control.MergeControl;
import net.geco.framework.IGeco;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.PunchPanel;
import net.geco.ui.basics.SwingUtils;

/**
 * @author Simon Denier
 * @since Jul 5, 2012
 *
 */
public class MergeWizard extends JDialog {

	private RunnerRaceData ecardData;
	private Runner sourceRunner;
	private String mergedECard;

	private IGeco geco;
	private MergeControl mergeControl;

	private JLabel mergeInfo;
	private ECardBoard ecardBoard;
	private RegistryBoard registryBoard;
	private PunchPanel punchPanel;
	private ArchiveBoard archiveBoard;

	
	public MergeWizard(IGeco geco, JFrame frame) {
		super(frame, "Merge Wizard", true);
		this.geco = geco;
		this.mergeControl = geco.mergeControl();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeAndReturn(null);
			}
		});
		setResizable(false);
		mergeInfo = new JLabel("");
		add(SwingUtils.embed(mergeInfo), BorderLayout.NORTH);
		add(createMergePanel(), BorderLayout.CENTER);
		add(createPunchPanel(), BorderLayout.EAST);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel createMergePanel() {
		JPanel mergePanel = new JPanel(new GridBagLayout());
		ecardBoard = new ECardBoard(this, mergePanel, 0);
		registryBoard = new RegistryBoard(this, mergePanel, 4);
		archiveBoard = new ArchiveBoard(this, mergePanel, 9);
		return mergePanel;
	}
	
	private JPanel createPunchPanel() {
		punchPanel = new PunchPanel();
		return punchPanel;
	}
	
	private void setInfo(String text) {
		mergeInfo.setText(Html.htmlTag("b", text));
	}
	
	public void closeAndReturn(String ecard) {
		mergedECard = ecard;
		setVisible(false);
	}

	public void closeAfterCreate(RunnerRaceData data) {
		geco.log("Creation " + data.infoString()); //$NON-NLS-1$
		closeAndReturn(data.getRunner().getEcard());
	}
	
	public void closeAfterMerge(RunnerRaceData data) {
		askForRunnerDeletion();
		geco.log("Merge " + data.infoString()); //$NON-NLS-1$
		closeAndReturn(data.getRunner().getEcard());
	}

	public void closeAfterInsert(RunnerRaceData data) {
		askForRunnerDeletion();
		geco.log("Insert " + data.infoString()); //$NON-NLS-1$
		closeAndReturn(data.getRunner().getEcard());
	}

	private void askForRunnerDeletion() {
		if( sourceRunner != null ) {// offer to delete source runner if applicable
			int confirm = JOptionPane.showConfirmDialog(
							this,
							Messages.uiGet("MergeRunnerDialog.RunnerDeletionLabel") + sourceRunner.idString(), //$NON-NLS-1$
							Messages.uiGet("MergeRunnerDialog.RunnerDeletionTitle"), //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION);
			if( confirm == JOptionPane.YES_OPTION ) {
				geco.log("Delete " + sourceRunner.idString()); //$NON-NLS-1$
				mergeControl().deleteRunner(sourceRunner);
			}
		}
	}

	public String showMergeRunner(RunnerRaceData data) {
		setInfo("Selected runner " + data.getRunner().idString());
		sourceRunner = data.getRunner();
		initMockRunner(data, sourceRunner.getEcard(), data.getCourse());
		openMergeWizard();
		return mergedECard;
	}

	public String showMergeUnknownECard(RunnerRaceData data, String ecard, Course course) {
		setInfo("Unknown ecard " + ecard);
		initMockRunner(data, ecard, course);
		openMergeWizard();
		return mergedECard;
	}

	public String showMergeDuplicateECard(RunnerRaceData data, Runner target, Course course) {
		setInfo("Duplicate ecard " + target.idString());
		initMockRunner(data, target.getEcard(), course);
		selectTargetRunner(target);
		openMergeWizard();
		return mergedECard;
	}

	private void initMockRunner(RunnerRaceData data, String ecard, Course course) {
		this.ecardData = data;
		Runner mockRunner = mergeControl().buildMockRunner();
		mockRunner.setEcard(ecard);
		mockRunner.setCourse(course);
		data.setRunner(mockRunner);
	}

	private void openMergeWizard() {
		updatePanels();
		setVisible(true);
	}

	public void updatePanels() {
		ecardBoard.updatePanel();
		registryBoard.updatePanel();
		archiveBoard.updatePanel();
		punchPanel.refreshPunches(this.ecardData);
	}

	private void selectTargetRunner(Runner target) {
		registryBoard.selectTargetRunner(target);
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
	
	protected RunnerRaceData getECardData() {
		return ecardData;
	}
	
	protected Runner getSourceRunner() {
		return sourceRunner;
	}

	public void catchException(IOException ex) {
		JOptionPane.showMessageDialog(
				this,
				ex.toString(),
				"Exception in wizard",
				JOptionPane.ERROR_MESSAGE);
	}
	
}
