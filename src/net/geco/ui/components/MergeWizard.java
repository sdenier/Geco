/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.geco.framework.IGeco;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.PunchPanel;
import net.geco.ui.basics.SwingUtils;

/**
 * @author Simon Denier
 * @since Jul 5, 2012
 *
 */
public class MergeWizard extends JDialog {

	private IGeco geco;
	
	public static void main(String[] args) {
		MergeWizard wizard = new MergeWizard(null, null, "Merge");
		wizard.pack();
		wizard.setLocationRelativeTo(null);
		wizard.setVisible(true);
	}

	public MergeWizard(IGeco geco, JFrame frame, String title) {
		super(frame, title, true);
		this.geco = geco;
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		add(createMergePanel(), BorderLayout.CENTER);
		add(new PunchPanel(), BorderLayout.EAST);
	}

	private JPanel createMergePanel() {
		JPanel mergePanel = new JPanel(new GridBagLayout());
		new ECardPanel(mergePanel, 0);
		new RegistryPanel(mergePanel, 4);
		new ArchivePanel(mergePanel, 8);
		return mergePanel;
	}
	
	private void close() {
		setVisible(false);
	}

	public void showMergeDialogFor(RunnerRaceData clone, String ecard, Status status) {
		pack();
		setLocationRelativeTo(null);
		setVisible(true);		
	}
	
	public static class DataField extends JTextField {
		public DataField(String data) {
			super(data, 5);
			setEditable(false);
			setHorizontalAlignment(CENTER);
		}
	}

	public static class StatusField extends DataField {
		public StatusField(Status status) {
			super(status.toString());
			setBackground(status.color());
		}
	}


	public static abstract class MergeSubpanel {

		protected int nextLine;
		
		public MergeSubpanel(JComponent panel, String title, int firstLineY) {
			this.nextLine = firstLineY;
			initTitle(panel, title);
			initButtons(panel);
			initDataPanel(panel);
		}

		protected void initTitle(JComponent panel, String title) {
			Box titleBox = Box.createHorizontalBox();
			titleBox.add(new JLabel(title));
			titleBox.add(new JSeparator());
			GridBagConstraints c = gridLine();
			c.insets = new Insets(INSET, 2 * INSET, INSET, 2 * INSET);
			c.gridwidth = 6;
			panel.add(titleBox, c);
		}

		protected abstract void initButtons(JComponent panel);

		protected abstract void initDataPanel(JComponent panel);

		protected int nextLine() {
			return nextLine++;
		}

		protected GridBagConstraints gridLine() {
			GridBagConstraints c = SwingUtils.compConstraint(GridBagConstraints.RELATIVE,
															 nextLine(),
															 GridBagConstraints.HORIZONTAL,
															 GridBagConstraints.CENTER);
			resetInsets(c);
			return c;
		}

		protected GridBagConstraints buttonsCol(int colHeight) {
			GridBagConstraints c = SwingUtils.gbConstr(nextLine);
			setInsets(c, 2 * INSET, 20);
			c.anchor = GridBagConstraints.NORTH;
			c.gridheight = colHeight;
			return c;
		}

		protected void resetInsets(GridBagConstraints c) {
			setInsets(c, 0, INSET);
		}

		protected void setInsets(GridBagConstraints c, int left, int right) {
			c.insets = new Insets(TOP, left, 0, right);
		}

		protected static final int TOP = 3;

		protected static final int INSET = 5;
		
	}
	
	public static class ECardPanel extends MergeSubpanel {

		public ECardPanel(JComponent panel, int firstLine) {
			super(panel, "ECard Data", firstLine);
		}

		protected void initButtons(JComponent panel) {
			Box buttons = Box.createVerticalBox();
			buttons.add(new JButton(GecoIcon.createIcon(GecoIcon.CreateAnon)));
			buttons.add(new JButton(GecoIcon.createIcon(GecoIcon.Cancel)));
			panel.add(buttons, buttonsCol(3));
		}

		protected void initDataPanel(JComponent panel) {
			initDataLine1(panel);
			initDataLine2(panel);
			initDataLine3(panel);
		}

		private void initDataLine1(JComponent panel) {
			GridBagConstraints c = gridLine();
			panel.add(new JLabel("ECard"), c);
			panel.add(new DataField("1651011"), c);
			c.gridwidth = 2;
			setInsets(c, 0, 0);
			panel.add(new JComboBox(new String[]{"Course A", "Course B"}), c);
			c.gridwidth = 1;
			resetInsets(c);
			panel.add(new JButton(GecoIcon.createIcon(GecoIcon.DetectCourse)), c);
		}

		private void initDataLine2(JComponent panel) {
			GridBagConstraints c = gridLine();
			panel.add(new JLabel("Start"), c);
			panel.add(new DataField("12:31:00"), c);
			setInsets(c, INSET, INSET);
			panel.add(new JLabel("Finish"), c);
			resetInsets(c);
			panel.add(new DataField("14:02:32"), c);
		}

		private void initDataLine3(JComponent panel) {
			GridBagConstraints c = gridLine();
			panel.add(new JLabel("Status"), c);
			panel.add(new StatusField(Status.OK), c);
			setInsets(c, INSET, INSET);
			panel.add(new JLabel("Time"), c);
			resetInsets(c);
			panel.add(new DataField("1:31:32"), c);
		}
		
	}

	public static class RegistryPanel extends MergeSubpanel {

		public RegistryPanel(JComponent panel, int firstLine) {
			super(panel, "Registry", firstLine);
		}

		protected void initButtons(JComponent panel) {
			JButton mergeB = new JButton(GecoIcon.createIcon(GecoIcon.MergeRunner));
			JLabel warningL = new JLabel(GecoIcon.createIcon(GecoIcon.Overwrite));
			mergeB.setAlignmentX(CENTER_ALIGNMENT);
			warningL.setAlignmentX(CENTER_ALIGNMENT);
			Box buttons = Box.createVerticalBox();
			buttons.add(mergeB);
			buttons.add(warningL);
			panel.add(buttons, buttonsCol(3));
		}

		protected void initDataPanel(JComponent panel) {
			initDataLine1(panel);
			initDataLine2(panel);
			initDataLine3(panel);
		}

		private void initDataLine1(JComponent panel) {
			GridBagConstraints c = gridLine();
			c.anchor = GridBagConstraints.WEST;
			setInsets(c, 0, 0);
			c.gridwidth = 4;
			JComboBox searchRegistry = new JComboBox(new String[]{"Runner 1", "Runner 2", "Runner 3"});
			searchRegistry.setEditable(true);
			panel.add(searchRegistry, c);
		}

		private void initDataLine2(JComponent panel) {
			GridBagConstraints c = gridLine();
			c.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel("Category"), c);
			panel.add(new DataField("H21"), c);
			panel.add(new JLabel("Club"), c);
			panel.add(new DataField("Orient'Alp"), c);
		}

		private void initDataLine3(JComponent panel) {
			GridBagConstraints c = gridLine();
			c.anchor = GridBagConstraints.WEST;
			panel.add(new JLabel("Status"), c);
			panel.add(new StatusField(Status.DSQ), c);
			panel.add(new JLabel("Time"), c);
			panel.add(new DataField("--:--"), c);
		}
		
	}

	public static class ArchivePanel extends MergeSubpanel {

		public ArchivePanel(JComponent panel, int firstLine) {
			super(panel, "Archive", firstLine);
		}

		protected void initButtons(JComponent panel) {
			JButton insertArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveAdd));
			panel.add(insertArchiveB, buttonsCol(2));
		}

		protected void initDataPanel(JComponent panel) {
			initDataLine1(panel);
			initDataLine2(panel);
		}

		private void initDataLine1(JComponent panel) {
			GridBagConstraints c = gridLine();
			c.gridwidth = 4;
			JComboBox searchArchive = new JComboBox(new String[]{"", "Runner 1", "Runner 2", "Runner 3"});
			searchArchive.setEditable(true);
			panel.add(searchArchive, c);
			c.gridwidth = 1;
			JButton searchB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveSearch));
			panel.add(searchB, c);
		}

		private void initDataLine2(JComponent panel) {
			GridBagConstraints c = gridLine();
			panel.add(new JLabel("Category"), c);
			panel.add(new DataField(""), c);
			panel.add(new JLabel("Club"), c);
			panel.add(new DataField(""), c);
		}

	}

}
