/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import valmo.geco.model.Messages;
import valmo.geco.ui.basics.SwingUtils;

/**
 * @author Simon Denier
 * @since Sep 5, 2010
 *
 */
public class LiveConfigPanel extends JPanel {

	private Component frame;
	private LiveComponent liveComponent;

	private JButton mapfileB;
	private JButton coursefileB;
	private JLabel mapfileL;
	private JLabel coursefileL;

	private JSpinner dpiS;
	private JSpinner xfactorS;
	private JSpinner yfactorS;
	private JSpinner xtranS;
	private JSpinner ytranS;
	private JButton refreshB;
	
	private JButton showControlsB;
	private JButton showMapB;
	private JComboBox showCourseCB;
	

	public LiveConfigPanel(JFrame frame, LiveComponent liveComp) {
		this(frame, liveComp, false);
	}
	
	public LiveConfigPanel(JFrame frame, LiveComponent liveComp, boolean withLiveNetwork) {
		this.frame = frame;
		this.liveComponent = liveComp;
		createComponents(withLiveNetwork);
		initListeners();
		initConfigPanel();
	}
	
	public static float dpi2dpmmFactor(float dpi) {
		return dpi / 25.4f; // dpi / mm/inch
	}
	
	private void createComponents(boolean withLiveNetwork) {
		// data files
		mapfileB = new JButton(Messages.liveGet("LiveConfigPanel.MapImageLabel")); //$NON-NLS-1$
		mapfileL = new JLabel();
		coursefileB = new JButton(Messages.liveGet("LiveConfigPanel.CourseFileLabel")); //$NON-NLS-1$
		coursefileL = new JLabel();
		// map config
		dpiS = new JSpinner(new SpinnerNumberModel(150, 0, null, 50));
		dpiS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		xfactorS = new JSpinner(new SpinnerNumberModel(150 / 25.4f, 1f, null, 0.1f));
		xfactorS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		yfactorS = new JSpinner(new SpinnerNumberModel(150 / 25.4f, 1f, null, 0.1f));
		yfactorS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		xtranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		xtranS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		ytranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		ytranS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		refreshB = new JButton(Messages.liveGet("LiveConfigPanel.RefreshLabel")); //$NON-NLS-1$
		// course config
		showControlsB = new JButton(Messages.liveGet("LiveConfigPanel.ShowControlsLabel")); //$NON-NLS-1$
		showMapB = new JButton(Messages.liveGet("LiveConfigPanel.ShowMapLabel")); //$NON-NLS-1$
		showCourseCB = new JComboBox();
	}
	
	private void initListeners() {
		mapfileB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.liveGet("LiveConfigPanel.ImageDialogTitle")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						mapfileL.setText(fileChooser.getSelectedFile().getName());
						liveComponent.loadMapImage(fileChooser.getSelectedFile().getCanonicalPath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		coursefileB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.liveGet("LiveConfigPanel.XmlDialogTitle")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						coursefileL.setText(fileChooser.getSelectedFile().getName());
						liveComponent.importCourseData(fileChooser.getSelectedFile().getCanonicalPath());
						showCourseCB.setModel(new DefaultComboBoxModel(liveComponent.coursenames()));
						refreshCourses();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		dpiS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				xfactorS.setValue(dpi2dpmmFactor(((Number) dpiS.getValue()).floatValue()));
				yfactorS.setValue(dpi2dpmmFactor(((Number) dpiS.getValue()).floatValue()));
			}
		});
		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshCourses();
			}
		});
		showControlsB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveComponent.displayAllControls();
			}
		});
		showMapB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveComponent.displayMap();
			}
		});
		showCourseCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveComponent.displayCourse((String) showCourseCB.getSelectedItem());
			}
		});
	}
	
	private void refreshCourses() {
		liveComponent.createCourses(
				((Number) xfactorS.getValue()).floatValue(),
				((Number) yfactorS.getValue()).floatValue(),
				((Number) xtranS.getValue()).intValue(),
				((Number) ytranS.getValue()).intValue());
		liveComponent.displayAllControls();
	}

	private JPanel initConfigPanel() {
		JPanel datafileP = new JPanel(new GridLayout(0, 2));
		datafileP.setBorder(BorderFactory.createTitledBorder(Messages.liveGet("LiveConfigPanel.LoadDataLabel"))); //$NON-NLS-1$
		addComponent(datafileP, mapfileB);
		addComponent(datafileP, mapfileL);
		addComponent(datafileP, coursefileB);
		addComponent(datafileP, coursefileL);
				
		JPanel mapConfigP = new JPanel(new GridLayout(0, 2));
		mapConfigP.setBorder(BorderFactory.createTitledBorder(Messages.liveGet("LiveConfigPanel.SetupLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.ImageDpiLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, dpiS);
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.XFactorLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.YFactorLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, xfactorS);
		addComponent(mapConfigP, yfactorS);
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.XTranslationLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.YTranslationLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, xtranS);
		addComponent(mapConfigP, ytranS);
		addComponent(mapConfigP, refreshB);
		
		JPanel courseConfigP = new JPanel(new GridLayout(0, 2));
		courseConfigP.setBorder(BorderFactory.createTitledBorder(Messages.liveGet("LiveConfigPanel.CheckCoursesLabel"))); //$NON-NLS-1$
		addComponent(courseConfigP, showControlsB);
		addComponent(courseConfigP, showMapB);
		addComponent(courseConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.ShowCourseLabel"))); //$NON-NLS-1$
		addComponent(courseConfigP, showCourseCB);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(datafileP);
		add(mapConfigP);
		add(courseConfigP);
		
		return this;
	}
	
	public void addComponent(Container cont, Component comp) {
		cont.add(SwingUtils.embed(comp));
	}

	public void setProperties(Properties liveProp) {
		mapfileL.setText(liveProp.getProperty("MapFile")); //$NON-NLS-1$
		coursefileL.setText(liveProp.getProperty("CourseFile")); //$NON-NLS-1$
		dpiS.setValue(new Integer(liveProp.getProperty("DPI"))); //$NON-NLS-1$
		xtranS.setValue(new Integer(liveProp.getProperty("XTrans"))); //$NON-NLS-1$
		ytranS.setValue(new Integer(liveProp.getProperty("YTrans"))); //$NON-NLS-1$
		showCourseCB.setModel(new DefaultComboBoxModel(liveComponent.coursenames()));
		refreshCourses();
	}
	
}
