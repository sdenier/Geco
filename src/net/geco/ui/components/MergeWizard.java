/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
		System.exit(0);
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
//		mergedCard = null;
		setVisible(false);
	}

	public void showMergeDialogFor(RunnerRaceData clone, String ecard, Status status) {
		pack();
		setLocationRelativeTo(null);
		setVisible(true);		
	}


	public abstract class MergeSubpanel {

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
			titleBox.add(Box.createHorizontalStrut(INSET));
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
			setInsets(c, 15, 15);
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
	
	public class ECardPanel extends MergeSubpanel {

		public ECardPanel(JComponent panel, int firstLine) {
			super(panel, "ECard Data", firstLine);
		}

		protected void initButtons(JComponent panel) {
			JButton createAnonB = new JButton(GecoIcon.createIcon(GecoIcon.CreateAnon));
			createAnonB.setToolTipText("Create anonymous runner with ecard data");
			JButton cancelB = new JButton(GecoIcon.createIcon(GecoIcon.Cancel));
			cancelB.setToolTipText("Close wizard and cancel the merge");
			cancelB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					close();
				}
			});
			Box buttons = Box.createVerticalBox();
			buttons.add(createAnonB);
			buttons.add(cancelB);
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
			JButton detectCourseB = new JButton(GecoIcon.createIcon(GecoIcon.DetectCourse));
			detectCourseB.setToolTipText("Detect course with best match");
			panel.add(detectCourseB, c);
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

	public class RegistryPanel extends MergeSubpanel {

		public RegistryPanel(JComponent panel, int firstLine) {
			super(panel, "Registry", firstLine);
		}

		protected void initButtons(JComponent panel) {
			JButton mergeRunnerB = new JButton(GecoIcon.createIcon(GecoIcon.MergeRunner));
			mergeRunnerB.setToolTipText("Merge ecard data into selected runner");
			JLabel overwriteWarningL = new JLabel(GecoIcon.createIcon(GecoIcon.Overwrite));
			overwriteWarningL.setToolTipText("Warning! Runner already has ecard data. Merging will overwrite existing data");
			mergeRunnerB.setAlignmentX(CENTER_ALIGNMENT);
			overwriteWarningL.setAlignmentX(CENTER_ALIGNMENT);
			Box buttons = Box.createVerticalBox();
			buttons.add(mergeRunnerB);
			buttons.add(overwriteWarningL);
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
			JComboBox searchRegistryCB = new JComboBox(new String[]{"Runner 1", "Runner 2", "Runner 3"});
			searchRegistryCB.setEditable(true);
			panel.add(searchRegistryCB, c);
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

	public class ArchivePanel extends MergeSubpanel {

		public ArchivePanel(JComponent panel, int firstLine) {
			super(panel, "Archive", firstLine);
		}

		protected void initButtons(JComponent panel) {
			JButton insertArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveAdd));
			insertArchiveB.setToolTipText("Insert runner from archive with ecard data");
			panel.add(insertArchiveB, buttonsCol(2));
		}

		protected void initDataPanel(JComponent panel) {
			initDataLine1(panel);
			initDataLine2(panel);
		}

		private void initDataLine1(JComponent panel) {
			GridBagConstraints c = gridLine();
			c.gridwidth = 4;
			JComboBox searchArchiveCB = new JComboBox(new String[]{"", "Runner 1", "Runner 2", "Runner 3"});
			searchArchiveCB.setEditable(true);
			panel.add(searchArchiveCB, c);
			c.gridwidth = 1;
			JButton lookupArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveSearch));
			lookupArchiveB.setToolTipText("Lookup ecard in archive");
			panel.add(lookupArchiveB, c);
		}

		private void initDataLine2(JComponent panel) {
			GridBagConstraints c = gridLine();
			panel.add(new JLabel("Category"), c);
			panel.add(new DataField(""), c);
			panel.add(new JLabel("Club"), c);
			panel.add(new DataField(""), c);
		}

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

}
