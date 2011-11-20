/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

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
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import net.geco.basics.Announcer.StageConfigListener;
import net.geco.control.AResultExporter;
import net.geco.control.ResultBuilder;
import net.geco.control.ResultBuilder.ResultConfig;
import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.model.ResultType;
import net.geco.model.Stage;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.framework.TabPanel;


/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class ResultsPanel extends TabPanel implements StageConfigListener {
	
	private static final int AutoexportDelay = 60;
	
	private List<String> coursenames;
	private List<String> categorynames;
	private JTextPane resultTA;
	
	private JComboBox resultTypeCB;
	private JRadioButton rankingResultRB;
	private JRadioButton splitResultRB;
	private JRadioButton cnScoreRB;

	private JButton refreshB;
	private JButton exportB;

	private JCheckBox showNcC;
	private JCheckBox showOtC;
	private JCheckBox showEsC;
	private JCheckBox showPeC;

	private String exportFormat;
	private JFileChooser filePane;

	private JButton selectAllB;
	private JButton selectNoneB;
	private JList poolList;
	
	private Thread autoexportThread;
	private JButton autoexportB;
	private JSpinner autodelayS;
	private JRadioButton refreshRB;

	@Override
	public String getTabTitle() {
		return Messages.uiGet("ResultsPanel.Title"); //$NON-NLS-1$
	}

	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public ResultsPanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		updateNames();
		initResultsPanel(this);
		initFileDialog();
		createListeners();
		geco().announcer().registerStageConfigListener(this);
	}

	private void updateNames() {
		coursenames = registry().getSortedCourseNames();
		categorynames = registry().getSortedCategoryNames();
	}
	private void updateCourseList() {
		poolList.setModel(new AbstractListModel() {
			public int getSize() {
				return coursenames.size();
			}
			public Object getElementAt(int index) {
				return coursenames.get(index);
			}
		});
		selectAllPools();
	}
	private void updateCategoryList() {
		poolList.setModel(new AbstractListModel() {
			public int getSize() {
				return categorynames.size();
			}
			public Object getElementAt(int index) {
				return categorynames.get(index);
			}
		});
		selectAllPools();
	}
	private void selectAllPools() {
		poolList.setSelectionInterval(0, poolList.getModel().getSize() - 1);
	}
	private void selectNoPool() {
		poolList.clearSelection();
	}
	private ResultType getResultType() {
		return (ResultType) resultTypeCB.getSelectedItem();
	}
	private boolean showCourses() {
		return getResultType() == ResultType.CourseResult;
	}
	private ResultConfig createResultConfig() {
		return ResultBuilder.createResultConfig(
				poolList.getSelectedValues(), 
				getResultType(),
				showEsC.isSelected(),
				showNcC.isSelected(),
				showOtC.isSelected(),
				showPeC.isSelected());
	}

	public AResultExporter resultExporter() {
		if( rankingResultRB.isSelected() ) {
			return geco().resultExporter();
		} else
		if( splitResultRB.isSelected() ) {
			return geco().splitsExporter();
		} else {
			return geco().cnCalculator();
		}
	}

	public void createListeners() {
		resultTypeCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch (getResultType()) {
				case CourseResult:
					updateCourseList();
					break;
				case CategoryResult:
				case MixedResult:
					updateCategoryList();
					break;
				}
			}
		});
		selectAllB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectAllPools();
			}
		});
		selectNoneB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectNoPool();
			}
		});
		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshResultView();
			}
		});
		exportB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String resultFile;
				if( rankingResultRB.isSelected() ) {
					resultFile = geco().getCurrentStagePath()
								+ File.separator
								+ Messages.uiGet("ResultsPanel.RankingFilename"); //$NON-NLS-1$
				} else {
					resultFile = geco().getCurrentStagePath()
								+ File.separator
								+ Messages.uiGet("ResultsPanel.SplitsFilename"); //$NON-NLS-1$
				}
				filePane.setSelectedFile(new File(resultFile).getAbsoluteFile());
				int response = filePane.showSaveDialog(frame());
				if( response==JFileChooser.APPROVE_OPTION ) {
					String filename = filePane.getSelectedFile().getAbsolutePath();
					try {
						resultExporter().exportFile(filename, exportFormat, createResultConfig(), -1);
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(
								frame(),
								Messages.uiGet("ResultsPanel.FileSaveWarning1")//$NON-NLS-1$ 
									+ filename
									+ "(" + ex +")", //$NON-NLS-1$ //$NON-NLS-2$
								Messages.uiGet("ResultsPanel.FileSaveWarning2"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
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

		// Commands: options and actions
		resultTypeCB = new JComboBox(ResultType.values());

		rankingResultRB = new JRadioButton(Messages.uiGet("ResultsPanel.RankingLabel")); //$NON-NLS-1$
		splitResultRB = new JRadioButton(Messages.uiGet("ResultsPanel.SplitsLabel")); //$NON-NLS-1$
		cnScoreRB = new JRadioButton(Messages.uiGet("ResultsPanel.CNLabel")); //$NON-NLS-1$
		ButtonGroup builderGroup = new ButtonGroup();
		builderGroup.add(rankingResultRB);
		builderGroup.add(splitResultRB);
		builderGroup.add(cnScoreRB);
		builderGroup.setSelected(rankingResultRB.getModel(), true);
		
		showNcC = new JCheckBox(Messages.uiGet("ResultsPanel.ShowNCLabel")); //$NON-NLS-1$
		showNcC.setToolTipText(Messages.uiGet("ResultsPanel.ShowNCTooltip")); //$NON-NLS-1$
		showOtC = new JCheckBox(Messages.uiGet("ResultsPanel.ShowOthersLabel")); //$NON-NLS-1$
		showOtC.setToolTipText(Messages.uiGet("ResultsPanel.ShowOthersTooltip")); //$NON-NLS-1$
		showPeC = new JCheckBox(Messages.uiGet("ResultsPanel.ShowPenaltiesLabel")); //$NON-NLS-1$
		showPeC.setToolTipText(Messages.uiGet("ResultsPanel.ShowPenaltiesTooltip")); //$NON-NLS-1$
		showEsC = new JCheckBox(Messages.uiGet("ResultsPanel.ShowESLabel")); //$NON-NLS-1$
		showEsC.setToolTipText(Messages.uiGet("ResultsPanel.ShowESTooltip")); //$NON-NLS-1$
		JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
		optionsPanel.add(showNcC);
		optionsPanel.add(showOtC);
		optionsPanel.add(showPeC);
		optionsPanel.add(showEsC);
		
		refreshB = new JButton(Messages.uiGet("ResultsPanel.RefreshLabel")); //$NON-NLS-1$
		exportB = new JButton(Messages.uiGet("ResultsPanel.ExportLabel")); //$NON-NLS-1$
		JButton printB = new JButton(Messages.uiGet("ResultsPanel.PrintLabel")); //$NON-NLS-1$
		
		printB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					refreshResultView();
					resultTA.print();
				} catch (PrinterException e1) {
					JOptionPane.showMessageDialog(
							frame(),
							Messages.uiGet("ResultsPanel.PrintWarning1"), //$NON-NLS-1$
							Messages.uiGet("ResultsPanel.PrintWarning2"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// Layout of Command panel
		JPanel commandPanel = new JPanel();
		commandPanel.setBorder(
				BorderFactory.createTitledBorder(Messages.uiGet("ResultsPanel.CommandTitle"))); //$NON-NLS-1$
		commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
		commandPanel.add(SwingUtils.embed(resultTypeCB));
		commandPanel.add(SwingUtils.makeButtonBar(FlowLayout.CENTER, rankingResultRB, splitResultRB, cnScoreRB));
		commandPanel.add(Box.createVerticalStrut(10));
		commandPanel.add(optionsPanel);
		commandPanel.add(Box.createVerticalStrut(10));
		commandPanel.add(SwingUtils.makeButtonBar(FlowLayout.CENTER, refreshB, exportB, printB));

		
		// Pool list
		selectAllB = new JButton(Messages.uiGet("ResultsPanel.SelectAllLabel")); //$NON-NLS-1$
		selectNoneB = new JButton(Messages.uiGet("ResultsPanel.SelectNoneLabel")); //$NON-NLS-1$
		JPanel listButtonsPanel = new JPanel();
		listButtonsPanel.setLayout(new BoxLayout(listButtonsPanel, BoxLayout.Y_AXIS));
		listButtonsPanel.add(SwingUtils.embed(selectAllB));
		listButtonsPanel.add(SwingUtils.embed(selectNoneB));

		poolList = new JList(coursenames.toArray());
		selectAllPools();
		JScrollPane scrollPane = new JScrollPane(poolList);
		scrollPane.setPreferredSize(new Dimension(150, 250));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(SwingUtils.embed(scrollPane), BorderLayout.CENTER);
		listPanel.add(SwingUtils.embed(listButtonsPanel), BorderLayout.EAST);

		
		// Automode
		JPanel autoPanel = new JPanel(new GridLayout(0, 2));
		autoPanel.setBorder(
				BorderFactory.createTitledBorder(Messages.uiGet("ResultsPanel.AutomodeTitle"))); //$NON-NLS-1$

		ButtonGroup autoGroup = new ButtonGroup();
		refreshRB = new JRadioButton(Messages.uiGet("ResultsPanel.RefreshLabel")); //$NON-NLS-1$
		JRadioButton exportRB = new JRadioButton(Messages.uiGet("ResultsPanel.ExportLabel")); //$NON-NLS-1$
		autoGroup.add(refreshRB);
		autoGroup.add(exportRB);
		refreshRB.setSelected(true);
		autoPanel.add(SwingUtils.embed(refreshRB));
		autoPanel.add(SwingUtils.embed(exportRB));

		autoexportB = new JButton(Messages.uiGet("ResultsPanel.AutoLabel")); //$NON-NLS-1$
		autodelayS = new JSpinner(new SpinnerNumberModel(AutoexportDelay, 1, null, 10));
		autodelayS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		autodelayS.setToolTipText(Messages.uiGet("ResultsPanel.AutoTooltip")); //$NON-NLS-1$
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
		resultTA.setContentType("text/html"); //$NON-NLS-1$
		resultTA.setEditable(false);
		return resultTA;
	}

	public void initFileDialog() {
		JPanel fileFormatRB = new JPanel();
		fileFormatRB.setLayout(new BoxLayout(fileFormatRB, BoxLayout.Y_AXIS));
		fileFormatRB.setBorder(
			BorderFactory.createTitledBorder(Messages.uiGet("ResultsPanel.FileFormatTitle"))); //$NON-NLS-1$
		JRadioButton selectHtmlB = new JRadioButton("HTML"); //$NON-NLS-1$
		selectHtmlB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportFormat = "html"; //$NON-NLS-1$
			}
		});
		JRadioButton selectCsvB = new JRadioButton("CSV"); //$NON-NLS-1$
		selectCsvB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportFormat = "csv"; //$NON-NLS-1$
			}
		});
		JRadioButton selectOECsvB = new JRadioButton("OE CSV"); //$NON-NLS-1$
		selectOECsvB.setToolTipText(Messages.uiGet("ResultsPanel.OECSVTooltip")); //$NON-NLS-1$
		selectOECsvB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportFormat = "oe.csv"; //$NON-NLS-1$
			}
		});
		JRadioButton selectXmlB = new JRadioButton("XML"); //$NON-NLS-1$
		selectXmlB.setToolTipText("XML with IOF standards (for RouteGadget etc.)");
		selectXmlB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportFormat = "xml"; //$NON-NLS-1$
			}
		});
		ButtonGroup group = new ButtonGroup();
		group.add(selectHtmlB);
		group.add(selectCsvB);
		group.add(selectOECsvB);
		group.add(selectXmlB);
		group.setSelected(selectHtmlB.getModel(), true);
		exportFormat = "html"; //$NON-NLS-1$
		fileFormatRB.add(selectHtmlB);
		fileFormatRB.add(selectCsvB);
		fileFormatRB.add(selectOECsvB);
		fileFormatRB.add(selectXmlB);
		
		filePane = new JFileChooser();
		filePane.setAccessory(fileFormatRB);
	}


	public void refreshResultView() {
		String htmlResults = resultExporter().generateHtmlResults(createResultConfig(), -1, false);
		resultTA.setText(htmlResults);
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
			String resultFile = geco().getCurrentStagePath()
								+ File.separator
								+ Messages.uiGet("ResultsPanel.LastresultsLabel"); //$NON-NLS-1$
			try {
				try {
					resultExporter().exportFile(resultFile, exportFormat, createResultConfig(), refreshDelay);
				} catch (Exception ex) {
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

	private void refresh() {
		updateNames();
		if( showCourses() ) {
			updateCourseList();
		} else {
			updateCategoryList();
		}
		repaint();
	}

	@Override
	public void changed(Stage previous, Stage next) {
		resultTA.setText(""); //$NON-NLS-1$
		refresh();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		File selectedFile = filePane.getSelectedFile();
		if( selectedFile!=null ){
			properties.setProperty("LastResultFile", selectedFile.getName()); //$NON-NLS-1$
		}
	}

	
	@Override
	public void categoriesChanged() {
		refresh();
	}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		refresh();
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		refreshB.requestFocusInWindow();
	}


}
