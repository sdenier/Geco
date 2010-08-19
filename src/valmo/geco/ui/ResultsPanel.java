/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import valmo.geco.control.ResultBuilder;
import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Geco;
import valmo.geco.core.Announcer.StageConfigListener;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class ResultsPanel extends TabPanel implements StageConfigListener {
	
	// cache field
	private Vector<String> coursenames;
	private Vector<String> categorynames;
	
	private String exportFormat;
	
	private JList list;
	private JTextPane resultTA;
	private JRadioButton selectCourseB;
	private JRadioButton selectCatB;
	private JButton refreshB;
	private JButton exportB;
	private JButton selectAllB;
	private JButton selectNoneB;
	private JCheckBox showNcC;
	private JCheckBox showOtC;
	private JCheckBox showEsC;
	private JCheckBox showPeC;
	private JFileChooser filePane;
	
	private Thread autoexportThread;
	private int autoexportDelay = 60;
	private JButton autoexportB;
	private JTextField autodelayF;

	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public ResultsPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		updateNames();
		initResultsPanel(this);
		initFileDialog();
		createListeners();
		geco().announcer().registerStageConfigListener(this);
	}

	private void updateNames() {
		coursenames = registry().getSortedCoursenames();
		categorynames = registry().getSortedCategorynames();
	}
	private void updateCourseList() {
		list.setModel(new AbstractListModel() {
			public int getSize() {
				return nbCourses();
			}
			public Object getElementAt(int index) {
				return coursenames.get(index);
			}
		});
		selectAllCourses(list);
	}
	private void updateCategoryList() {
		list.setModel(new AbstractListModel() {
			public int getSize() {
				return nbCategories();
			}
			public Object getElementAt(int index) {
				return categorynames.get(index);
			}
		});
		selectAllCategories(list);
	}

	/**
	 * 
	 */
	public void createListeners() {
		selectCourseB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCourseList();
			}
		});
		selectCatB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCategoryList();
			}
		});
		selectAllB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( showCourses() ) {
					selectAllCourses(list);
				} else {
					selectAllCategories(list);
				}
			}
		});
		selectNoneB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNone(list);
			}
		});
		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshResultView();
			}
		});
		exportB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String resultFile = geco().getCurrentStagePath() + File.separator + "ranking";
				filePane.setSelectedFile(new File(resultFile).getAbsoluteFile());
				int response = filePane.showSaveDialog(frame());
				if( response==JFileChooser.APPROVE_OPTION ) {
					String filename = filePane.getSelectedFile().getAbsolutePath();
					try {
						geco().resultBuilder().exportFile(filename, exportFormat, createResultConfig());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(frame(), "Error while saving " + filename + "(" + ex +")",
								"Export Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		autoexportB.addActionListener(new ActionListener() {
			private Color defaultColor;
			@Override
			public void actionPerformed(ActionEvent e) {
				if( autoexportB.isSelected() ) {
					autoexportB.setSelected(false);
					autoexportB.setBackground(defaultColor);
					autodelayF.setEnabled(true);
					stopAutoexport();
				} else {
					autoexportB.setSelected(true);
					defaultColor = autoexportB.getBackground();
					autoexportB.setBackground(Color.GREEN);
					autodelayF.setEnabled(false);
					startAutoexport();
				}
			}
		});
		autodelayF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					autoexportDelay = Integer.parseInt(autodelayF.getText());
				} catch (NumberFormatException e1) {
					geco().info("Bad number format", true);
					autodelayF.setText(Integer.toString(autoexportDelay));
				}
			}
		});
		autodelayF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				try {
					Integer.parseInt(autodelayF.getText());
					return true;
				} catch (NumberFormatException e1) {
					return false;
				}
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				try {
					autoexportDelay = Integer.parseInt(autodelayF.getText());
					return true;
				} catch (NumberFormatException e1) {
					geco().info("Bad number format", true);
					autodelayF.setText(Integer.toString(autoexportDelay));
					return false;
				}
			}
		});
	}

	/**
	 * @param panel
	 */
	public void initResultsPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		JPanel resultSelectionPanel = initSelectionPanel();
		JTextPane resultTA = initResultPanel();
		JScrollPane scrollPane = new JScrollPane(resultTA);
		panel.add(resultSelectionPanel, BorderLayout.WEST);
		panel.add(scrollPane, BorderLayout.CENTER);
	}

	private JPanel initSelectionPanel() {
		JPanel commandPanel = new JPanel();
		commandPanel.setLayout(new GridLayout(0, 2));

		selectCourseB = new JRadioButton("Courses");
		selectCatB = new JRadioButton("Categories");
		ButtonGroup group = new ButtonGroup();
		group.add(selectCourseB);
		group.add(selectCatB);
		group.setSelected(selectCourseB.getModel(), true);
		commandPanel.add(selectCourseB);
		commandPanel.add(selectCatB);
		
		showNcC = new JCheckBox("Show NC");
		showOtC = new JCheckBox("Show Others");
		showPeC = new JCheckBox("Show penalties");
		showEsC = new JCheckBox("Show empty sets");
		commandPanel.add(showNcC);
		commandPanel.add(showOtC);
		commandPanel.add(showPeC);
		commandPanel.add(showEsC);
		
		refreshB = new JButton("Refresh");
		refreshB.requestFocusInWindow();
		exportB = new JButton("Export");
		JButton printB = new JButton("Print");
		commandPanel.add(SwingUtils.embed(refreshB));
		commandPanel.add(Box.createHorizontalGlue());
		commandPanel.add(SwingUtils.embed(exportB));
		commandPanel.add(SwingUtils.embed(printB));
		
		printB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					refreshResultView();
					resultTA.print();
				} catch (PrinterException e1) {
					JOptionPane.showMessageDialog(frame(), "Fail to print", "Printing Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		autoexportB = new JButton("Autoexport");
		autodelayF = new JTextField(5);
		autodelayF.setText(Integer.toString(autoexportDelay));
		autodelayF.setToolTipText("Autoexport delay in seconds");
		commandPanel.add(SwingUtils.embed(autoexportB));
		commandPanel.add(SwingUtils.embed(autodelayF));
		
		selectAllB = new JButton("All");
		selectNoneB = new JButton("None");
		commandPanel.add(SwingUtils.embed(selectAllB));
		commandPanel.add(SwingUtils.embed(selectNoneB));
		
		commandPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.add(SwingUtils.embed(commandPanel), BorderLayout.NORTH);
	
		list = new JList(coursenames);
		selectAllCourses(list);
		JScrollPane scrollPane = new JScrollPane(list);
		JPanel embed = SwingUtils.embed(scrollPane);
		scrollPane.setPreferredSize(new Dimension(150, 300));
		selectionPanel.add(embed, BorderLayout.CENTER);

		return selectionPanel;
	}
	
	private JTextPane initResultPanel() {
		resultTA = new JTextPane();
		resultTA.setContentType("text/html");
		resultTA.setEditable(false);
		return resultTA;
	}

	public void initFileDialog() {
		JPanel fileFormatRB = new JPanel();
		fileFormatRB.setLayout(new BoxLayout(fileFormatRB, BoxLayout.Y_AXIS));
		fileFormatRB.setBorder(BorderFactory.createTitledBorder("Format"));
		JRadioButton selectHtmlB = new JRadioButton("HTML");
		selectHtmlB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportFormat = "html";
			}
		});
		JRadioButton selectCsvB = new JRadioButton("CSV");
		selectCsvB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportFormat = "csv";
			}
		});
		ButtonGroup group = new ButtonGroup();
		group.add(selectHtmlB);
		group.add(selectCsvB);
		group.setSelected(selectHtmlB.getModel(), true);
		exportFormat = "html";
		fileFormatRB.add(selectHtmlB);
		fileFormatRB.add(selectCsvB);
		
		filePane = new JFileChooser();
		filePane.setAccessory(fileFormatRB);
	}

	private boolean showCourses() {
		return selectCourseB.isSelected();
	}

	private void selectAllCourses(JList list) {
		list.setSelectionInterval(0, nbCourses() -1);
	}

	private int nbCourses() {
		return coursenames.size();
	}

	private void selectAllCategories(JList list) {
		list.setSelectionInterval(0, nbCategories() -1);
	}

	private int nbCategories() {
		return categorynames.size();
	}
	
	private void selectNone(JList list) {
		list.clearSelection();
	}

	public void refreshResultView() {
		resultTA.setText(geco().resultBuilder().generateHtmlResults(createResultConfig()));
	}
	
	private ResultConfig createResultConfig() {
		return ResultBuilder.createResultConfig(
				list.getSelectedValues(), 
				showCourses(),
				showEsC.isSelected(),
				showNcC.isSelected(),
				showOtC.isSelected(),
				showPeC.isSelected());
	}

	
	public Thread startAutoexport() {
		autoexportThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while( true ){
					String resultFile = geco().getCurrentStagePath() + File.separator + "lastresults";
					try {
						try {
							geco().resultBuilder().exportFile(resultFile, exportFormat, createResultConfig());
						} catch (IOException ex) {
							geco().logger().debug(ex);
						}
						wait(autoexportDelay * 1000);
					} catch (InterruptedException e) {
						return;
					}					
				}
			}});
		autoexportThread.start();
		return autoexportThread;
	}
	
	public void stopAutoexport() {
		if( autoexportThread!=null ) {
			autoexportThread.interrupt();
		}
	}

	@Override
	public void changed(Stage previous, Stage next) {
		updateNames();
		if( showCourses() ) {
			updateCourseList();
		} else {
			updateCategoryList();
		}
		repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		File selectedFile = filePane.getSelectedFile();
		if( selectedFile!=null ){
			properties.setProperty("LastResultFile", selectedFile.getName());
		}
	}

	
	@Override
	public void categoriesChanged() {
		changed(null, null);
	}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		changed(null, null);
	}

}
