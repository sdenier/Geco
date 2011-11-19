/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.model.Messages;
import net.geco.ui.basics.StartStopButton;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Sep 5, 2010
 *
 */
public class LiveConfigPanel extends JPanel {

	public enum AdjustStep { START_CONTROL, START_MAP, END_CONTROL, END_MAP };
	
	public class AdjustButton extends StartStopButton {
		private MouseAdapter mapAdjuster;
		private Color defaultColor;
		
		@Override
		public void initialize() {
			super.initialize();
			setText(Messages.liveGet("LiveConfigPanel.AdjustLabel")); //$NON-NLS-1$
		}
		@Override
		public void actionOn() {
			defaultColor = getBackground();
			setBackground(Color.green);
				
			mapAdjuster = new MouseAdapter() {
				private AdjustStep step = AdjustStep.START_CONTROL;
				private Point controlPoint;
				private Point controlPoint2;
				
				public void mouseClicked(MouseEvent e) {
					ControlCircle control;
					super.mouseClicked(e);
					switch (step) {
					case START_CONTROL:
						control = liveComponent.mapComponent().findControlNextTo(e.getPoint());
						if( control!=null ){
							controlL.setText(control.getCode());
							controlPoint = control.getPosition();
							step = AdjustStep.START_MAP;
						}
						instructionsL.setText(Html.htmlTag("i", Messages.liveGet("LiveConfigPanel.Step2Instruction"))); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					case START_MAP:
						mapPoint = e.getPoint(); // new origin
						int ddx = mapPoint.x - controlPoint.x;
						xtranS.setValue(new Integer(((Integer) xtranS.getValue()).intValue() + ddx));
						int ddy = mapPoint.y - controlPoint.y;
						ytranS.setValue(new Integer(((Integer) ytranS.getValue()).intValue() + ddy));
						translateControls(ddx, ddy);
						liveComponent.displayAllControls();
						step = AdjustStep.END_CONTROL;
						instructionsL.setText(Html.htmlTag("i", Messages.liveGet("LiveConfigPanel.Step3Instruction"))); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					case END_CONTROL:
						control = liveComponent.mapComponent().findControlNextTo(e.getPoint());
						if( control!=null ){
							controlPoint2 = control.getPosition();
							step = AdjustStep.END_MAP;
							instructionsL.setText(Html.htmlTag("i", Messages.liveGet("LiveConfigPanel.Step4Instruction"))); //$NON-NLS-1$ //$NON-NLS-2$
						}
						break;
					case END_MAP:
						Point mapPoint2 = e.getPoint();
						float controlDx = controlPoint2.x - mapPoint.x;
						float mapDx = mapPoint2.x - mapPoint.x;
						xfactorS.setValue(new Float(mapDx / controlDx));

						float controlDy = controlPoint2.y - mapPoint.y;
						float mapDy = mapPoint2.y - mapPoint.y;
						yfactorS.setValue(new Float(mapDy / controlDy));
						
						step = AdjustStep.START_CONTROL;
						adjustControls();
						liveComponent.displayAllControls();
						doOffAction();
						break;
					}
				}
			};
			liveComponent.mapComponent().addMouseListener(mapAdjuster);
			instructionsL.setText(Html.htmlTag("i", Messages.liveGet("LiveConfigPanel.Step1Instruction"))); //$NON-NLS-1$ //$NON-NLS-2$
			instructionsL.setVisible(true);
		}
		
		@Override
		public void actionOff() {
			instructionsL.setVisible(false);
			liveComponent.mapComponent().removeMouseListener(mapAdjuster);
			setBackground(defaultColor);
		}
	}

	private Component frame;
	private LiveComponent liveComponent;

	private String baseDir;
	private File mapFile;
	private File courseFile;
	private Point mapPoint;
	
	private JButton mapfileB;
	private JButton coursefileB;
	private JLabel mapfileL;
	private JLabel coursefileL;

	private JSpinner dpiS;
	private JSpinner xtranS;
	private JSpinner ytranS;
	private JLabel controlL;
	private JSpinner xfactorS;
	private JSpinner yfactorS;
	private JButton refreshB;
	private AdjustButton adjustB;
	
	private JButton showControlsB;
	private JButton showMapB;
	private JComboBox showCourseCB;
	private JButton saveB;
	private JLabel instructionsL;
	

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
		xtranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		xtranS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		ytranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		ytranS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		controlL = new JLabel();
		xfactorS = new JSpinner(new SpinnerNumberModel(1.0f, 0f, null, 0.1f));
		xfactorS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		yfactorS = new JSpinner(new SpinnerNumberModel(1.0f, 0f, null, 0.1f));
		yfactorS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		refreshB = new JButton(Messages.liveGet("LiveConfigPanel.RefreshLabel")); //$NON-NLS-1$
		adjustB = new AdjustButton();
		// course config
		showControlsB = new JButton(Messages.liveGet("LiveConfigPanel.ShowControlsLabel")); //$NON-NLS-1$
		showMapB = new JButton(Messages.liveGet("LiveConfigPanel.ShowMapLabel")); //$NON-NLS-1$
		showCourseCB = new JComboBox();
		saveB = new JButton(Messages.liveGet("LiveConfigPanel.SaveLabel")); //$NON-NLS-1$
		saveB.setToolTipText(Messages.liveGet("LiveConfigPanel.SaveTooltip")); //$NON-NLS-1$
		instructionsL = new JLabel();
		instructionsL.setVisible(false);
	}
	
	private void initListeners() {
		mapfileB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.liveGet("LiveConfigPanel.ImageDialogTitle")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						mapFile = fileChooser.getSelectedFile();
						liveComponent.loadMapImage(mapFile.getCanonicalPath());
						refreshMapfileName();
					} catch (IOException e1) {
						showFileErrorDialog(e1);
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
						courseFile = fileChooser.getSelectedFile();
						liveComponent.importCourseData(courseFile.getCanonicalPath());
						refreshCourseNames();
						refreshCourses();
					} catch (IOException e1) {
						showFileErrorDialog(e1);
					}
				}
			}
		});
		dpiS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float factor = dpi2dpmmFactor(((Number) dpiS.getValue()).floatValue());
				liveComponent.createControls(factor);
				liveComponent.createCourses();
				liveComponent.displayAllControls();
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
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveProperties();
			}
		});
	}
	
	private void translateControls() {
		translateControls(
				((Number) xtranS.getValue()).intValue(),
				((Number) ytranS.getValue()).intValue());
	}

	private void translateControls(int dx, int dy) {
		liveComponent.translateControls(dx, dy);
	}

	private void adjustControls() {
		if( mapPoint!=null ){
			liveComponent.adjustControls(
					mapPoint.x,
					mapPoint.y,
					((Number) xfactorS.getValue()).floatValue(),
					((Number) yfactorS.getValue()).floatValue());			
		}
	}
	
	public void refreshCourses() {
		float factor = dpi2dpmmFactor(((Number) dpiS.getValue()).floatValue());
		liveComponent.createControls(factor);
		liveComponent.createCourses();
		translateControls();
		adjustControls();
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
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.XTranslationLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.YTranslationLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, xtranS);
		addComponent(mapConfigP, ytranS);
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.CenteredControlLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, controlL);
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.XFactorLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, new JLabel(Messages.liveGet("LiveConfigPanel.YFactorLabel"))); //$NON-NLS-1$
		addComponent(mapConfigP, xfactorS);
		addComponent(mapConfigP, yfactorS);
		addComponent(mapConfigP, refreshB);
		addComponent(mapConfigP, adjustB);
		
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
		
		addComponent(this, saveB);
		addComponent(this, instructionsL);
		
		return this;
	}
	
	protected void addComponent(Container cont, Component comp) {
		cont.add(SwingUtils.embed(comp));
	}
	
	public void loadFromProperties(String dir) {
		this.baseDir = dir;
		try {
			Properties liveProp = new Properties();
			liveProp.load(GecoResources.getSafeReaderFor(dir + GecoResources.sep + "live.prop")); //$NON-NLS-1$
			liveComponent.loadMapImage(liveProp.getProperty("MapFile")); //$NON-NLS-1$
			liveComponent.importCourseData(liveProp.getProperty("CourseFile")); //$NON-NLS-1$
			setProperties(liveProp);
		} catch (FileNotFoundException f) {
			// do nothing
		} catch (IOException e) {
			showFileErrorDialog(e);
		}
	}

	protected void setProperties(Properties liveProp) {
		mapFile = new File(liveProp.getProperty("MapFile")); //$NON-NLS-1$
		refreshMapfileName();
		courseFile = new File(liveProp.getProperty("CourseFile")); //$NON-NLS-1$
		refreshCourseNames();
		dpiS.setValue(new Integer(liveProp.getProperty("DPI"))); //$NON-NLS-1$
		xtranS.setValue(new Integer(liveProp.getProperty("XTrans"))); //$NON-NLS-1$
		ytranS.setValue(new Integer(liveProp.getProperty("YTrans"))); //$NON-NLS-1$
		String control = liveProp.getProperty("Control"); //$NON-NLS-1$
		if( control!=null ){
			controlL.setText(control);
			mapPoint = new Point(
					Integer.parseInt(liveProp.getProperty("XMap")), //$NON-NLS-1$
					Integer.parseInt(liveProp.getProperty("YMap"))); //$NON-NLS-1$
		}
		xfactorS.setValue(new Float(liveProp.getProperty("XFactor", "1.0"))); //$NON-NLS-1$ //$NON-NLS-2$
		yfactorS.setValue(new Float(liveProp.getProperty("YFactor", "1.0"))); //$NON-NLS-1$ //$NON-NLS-2$
		refreshCourses();
	}
	
	public void saveProperties() {
		Properties prop = new Properties();
		prop.setProperty("MapFile", mapFile.getAbsolutePath()); //$NON-NLS-1$
		prop.setProperty("CourseFile", courseFile.getAbsolutePath()); //$NON-NLS-1$
		prop.setProperty("DPI", dpiS.getValue().toString()); //$NON-NLS-1$
		prop.setProperty("XTrans", xtranS.getValue().toString()); //$NON-NLS-1$
		prop.setProperty("YTrans", ytranS.getValue().toString()); //$NON-NLS-1$
		if( mapPoint!=null ){
			prop.setProperty("Control", controlL.getText()); //$NON-NLS-1$
			prop.setProperty("XMap", Integer.toString(mapPoint.x)); //$NON-NLS-1$
			prop.setProperty("YMap", Integer.toString(mapPoint.y)); //$NON-NLS-1$
		}
		prop.setProperty("XFactor", xfactorS.getValue().toString()); //$NON-NLS-1$
		prop.setProperty("YFactor", yfactorS.getValue().toString()); //$NON-NLS-1$
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(baseDir + GecoResources.sep + "live.prop")); //$NON-NLS-1$
			prop.store(writer, "Geco LiveMap"); //$NON-NLS-1$
		} catch (IOException e) {
			showFileErrorDialog(e);
		}
	}

	public void refreshMapfileName() {
		mapfileL.setText(mapFile.getName());
	}

	public void refreshCourseNames() {
		coursefileL.setText(courseFile.getName());
		showCourseCB.setModel(new DefaultComboBoxModel(liveComponent.coursenames()));
	}

	public void showFileErrorDialog(IOException e) {
		JOptionPane.showMessageDialog(
				null,
				Messages.liveGet("LiveConfigPanel.FileErrorMessage") + e.getLocalizedMessage(), //$NON-NLS-1$
				Messages.liveGet("LiveConfigPanel.Error"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE);
	}
	
}
