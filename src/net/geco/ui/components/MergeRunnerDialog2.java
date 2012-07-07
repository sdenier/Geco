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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
public class MergeRunnerDialog2 extends JDialog {

	private IGeco geco;
	
	public static void main(String[] args) {
		MergeRunnerDialog2 dialog2 = new MergeRunnerDialog2(null, null, "Merge");
		dialog2.pack();
		dialog2.setLocationRelativeTo(null);
		dialog2.setVisible(true);
	}

	public MergeRunnerDialog2(IGeco geco, JFrame frame, String title) {
		super(frame, title, true);
		this.geco = geco;
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		add(new PunchPanel(), BorderLayout.EAST);
		Box box = Box.createVerticalBox();
		box.add(new ECardPanel());
		box.add(new RegistryPanel());
		box.add(new ArchivePanel());
		add(box, BorderLayout.CENTER);
	}

	private void close() {
		setVisible(false);
	}

	public void showMergeDialogFor(RunnerRaceData clone, String ecard, Status status) {
		pack();
		setLocationRelativeTo(null);
		setVisible(true);		
	}

	private static GridBagConstraints gridConstraints(int gridY) {
		GridBagConstraints c = SwingUtils.compConstraint(GridBagConstraints.RELATIVE,
														 gridY,
														 GridBagConstraints.HORIZONTAL,
														 GridBagConstraints.CENTER);
		resetInsets(c);
		return c;
	}

	private static void resetInsets(GridBagConstraints c) {
		setInsets(c, 0, INSET);
	}

	private static final int TOP = 3;
	private static final int INSET = 5;
	
	private static void setInsets(GridBagConstraints c, int left, int right) {
		c.insets = new Insets(TOP, left, 0, right);
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

	public static class ECardPanel extends JPanel {

		public ECardPanel() {
			setBorder(BorderFactory.createTitledBorder("ECard Data"));
			setLayout(new GridBagLayout());
			initButtons();
			initDataPanel();
		}

		private void initButtons() {
			Box buttons = Box.createVerticalBox();
			buttons.add(new JButton(GecoIcon.createIcon(GecoIcon.CreateAnon)));
			buttons.add(new JButton(GecoIcon.createIcon(GecoIcon.Cancel)));
			GridBagConstraints c = SwingUtils.gbConstr();
			setInsets(c, 2 * INSET, 20);
			c.anchor = GridBagConstraints.NORTH;
			c.gridheight = 3;
			add(buttons, c);
		}

		private void initDataPanel() {
			initDataLine1();
			initDataLine2();
			initDataLine3();
		}

		private void initDataLine1() {
			GridBagConstraints c = gridConstraints(0);
			add(new JLabel("ECard"), c);
			add(new DataField("1651011"), c);
			c.gridwidth = 2;
			setInsets(c, 0, 0);
			add(new JComboBox(new String[]{"Course A", "Course B"}), c);
			c.gridwidth = 1;
			resetInsets(c);
			add(new JButton(GecoIcon.createIcon(GecoIcon.DetectCourse)), c);
		}

		private void initDataLine2() {
			GridBagConstraints c = gridConstraints(1);
			add(new JLabel("Start"), c);
			add(new DataField("12:31:00"), c);
			setInsets(c, INSET, INSET);
			add(new JLabel("Finish"), c);
			c.gridwidth = 2;
			resetInsets(c);
			add(new DataField("14:02:32"), c);
		}

		private void initDataLine3() {
			GridBagConstraints c = gridConstraints(2);
			add(new JLabel("Status"), c);
			add(new StatusField(Status.OK), c);
			setInsets(c, INSET, INSET);
			add(new JLabel("Time"), c);
			c.gridwidth = 2;
			resetInsets(c);
			add(new DataField("1:31:32"), c);
		}
		
	}

	public static class RegistryPanel extends JPanel {

		public RegistryPanel() {
			setBorder(BorderFactory.createTitledBorder("Registry"));
			setLayout(new GridBagLayout());
			initButtons();
			initDataPanel();
		}

		private void initButtons() {
			Box buttons = Box.createVerticalBox();
			buttons.add(new JButton(GecoIcon.createIcon(GecoIcon.MergeRunner)));
			buttons.add(new JLabel(GecoIcon.createIcon(GecoIcon.Overwrite)));
			GridBagConstraints c = SwingUtils.gbConstr();
			setInsets(c, 2 * INSET, 20);
			c.anchor = GridBagConstraints.NORTH;
			c.gridheight = 3;
			add(buttons, c);
		}

		private void initDataPanel() {
			initDataLine1();
			initDataLine2();
			initDataLine3();
		}

		private void initDataLine1() {
			GridBagConstraints c = gridConstraints(0);
			setInsets(c, 0, 0);
			c.gridwidth = 4;
			JComboBox searchRegistry = new JComboBox(new String[]{"Runner 1", "Runner 2", "Runner 3"});
			searchRegistry.setEditable(true);
			add(searchRegistry, c);
		}

		private void initDataLine2() {
			GridBagConstraints c = gridConstraints(1);
			add(new JLabel("Category"), c);
			add(new DataField("H21"), c);
			add(new JLabel("Club"), c);
			add(new DataField("Orient'Alp"), c);
		}

		private void initDataLine3() {
			GridBagConstraints c = gridConstraints(2);
			add(new JLabel("Status"), c);
			add(new StatusField(Status.DSQ), c);
			add(new JLabel("Time"), c);
			add(new DataField("--:--"), c);
		}
		
	}

	public static class ArchivePanel extends JPanel {

		public ArchivePanel() {
			setBorder(BorderFactory.createTitledBorder("Archive"));
			setLayout(new GridBagLayout());
			initButtons();
			initDataPanel();
		}

		private void initButtons() {
			JButton insertArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveAdd));
			GridBagConstraints c = SwingUtils.gbConstr();
			setInsets(c, 2 * INSET, 20);
			c.anchor = GridBagConstraints.NORTH;
			c.gridheight = 2;
			add(insertArchiveB, c);
		}

		private void initDataPanel() {
			initDataLine1();
			initDataLine2();
		}

		private void initDataLine1() {

			GridBagConstraints c = gridConstraints(0);
			c.gridwidth = 4;
			JComboBox searchArchive = new JComboBox(new String[]{"", "Runner 1", "Runner 2", "Runner 3"});
			searchArchive.setEditable(true);
			add(searchArchive, c);
			c.gridwidth = 1;
			JButton searchB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveSearch));
			add(searchB, c);
		}

		private void initDataLine2() {
			GridBagConstraints c = gridConstraints(1);
			add(new JLabel("Category"), c);
			add(new DataField(""), c);
			add(new JLabel("Club"), c);
//			c.gridwidth = 2;
			add(new DataField(""), c);
		}

	}

}
