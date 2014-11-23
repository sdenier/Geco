/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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

import net.geco.app.AppBuilder;
import net.geco.app.ClassicAppBuilder;
import net.geco.app.FreeOrderAppBuilder;
import net.geco.app.OrientShowAppBuilder;
import net.geco.app.MultiSectionsAppBuilder;
import net.geco.basics.GecoResources;
import net.geco.control.StageBuilder;
import net.geco.framework.IStageLaunch;
import net.geco.model.Messages;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Jul 17, 2010
 *
 */
public class GecoLauncher extends JDialog {
	
	static {
		SwingUtils.setLookAndFeel();
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private IStageLaunch stageLaunch;
	private IStageLaunch openStage;
	private IStageLaunch createStage;
	private boolean cancelled;
	
	public GecoLauncher(JFrame frame, IStageLaunch stageLaunch, final List<IStageLaunch> history) {
		super(frame, Messages.uiGet("GecoLauncher.Title"), true); //$NON-NLS-1$
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
		getContentPane().add(initGUIPanel(history));
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
		openWizard.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("GecoLauncher.PreviousStagesLabel"))); //$NON-NLS-1$

		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		final JList historyL = new JList(history.toArray());
		historyL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		openWizard.add(new JScrollPane(historyL), c);

		JLabel stageDirL = new JLabel(Messages.uiGet("GecoLauncher.PathLabel")); //$NON-NLS-1$
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
		
		JButton selectPathB = new JButton(GecoIcon.createIcon(GecoIcon.OpenSmall));
		openWizard.add(selectPathB, c);
		selectPathB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(openStage.getStageDir());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle(Messages.uiGet("GecoLauncher.Title")); //$NON-NLS-1$
				int returnValue = chooser.showDialog(GecoLauncher.this, Messages.uiGet("GecoLauncher.SelectPathLabel")); //$NON-NLS-1$
				if( returnValue==JFileChooser.APPROVE_OPTION ) {
					String basePath = chooser.getSelectedFile().getAbsolutePath();
					openStage.loadFromFileSystem(basePath);
					stagePathL.setText(basePath);
				}
			}
		});
		
		JButton openB = new JButton(Messages.uiGet("GecoLauncher.OpenLabel")); //$NON-NLS-1$
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
			JOptionPane.showMessageDialog(
					GecoLauncher.this,
					Messages.uiGet("GecoLauncher.NoGecoDataWarning"), //$NON-NLS-1$
					Messages.uiGet("GecoLauncher.Error"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		} else {
			stageLaunch.copyFrom(openStage);
			cancelled = false;
			setVisible(false);
		}
	}

	private JPanel initCreationPanel() {		
		JPanel creationWizard = new JPanel();
		creationWizard.setLayout(new GridBagLayout());
		creationWizard.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("GecoLauncher.NewStageLabel"))); //$NON-NLS-1$
		
		JLabel stageNameL = new JLabel(Messages.uiGet("GecoLauncher.NameLabel")); //$NON-NLS-1$
		GridBagConstraints c = SwingUtils.gbConstr(0);
		creationWizard.add(stageNameL, c);
		final JTextField stageNameF = new JTextField();
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		creationWizard.add(stageNameF, c);
		
		JLabel stageDirL = new JLabel(Messages.uiGet("GecoLauncher.PathLabel")); //$NON-NLS-1$
		c = SwingUtils.gbConstr(1);
		creationWizard.add(stageDirL, c);
		final JTextField stagePathL = new JTextField(createStage.getStageDir());
		stagePathL.setEditable(false);
		stagePathL.setColumns(12);
		c.fill = GridBagConstraints.HORIZONTAL;
		creationWizard.add(stagePathL, c);
		
		JButton selectPathB = new JButton(GecoIcon.createIcon(GecoIcon.OpenSmall));
		creationWizard.add(selectPathB, c);
		selectPathB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(createStage.getStageDir());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle(Messages.uiGet("GecoLauncher.Title")); //$NON-NLS-1$
				int returnValue = chooser.showDialog(GecoLauncher.this, Messages.uiGet("GecoLauncher.SelectPathLabel")); //$NON-NLS-1$
				if( returnValue==JFileChooser.APPROVE_OPTION ) {
					String basePath = chooser.getSelectedFile().getAbsolutePath();
					createStage.setStageDir(basePath);
					stagePathL.setText(basePath);
				}
			}
		});

		stageNameF.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String stageName = stageNameF.getText().trim();
				String proposedPath = createStage.getStageDir() + GecoResources.sep + stageName.replace(GecoResources.sep, "-"); //$NON-NLS-1$
				createStage.setStageDir(proposedPath);
				stagePathL.setText(proposedPath);
			}
		});
		stageNameF.setText(createStage.getStageName());
		
		JPanel rulePanel = new JPanel();
		rulePanel.setLayout(new BoxLayout(rulePanel, BoxLayout.Y_AXIS));
		ButtonGroup builderGroup = new ButtonGroup();
		JRadioButton classicAppRB =
			addAppRadioButton(ClassicAppBuilder.getName(), ClassicAppBuilder.class, rulePanel, builderGroup); 
		addAppRadioButton(OrientShowAppBuilder.getName(), OrientShowAppBuilder.class, rulePanel, builderGroup);
		addAppRadioButton(FreeOrderAppBuilder.getName(), FreeOrderAppBuilder.class, rulePanel, builderGroup);
		addAppRadioButton(MultiSectionsAppBuilder.getName(), MultiSectionsAppBuilder.class, rulePanel, builderGroup);
		builderGroup.setSelected(classicAppRB.getModel(), true);
		c = SwingUtils.gbConstr(2);
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		creationWizard.add(rulePanel, c);
		
		JButton createB = new JButton(Messages.uiGet("GecoLauncher.CreateLabel")); //$NON-NLS-1$
		c = SwingUtils.gbConstr(3);
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_END;
		creationWizard.add(createB, c);
		createB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( StageBuilder.directoryHasData(createStage.getStageDir()) ){
					JOptionPane.showMessageDialog(GecoLauncher.this, Messages.uiGet("GecoLauncher.ExistingGecoDataWarning"), Messages.uiGet("GecoLauncher.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				String stageName = stageNameF.getText().trim();
				if( stageName.isEmpty() ){
					stageNameF.setText(createStage.getStageName());
					JOptionPane.showMessageDialog(GecoLauncher.this, Messages.uiGet("GecoLauncher.EmptyStageNameWarning"), Messages.uiGet("GecoLauncher.Warning"), JOptionPane.WARNING_MESSAGE);  //$NON-NLS-1$//$NON-NLS-2$
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

	private JRadioButton addAppRadioButton(String appName, final Class<? extends AppBuilder> appClass, JPanel rulePanel, ButtonGroup builderGroup) {
		JRadioButton appRB = new JRadioButton(appName);
		appRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createStage.setAppBuilderName(appClass.getName());
			}
		});
		builderGroup.add(appRB);
		rulePanel.add(appRB);
		return appRB;
	}

}
