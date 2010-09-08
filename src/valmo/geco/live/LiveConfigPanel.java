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

import valmo.geco.ui.SwingUtils;

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
	
//	private JTextField portF;
//	private JButton listenB;


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
		mapfileB = new JButton("Map image...");
		mapfileL = new JLabel();
		coursefileB = new JButton("Course file...");
		coursefileL = new JLabel();
		// map config
		dpiS = new JSpinner(new SpinnerNumberModel(150, 0, null, 50));
		dpiS.setPreferredSize(new Dimension(75, 20));
		xfactorS = new JSpinner(new SpinnerNumberModel(150 / 25.4f, 1f, null, 0.1f));
		xfactorS.setPreferredSize(new Dimension(75, 20));
		yfactorS = new JSpinner(new SpinnerNumberModel(150 / 25.4f, 1f, null, 0.1f));
		yfactorS.setPreferredSize(new Dimension(75, 20));
		xtranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		xtranS.setPreferredSize(new Dimension(75, 20));
		ytranS = new JSpinner(new SpinnerNumberModel(0, null, null, 1));
		ytranS.setPreferredSize(new Dimension(75, 20));
		refreshB = new JButton("Refresh");
		// course config
		showControlsB = new JButton("Show controls");
		showMapB = new JButton("Show map");
		showCourseCB = new JComboBox();
		// network config
//		if( withLiveNetwork ) {
//			DecimalFormat format = new DecimalFormat();
//			format.setGroupingUsed(false);
//			portF = new JFormattedTextField(format);
//			portF.setText("4444");
//			portF.setColumns(5);
//			listenB = new JButton("Listen");
//		}
	}
	
	private void initListeners() {
		mapfileB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
				fileChooser.setDialogTitle("Select image file for map");
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
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
				fileChooser.setDialogTitle("Select xml file with course descriptions");
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
//		if( listenB!=null ) { // live network enabled
//			listenB.addActionListener(new ActionListener() {
//				private Color defaultColor;
//				LiveServer server;
//				public void actionPerformed(ActionEvent e) {
//					if( listenB.isSelected() ) {
//						listenB.setSelected(false);
//						listenB.setBackground(defaultColor);
//						server.interrupt();
//					} else {
//						try {
//							// TODO gecoControl parameter
//							server = new LiveServer(null, Integer.parseInt(portF.getText())).accept();
//							listenB.setSelected(true);
//							defaultColor = listenB.getBackground();
//							listenB.setBackground(Color.GREEN);
//						} catch (NumberFormatException e1) {
//							e1.printStackTrace();
//						} catch (IOException e1) {
//							e1.printStackTrace();
//						}
//					}
//				}
//			});
//		}
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
		datafileP.setBorder(BorderFactory.createTitledBorder("1. Load Data"));
		addComponent(datafileP, mapfileB);
		addComponent(datafileP, mapfileL);
		addComponent(datafileP, coursefileB);
		addComponent(datafileP, coursefileL);
				
		JPanel mapConfigP = new JPanel(new GridLayout(0, 2));
		mapConfigP.setBorder(BorderFactory.createTitledBorder("2. Setup Map Parameters"));
		addComponent(mapConfigP, new JLabel("Image DPI:"));
		addComponent(mapConfigP, dpiS);
		addComponent(mapConfigP, new JLabel("X factor"));
		addComponent(mapConfigP, new JLabel("Y factor"));
		addComponent(mapConfigP, xfactorS);
		addComponent(mapConfigP, yfactorS);
		addComponent(mapConfigP, new JLabel("X translation"));
		addComponent(mapConfigP, new JLabel("Y translation"));
		addComponent(mapConfigP, xtranS);
		addComponent(mapConfigP, ytranS);
		addComponent(mapConfigP, refreshB);
		
		JPanel courseConfigP = new JPanel(new GridLayout(0, 2));
		courseConfigP.setBorder(BorderFactory.createTitledBorder("3. Check Courses"));		
		addComponent(courseConfigP, showControlsB);
		addComponent(courseConfigP, showMapB);
		addComponent(courseConfigP, new JLabel("Show course:"));
		addComponent(courseConfigP, showCourseCB);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(datafileP);
		add(mapConfigP);
		add(courseConfigP);
		
//		if( listenB!=null ) { // live network enabled
//			JPanel networkConfigP = new JPanel(new GridLayout(0, 3));
//			networkConfigP.setBorder(BorderFactory.createTitledBorder("4. Setup Live Server"));
//			addComponent(networkConfigP, listenB);
//			addComponent(networkConfigP, new JLabel("Port:"));
//			addComponent(networkConfigP, portF);
//			add(networkConfigP);
//		}
		return this;
	}
	
	public void addComponent(Container cont, Component comp) {
		cont.add(SwingUtils.embed(comp));
	}

	
}
