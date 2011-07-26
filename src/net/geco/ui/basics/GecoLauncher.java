/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.basics;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

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
	private boolean cancelled;
	
	public GecoLauncher(JFrame frame, IStageLaunch stageLaunch) {
		super(frame, "Geco Launch Wizard", true); //$NON-NLS-1$
		setResizable(false);
		setModalityType(DEFAULT_MODALITY_TYPE);
		cancelled = true;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		initDefaultStageLaunch(stageLaunch);

		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add( initGUIPanel() );
		pack();
		setLocationRelativeTo(null);
	}
	
	public boolean showLauncher() {
		setVisible(true);
		return cancelled;
	}
	
	public void initDefaultStageLaunch(IStageLaunch stageLaunch) {
		this.stageLaunch = stageLaunch;
		this.stageLaunch.setStageName("Stage name");
		this.stageLaunch.setStageDir(System.getProperty("user.dir"));
		this.stageLaunch.setAppBuilderName("net.geco.app.ClassicAppBuilder");
	}

	public boolean cancelled() {
		return this.cancelled;
	}

	private void cancel() {
		cancelled = true;
		setVisible(false);
	}
	
	public JPanel initGUIPanel() {		
		JPanel creationWizard = new JPanel();
		creationWizard.setLayout(new GridBagLayout());
		creationWizard.setBorder(BorderFactory.createTitledBorder("New Stage"));
		
		JLabel stageNameL = new JLabel("Name:");
		GridBagConstraints c = SwingUtils.gbConstr(0);
		creationWizard.add(stageNameL, c);
		final JTextField stageNameF = new JTextField();
		stageNameF.setText(stageLaunch.getStageName());
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		creationWizard.add(stageNameF, c);
		stageNameF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String stageName = stageNameF.getText().trim();
				if( ! stageName.isEmpty() ){
					stageLaunch.setStageName(stageName);
				} else {
					stageNameF.setText(stageLaunch.getStageName());
					JOptionPane.showMessageDialog(GecoLauncher.this, "Stage name can't be empty", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		JLabel stageDirL = new JLabel("Path:");
		c = SwingUtils.gbConstr(1);
		creationWizard.add(stageDirL, c);
		final JTextField stagePathL = new JTextField(stageLaunch.getStageDir());
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
				JFileChooser chooser = new JFileChooser(stageLaunch.getStageDir());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle(Messages.uiGet("GecoLauncher.Title")); //$NON-NLS-1$
				int returnValue = chooser.showDialog(GecoLauncher.this, Messages.uiGet("GecoLauncher.OpenLabel")); //$NON-NLS-1$
				if( returnValue==JFileChooser.APPROVE_OPTION ) {
					String basePath = chooser.getSelectedFile().getAbsolutePath();
					stageLaunch.setStageDir(basePath);
					stagePathL.setText(basePath);
				}
			}
		});

		JPanel rulePanel = new JPanel();
		rulePanel.setLayout(new BoxLayout(rulePanel, BoxLayout.Y_AXIS));
//		rulePanel.setBorder(BorderFactory.createTitledBorder("Rules"));
		JRadioButton classicAppRB = new JRadioButton("Classic inline");
		classicAppRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stageLaunch.setAppBuilderName("net.geco.app.ClassicAppBuilder");
			}
		});
		JRadioButton orientShowAppRB = new JRadioButton("Orient'Show");
		orientShowAppRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stageLaunch.setAppBuilderName("net.geco.app.OrientShowAppBuilder");
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
				if( directoryHasData(stageLaunch.getStageDir()) ){
					JOptionPane.showMessageDialog(GecoLauncher.this, "Can't overwrite an existing stage with new date", "Error", JOptionPane.WARNING_MESSAGE);
				} else {
					stageLaunch.initDirWithTemplateFiles();
					cancelled = false;
					setVisible(false);
				}
			}
		});
		
//		JButton cancelB = new JButton("Cancel");
//		cancelB.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//			}
//		});
//		creationWizard.add(cancelB);

		return creationWizard;
	}
	
//	throw new GecoWarning(Messages.uiGet("GecoLauncher.CancelCreation")); //$NON-NLS-1$
//	throw new GecoWarning(Messages.uiGet("GecoLauncher.CancelImport")); //$NON-NLS-1$
	
	public static boolean directoryHasData(String baseDir) {
		return new File(baseDir + File.separator + "geco.prop").exists(); //$NON-NLS-1$
	}
	
}
