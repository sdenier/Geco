/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
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
import javax.swing.JTextPane;

import valmo.geco.control.TimeManager;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Result.RankedRunner;
import valmo.geco.model.RunnerRaceData.RunnerResult;
import valmo.geco.model.RunnerRaceData.Status;

/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public abstract class AbstractResultsPanel extends GecoPanel {
	
	// cache field
	private Vector<String> coursenames;
	private Vector<String> categorynames;
	
//	private String exportFormat;
//	
//	private JList list;
//	private JTextPane resultTA;
//	private JRadioButton selectCourseB;
//	private JRadioButton selectCatB;
//	private JButton refreshB;
//	private JButton exportB;
//	private JButton selectAllB;
//	private JButton selectNoneB;
//	private JCheckBox showNcC;
//	private JCheckBox showOtC;
//	private JCheckBox showEsC;
//	private JFileChooser filePane;

	/**
	 * @param geco
	 * @param frame 
	 */
	public AbstractResultsPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		updateNames();
//		initResultsPanel(this);
//		initFileDialog();
//		createListeners();
//		geco().eventRegistry().registerStageListener(this);
	}

	private void updateNames() {
		coursenames = registry().getCoursenames();
		Collections.sort(coursenames);
		categorynames = registry().getCategorynames();
		Collections.sort(categorynames);
	}

	//	public void initFileDialog() {
//		JPanel fileFormatRB = new JPanel();
//		fileFormatRB.setLayout(new BoxLayout(fileFormatRB, BoxLayout.Y_AXIS));
//		fileFormatRB.setBorder(BorderFactory.createTitledBorder("Format"));
//		JRadioButton selectHtmlB = new JRadioButton("HTML");
//		selectHtmlB.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				exportFormat = "html";
//			}
//		});
//		JRadioButton selectCsvB = new JRadioButton("CSV");
//		selectCsvB.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				exportFormat = "csv";
//			}
//		});
//		ButtonGroup group = new ButtonGroup();
//		group.add(selectHtmlB);
//		group.add(selectCsvB);
//		group.setSelected(selectHtmlB.getModel(), true);
//		exportFormat = "html";
//		fileFormatRB.add(selectHtmlB);
//		fileFormatRB.add(selectCsvB);
//		
//		filePane = new JFileChooser();
//		filePane.setAccessory(fileFormatRB);
//	}

//	private boolean showCourses() {
//		return selectCourseB.isSelected();
//	}

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
	
	private Vector<Result> refreshResults(Object[] selectedValues) {
		// TODO: local cache for result (invalidate each time one focus on the result pane)
		Vector<Result> results = new Vector<Result>();
		for (Object selName : selectedValues) {
			if( showCourses() ) {
				results.add(
						geco().resultBuilder().buildResultForCourse(registry().findCourse((String) selName))
						);
			} else {
				results.add(
						geco().resultBuilder().buildResultForCategory(registry().findCategory((String) selName))
						);
			}
		}
		return results;
	}

//	public void refreshResultView() {
//		resultTA.setText(generateHtmlResults());
//	}

//	public void exportFile(String filename, String format) {
//		if( !filename.endsWith(format) ) {
//			filename = filename + "." + format;
//		}
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
//			if( format.equals("html") ) {
//				writer.write(generateHtmlResults()); // TODO: insert newline for readability	
//			}
//			if( format.equals("csv") ) {
//				generateCsvResult(writer);
//			}
//			writer.close();
//		} catch (IOException e) {
//			JOptionPane.showMessageDialog(frame(), "Error while saving " + filename + "(" + e +")",
//					"Export Error", JOptionPane.ERROR_MESSAGE);
//		}
//	}


//	private String generateHtmlResults() {
//		Vector<Result> results = refreshResults(list.getSelectedValues());
//		StringBuffer res = new StringBuffer("<html>");
//		for (Result result : results) {
//			if( showEsC.isSelected() || !result.isEmpty()) {
//				appendHtmlResult(result, res);	
//			}
//		}
//		res.append("</html>");
//		return res.toString();
//	}

	/**
	 * @param writer
	 * @throws IOException 
	 */
//	private void generateCsvResult(BufferedWriter writer) throws IOException {
//		Vector<Result> results = refreshResults(list.getSelectedValues());
//		for (Result result : results) {
//			if( showEsC.isSelected() || !result.isEmpty()) {
//				appendCsvResult(result, writer);
//			}
//		}
//	}


}
