/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;

import valmo.geco.Geco;
import valmo.geco.control.IResultBuilder;
import valmo.geco.control.ResultBuilder;
import valmo.geco.control.ResultBuilder.ResultConfig;
import valmo.geco.core.Announcer.StageConfigListener;
import valmo.geco.model.ResultType;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class ResultsPanel extends TabPanel implements StageConfigListener {
	
	private static final int AutoexportDelay = 60;
	
	private Vector<String> coursenames;
	private Vector<String> categorynames;
	private JTextPane resultTA;
	
	private JRadioButton selectCourseB;
	private JRadioButton selectCatB;
	private JRadioButton selectMixedB;
	private JButton refreshB;
	private JButton exportB;
	private JCheckBox showNcC;
	private JCheckBox showOtC;
	private JCheckBox showEsC;
	private JCheckBox showPeC;

	private JRadioButton normalResultB;
	private JRadioButton splitResultB;

	private String exportFormat;
	private JFileChooser filePane;

	private JButton selectAllB;
	private JButton selectNoneB;
	private JList list;
	
	private Thread autoexportThread;
	private JButton autoexportB;
	private JSpinner autodelayS;
	private JRadioButton refreshRB;

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
	
	
	public IResultBuilder resultBuilder() {
		if( normalResultB.isSelected() ) {
			return geco().resultBuilder();
		} else {
			return geco().splitsBuilder();
		}
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
		selectMixedB.addActionListener(new ActionListener() {
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
						resultBuilder().exportFile(filename, exportFormat, createResultConfig(), -1);
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
					autodelayS.setEnabled(true);
					stopAutoexport();
				} else {
					autoexportB.setSelected(true);
					defaultColor = autoexportB.getBackground();
					autoexportB.setBackground(Color.GREEN);
					autodelayS.setEnabled(false);
					startAutoexport();
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
		// Commands: options and buttons
		JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
		
		selectCourseB = new JRadioButton(ResultType.CourseResult.toString());
		selectCatB = new JRadioButton(ResultType.CategoryResult.toString());
		selectMixedB = new JRadioButton(ResultType.MixedResult.toString());
		ButtonGroup poolGroup = new ButtonGroup();
		poolGroup.add(selectCourseB);
		poolGroup.add(selectCatB);
		poolGroup.add(selectMixedB);
		poolGroup.setSelected(selectCourseB.getModel(), true);
		optionsPanel.add(selectCatB);
		optionsPanel.add(selectCourseB);
		optionsPanel.add(selectMixedB);
		optionsPanel.add(Box.createHorizontalGlue());
		optionsPanel.add(Box.createHorizontalGlue());
		optionsPanel.add(Box.createHorizontalGlue());
		
		showNcC = new JCheckBox("Show NC");
		showOtC = new JCheckBox("Show Others");
		showPeC = new JCheckBox("Show penalties");
		showEsC = new JCheckBox("Show empty sets");
		optionsPanel.add(showNcC);
		optionsPanel.add(showOtC);
		optionsPanel.add(showPeC);
		optionsPanel.add(showEsC);
		optionsPanel.add(Box.createHorizontalGlue());
		optionsPanel.add(Box.createHorizontalGlue());

		normalResultB = new JRadioButton("Normal");
		splitResultB = new JRadioButton("Splits");
		ButtonGroup builderGroup = new ButtonGroup();
		builderGroup.add(normalResultB);
		builderGroup.add(splitResultB);
		builderGroup.setSelected(normalResultB.getModel(), true);
		
		refreshB = new JButton("Refresh");
		exportB = new JButton("Export");
		JButton printB = new JButton("Print");
		
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
		
		JPanel commandPanel = new JPanel();
		commandPanel.setBorder(
				BorderFactory.createTitledBorder("Commands"));
		commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
		commandPanel.add(optionsPanel);
		commandPanel.add(SwingUtils.makeButtonBar(FlowLayout.CENTER, refreshB, exportB, printB));
		commandPanel.add(SwingUtils.makeButtonBar(FlowLayout.CENTER, normalResultB, splitResultB));

		
		// Pool list
		selectAllB = new JButton("All");
		selectNoneB = new JButton("None");
		JPanel listButtonsPanel = new JPanel();
		listButtonsPanel.setLayout(new BoxLayout(listButtonsPanel, BoxLayout.Y_AXIS));
		listButtonsPanel.add(SwingUtils.embed(selectAllB));
		listButtonsPanel.add(SwingUtils.embed(selectNoneB));

		list = new JList(coursenames);
		selectAllCourses(list);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(150, 250));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(SwingUtils.embed(scrollPane), BorderLayout.CENTER);
		listPanel.add(SwingUtils.embed(listButtonsPanel), BorderLayout.EAST);

		
		// Automode
		JPanel autoPanel = new JPanel(new GridLayout(0, 2));
		autoPanel.setBorder(BorderFactory.createTitledBorder("Automode"));

		ButtonGroup autoGroup = new ButtonGroup();
		refreshRB = new JRadioButton("Refresh");
		JRadioButton exportRB = new JRadioButton("Export");
		autoGroup.add(refreshRB);
		autoGroup.add(exportRB);
		refreshRB.setSelected(true);
		autoPanel.add(SwingUtils.embed(refreshRB));
		autoPanel.add(SwingUtils.embed(exportRB));

		autoexportB = new JButton("Auto");
		autodelayS = new JSpinner(new SpinnerNumberModel(AutoexportDelay, 1, null, 10));
		autodelayS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		autodelayS.setToolTipText("Auto delay in seconds");
		autoPanel.add(SwingUtils.embed(autoexportB));
		autoPanel.add(SwingUtils.embed(autodelayS));

		
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.add(commandPanel, BorderLayout.NORTH);
		selectionPanel.add(listPanel, BorderLayout.CENTER);
		selectionPanel.add(autoPanel, BorderLayout.SOUTH);
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
		String htmlResults = resultBuilder().generateHtmlResults(createResultConfig(), -1);
		resultTA.setText(htmlResults);
	}
	
	private ResultConfig createResultConfig() {
		return ResultBuilder.createResultConfig(
				list.getSelectedValues(), 
				getResultType(),
				showEsC.isSelected(),
				showNcC.isSelected(),
				showOtC.isSelected(),
				showPeC.isSelected());
	}
	
	private ResultType getResultType() {
		if( selectCourseB.isSelected() ) {
			return ResultType.CourseResult;
		}
		if( selectCatB.isSelected() ) {
			return ResultType.CategoryResult;
		}
		if( selectMixedB.isSelected() ) {
			return ResultType.MixedResult;
		}
		return null;
	}

	
	public Thread startAutoexport() {
		autoexportThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				int autoexportDelay = ((Integer) autodelayS.getValue()).intValue();
				if( refreshRB.isSelected() ) {
					autorefresh(autoexportDelay);
				} else {
					autoexport(autoexportDelay);
				}
			}});
		autoexportThread.start();
		return autoexportThread;
	}
	private synchronized void autorefresh(long autoexportDelay) {
		long delay = 1000 * autoexportDelay;
		while( true ){
			try {
				refreshResultView();
				wait(delay);
			} catch (InterruptedException e) {
				return;
			}					
		}	
	}
	private synchronized void autoexport(int refreshDelay) {
		long delay = 1000 * refreshDelay;
		while( true ){
			String resultFile = geco().getCurrentStagePath() + File.separator + "lastresults";
			try {
				try {
					resultBuilder().exportFile(resultFile, exportFormat, createResultConfig(), refreshDelay);
				} catch (IOException ex) {
					geco().logger().debug(ex);
				}
				wait(delay);
			} catch (InterruptedException e) {
				return;
			}					
		}	
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
	
	@Override
	public void componentShown(ComponentEvent e) {
		refreshB.requestFocusInWindow();
	}


}
