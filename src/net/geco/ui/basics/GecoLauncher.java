/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.basics;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.geco.control.StageBuilder;
import net.geco.framework.IStageLaunch;
import net.geco.model.Messages;


/**
 * @author Simon Denier
 * @since Jul 17, 2010
 *
 */
public class GecoLauncher extends JDialog {
	
	static {
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private IStageLaunch stageLaunch;
	private IStageLaunch openStage;
	private IStageLaunch createStage;
	private boolean cancelled;
	
	public GecoLauncher(JFrame frame, IStageLaunch stageLaunch, List<IStageLaunch> history) {
		super(frame, "Geco Launch Wizard", true); //$NON-NLS-1$
		setResizable(false);
		setModalityType(DEFAULT_MODALITY_TYPE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		cancelled = true;
		this.stageLaunch = stageLaunch;
		try {
			this.openStage = (IStageLaunch) stageLaunch.clone();
			this.createStage = (IStageLaunch) stageLaunch.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}

		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add( initGUIPanel(history) );
		pack();
		setLocationRelativeTo(null);
	}
	
	public boolean showLauncher() {
		setVisible(true);
		return cancelled;
	}

	public boolean cancelled() {
		return this.cancelled;
	}

	private void cancel() {
		cancelled = true;
		setVisible(false);
	}
	
	private JPanel initGUIPanel(List<IStageLaunch> history) {
		JPanel launchPanel = new JPanel(new BorderLayout());
		launchPanel.add(initOpenPanel(history), BorderLayout.WEST);
		launchPanel.add(initCreationPanel(), BorderLayout.EAST);
		return launchPanel;
	}
	
	private JPanel initOpenPanel(List<IStageLaunch> history) {
		JPanel openWizard = new JPanel();
		openWizard.setLayout(new GridBagLayout());
		openWizard.setBorder(BorderFactory.createTitledBorder("Previous Stages"));

		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		final JList historyL = new JList(history.toArray());
		historyL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		openWizard.add(new JScrollPane(historyL), c);

		JLabel stageDirL = new JLabel("Path:");
		c = SwingUtils.gbConstr(1);
		openWizard.add(stageDirL, c);
		final JTextField stagePathL = new JTextField(openStage.getStageDir());
		stagePathL.setEditable(false);
		stagePathL.setColumns(12);
		openWizard.add(stagePathL, c);

		historyL.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if( !e.getValueIsAdjusting() ){
					openStage = (IStageLaunch) historyL.getSelectedValue();
					stagePathL.setText(openStage.getStageDir());
				}
			}
		});
		historyL.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2 ){
					returnStage();
				}
			}
		});
		
		URL url = getClass().getResource("/resources/icons/crystal/folder_small.png"); //$NON-NLS-1$
		JButton selectPathB = new JButton(new ImageIcon(url));
		openWizard.add(selectPathB, c);
		selectPathB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(openStage.getStageDir());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle(Messages.uiGet("GecoLauncher.Title")); //$NON-NLS-1$
				int returnValue = chooser.showDialog(GecoLauncher.this, "Select");
				if( returnValue==JFileChooser.APPROVE_OPTION ) {
					String basePath = chooser.getSelectedFile().getAbsolutePath();
					openStage.loadFromFileSystem(basePath);
					stagePathL.setText(basePath);
				}
			}
		});
		
		JButton openB = new JButton("Open");
		c = SwingUtils.gbConstr(2);
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_END;
		openWizard.add(openB, c);
		openB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				returnStage();
			}
		});
		
		return openWizard;
	}

	private void returnStage() {
		if( ! StageBuilder.directoryHasData(openStage.getStageDir()) ){
			JOptionPane.showMessageDialog(GecoLauncher.this, "Can't find Geco data in directory", "Error", JOptionPane.WARNING_MESSAGE);
		} else {
			stageLaunch.copyFrom(openStage);
			cancelled = false;
			setVisible(false);
		}
	}

	private JPanel initCreationPanel() {		
		JPanel creationWizard = new JPanel();
		creationWizard.setLayout(new GridBagLayout());
		creationWizard.setBorder(BorderFactory.createTitledBorder("New Stage"));
		
		JLabel stageNameL = new JLabel("Name:");
		GridBagConstraints c = SwingUtils.gbConstr(0);
		creationWizard.add(stageNameL, c);
		final JTextField stageNameF = new JTextField();
		stageNameF.setText(createStage.getStageName());
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		creationWizard.add(stageNameF, c);
		
		JLabel stageDirL = new JLabel("Path:");
		c = SwingUtils.gbConstr(1);
		creationWizard.add(stageDirL, c);
		final JTextField stagePathL = new JTextField(createStage.getStageDir());
		stagePathL.setEditable(false);
		stagePathL.setColumns(12);
		c.fill = GridBagConstraints.HORIZONTAL;
		creationWizard.add(stagePathL, c);
		
		URL url = getClass().getResource("/resources/icons/crystal/folder_small.png"); //$NON-NLS-1$
		JButton selectPathB = new JButton(new ImageIcon(url));
		creationWizard.add(selectPathB, c);
		selectPathB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(createStage.getStageDir());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle(Messages.uiGet("GecoLauncher.Title")); //$NON-NLS-1$
				int returnValue = chooser.showDialog(GecoLauncher.this, "Select");
				if( returnValue==JFileChooser.APPROVE_OPTION ) {
					String basePath = chooser.getSelectedFile().getAbsolutePath();
					createStage.setStageDir(basePath);
					stagePathL.setText(basePath);
				}
			}
		});

		JPanel rulePanel = new JPanel();
		rulePanel.setLayout(new BoxLayout(rulePanel, BoxLayout.Y_AXIS));
		JRadioButton classicAppRB = new JRadioButton("Classic inline");
		classicAppRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createStage.setAppBuilderName("net.geco.app.ClassicAppBuilder");
			}
		});
		JRadioButton orientShowAppRB = new JRadioButton("Orient'Show");
		orientShowAppRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createStage.setAppBuilderName("net.geco.app.OrientShowAppBuilder");
			}
		});
		ButtonGroup builderGroup = new ButtonGroup();
		builderGroup.add(classicAppRB);
		builderGroup.add(orientShowAppRB);
		builderGroup.setSelected(classicAppRB.getModel(), true);
		rulePanel.add(classicAppRB);
		rulePanel.add(orientShowAppRB);
		c = SwingUtils.gbConstr(2);
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		creationWizard.add(rulePanel, c);
		
		JButton createB = new JButton("Create");
		c = SwingUtils.gbConstr(3);
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_END;
		creationWizard.add(createB, c);
		createB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( StageBuilder.directoryHasData(createStage.getStageDir()) ){
					JOptionPane.showMessageDialog(GecoLauncher.this, "Geco data detected! Can't overwrite an existing stage", "Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				String stageName = stageNameF.getText().trim();
				if( stageName.isEmpty() ){
					stageNameF.setText(createStage.getStageName());
					JOptionPane.showMessageDialog(GecoLauncher.this, "Stage name can't be empty", "Warning", JOptionPane.WARNING_MESSAGE);
					return;
				}
				createStage.setStageName(stageName);
				createStage.initDirWithTemplateFiles();
				stageLaunch.copyFrom(createStage);
				cancelled = false;
				setVisible(false);
			}
		});

		return creationWizard;
	}
	
//	throw new GecoWarning(Messages.uiGet("GecoLauncher.CancelCreation")); //$NON-NLS-1$
//	throw new GecoWarning(Messages.uiGet("GecoLauncher.CancelImport")); //$NON-NLS-1$
	
}
