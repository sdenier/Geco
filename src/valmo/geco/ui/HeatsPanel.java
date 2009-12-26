/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.model.Heat;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.Stage;

/**
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class HeatsPanel extends TabPanel implements Announcer.StageConfigListener {
//	TODO: remove duplication between ResultsPanel and HeatsPanel

	private Vector<String> coursenames;	
	private Vector<String> categorynames;
	private DefaultListModel heatlistModel;
	private HeatSetDialog heatDialog;
	
	private JTextPane heatsTA;
	private JList heatList;
	private JList setList;
	private JButton newB;
	private JButton deleteB;
	private JButton refreshB;
	private JButton exportB;
	private JFileChooser filePane;
	private String exportFormat;

	
	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public HeatsPanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		heatlistModel = new DefaultListModel();
		heatDialog = new HeatSetDialog(frame);
		refresh();
		initHeatsPanel(this);
		initFileDialog();
		createListeners();
		announcer.registerStageConfigListener(this);
	}

	private void updateNames() {
		coursenames = registry().getCoursenames();
		Collections.sort(coursenames);
		categorynames = registry().getCategorynames();
		Collections.sort(categorynames);
	}

	public void showHeatSetCreationDialog() {
		heatDialog.showDialog();
		if( !heatDialog.cancelled() ) {
			HeatSet newHeatset = heatDialog.getHeatSet();
			heatlistModel.addElement(newHeatset);
			heatList.setSelectedValue(newHeatset, true);
			registry().addHeatSet(newHeatset);
		}
	}
	
	public void showHeatSetDialog() {
		Object selectedHeatset = heatList.getSelectedValue();
		if( selectedHeatset!=null ) {
			heatDialog.showHeatSet((HeatSet) selectedHeatset);
			// selection listener not triggered because heatset already selected
			showSetList(); // so directly refresh the setlist
		}
	}

	/**
	 * 
	 */
	public void createListeners() {
		newB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showHeatSetCreationDialog();
			}
		});
		deleteB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int remove = heatList.getSelectedIndex();
				if( remove!=-1 )
					registry().removeHeatset(getSelectedHeatset());
					heatlistModel.remove(remove);
			}
		});
		
		heatList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectionSize = heatList.getSelectedIndices().length;
				if( selectionSize==1 ) {
					showSetList();
					setList.setVisible(true);
				} else {
					setList.setVisible(false);
				}
			}
		});
		heatList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==2 ) {
					showHeatSetDialog();
				}
			}
		});
		setList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==1 ) {
					(getSelectedHeatset()).setSelectedSets(setList.getSelectedValues());
				}
			}
		});

		refreshB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshHeatView();
			}
		});
		exportB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				filePane.setCurrentDirectory(); // stage dir
				// default file name?
				int response = filePane.showSaveDialog(frame());
				if( response==JFileChooser.APPROVE_OPTION ) {
					exportFile(filePane.getSelectedFile().getAbsolutePath(), exportFormat);
				}
			}
		});
	}

	/**
	 * @param panel
	 */
	public void initHeatsPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		JPanel resultSelectionPanel = initBuilderPanel();
		JTextPane resultTA = initHeatViewPanel();
		JScrollPane scrollPane = new JScrollPane(resultTA);
		panel.add(resultSelectionPanel, BorderLayout.WEST);
		panel.add(scrollPane, BorderLayout.CENTER);
	}

	private JPanel initBuilderPanel() {
		JPanel heatPanel = new JPanel();
		heatPanel.setLayout(new BoxLayout(heatPanel, BoxLayout.X_AXIS));
		JPanel butPanel = new JPanel();
		butPanel.setLayout(new BoxLayout(butPanel, BoxLayout.Y_AXIS));
		newB = new JButton("New");
		butPanel.add(Util.embed(newB));
		deleteB = new JButton("Delete");
		butPanel.add(Util.embed(deleteB));
		heatPanel.add(butPanel);
		heatList = new JList(heatlistModel);
//		heatList.setVisibleRowCount(4);
		JScrollPane spane = new JScrollPane(heatList);
		spane.setPreferredSize(new Dimension(90, 90));
		heatPanel.add(spane);

		JPanel selectionPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2));
		refreshB = new JButton("Refresh");
		JButton printB = new JButton("Print");
		exportB = new JButton("Export");
		buttonPanel.add(Util.embed(refreshB));
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(Util.embed(printB));
		buttonPanel.add(Util.embed(exportB));
		selectionPanel.add(buttonPanel, BorderLayout.NORTH);

		printB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					refreshHeatView();
					heatsTA.print();
				} catch (PrinterException e1) {
					JOptionPane.showMessageDialog(frame(), "Fail to print", "Printing Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		setList = new JList();
		setList.setVisible(false);
		JScrollPane scrollPane = new JScrollPane(setList);
		scrollPane.setPreferredSize(new Dimension(150, 300));
		JPanel embed = Util.embed(scrollPane);
		selectionPanel.add(embed, BorderLayout.CENTER);
		
		JPanel builderPanel = new JPanel(new BorderLayout());
		builderPanel.add(Util.embed(heatPanel), BorderLayout.NORTH);
		builderPanel.add(selectionPanel, BorderLayout.CENTER);
		return builderPanel;
	}
	
	private JTextPane initHeatViewPanel() {
		heatsTA = new JTextPane();
		heatsTA.setContentType("text/html");
		heatsTA.setEditable(false);
		return heatsTA;
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

	private Vector<Heat> refreshHeats(Object[] selectedValues) {
		Vector<Heat> heats = new Vector<Heat>();
		for (Object selName : selectedValues) {
			HeatSet heatset = (HeatSet) selName;
			List<Result> heatsetResults = new Vector<Result>();
			Object[] selectedSets = heatset.getSelectedSets();
			if( heatset.isCourseType() ) {
				for (Object setname : selectedSets) {
					heatsetResults.add(geco().resultBuilder().buildResultForCourse(registry().findCourse((String) setname)));	
				}
			} else {
				for (Object setname : selectedSets) {
					heatsetResults.add(geco().resultBuilder().buildResultForCategory(registry().findCategory((String) setname)));	
				}				
			}
			List<Heat> heatsForCurrentHeatset = geco().heatBuilder().buildHeatsFromResults(heatsetResults, heatset.getHeatNames(), heatset.getQualifyingRank());
			heats.addAll(heatsForCurrentHeatset);
//			List<List<Runner>> heatsForCurrentHeatset = geco().heatBuilder().buildHeatsFromResults(heatsetResults, heatset.getQualifyingRank(), heatset.getNbHeats());
//			String[] heatNames = heatset.getHeatNames();
//			for (int i = 0; i < heatNames.length; i++) {
//				heats.add(new Heat(heatNames[i], heatsForCurrentHeatset.get(i)));				
//			}

		}
		return heats;
	}
	
	public HeatSet getSelectedHeatset() {
		return (HeatSet) heatList.getSelectedValue();
	}

	
	public void showSetList() {
		HeatSet currentHeatset = getSelectedHeatset();
		final Vector<String> setnames = getNameset(currentHeatset);
		setList.setModel(new AbstractListModel() {
			public int getSize() {
				return setnames.size();
			}
			public Object getElementAt(int index) {
				return setnames.get(index);
			}
		});
		setList.setSelectedIndices(getSelectedIndices(currentHeatset));
	}
	
	public Vector<String> getNameset(HeatSet heatset) {
		if( heatset.isCourseType() ) {
			return coursenames;
		} else {
			return categorynames;
		}
	}
	
	private int[] getSelectedIndices(HeatSet heatset) {
		int[] indices = new int[heatset.getSelectedSets().length];
		Vector<String> nameset = getNameset(heatset);
		int i = 0;
		for (Object set : heatset.getSelectedSets()) {
			indices[i] = nameset.indexOf(set);
			i++;
		}
		return indices;
	}
	
	public void refreshHeatView() {
		heatsTA.setText(generateHtmlHeats());
	}
	
	public void exportFile(String filename, String format) {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format;
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			if( format.equals("html") ) {
				writer.write(generateHtmlHeats());	
			}
			if( format.equals("csv") ) {
				generateCsvHeats(writer);
			}
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame(), "Error while saving " + filename + "(" + e +")",
					"Export Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private String generateHtmlHeats() {
		Vector<Heat> heats = refreshHeats(heatList.getSelectedValues());
		StringBuffer res = new StringBuffer("<html>");
		for (Heat heat : heats) {
			appendHtmlHeat(heat, res);
		}
		res.append("</html>");
		return res.toString();
	}


	/**
	 * @param result
	 * @param res
	 */
	private void appendHtmlHeat(Heat heat, StringBuffer res) {
		res.append("<h1>").append(heat.getName()).append("</h1>");
		res.append("<table>");
		int i = 1;
		for (Runner runner : heat.getQualifiedRunners()) {
			res.append("<tr><td>");
			res.append(i).append("</td><td>").append(runner.getName());
			res.append("</td></tr>");
			i++;
		}
		res.append("</table>");
	}
	
	private void generateCsvHeats(BufferedWriter writer) throws IOException {
		Vector<Heat> heats = refreshHeats(heatList.getSelectedValues());
		for (Heat heat : heats) {
			appendCsvHeat(heat, writer);			
		}
	}

	/**
	 * @param heat
	 * @param writer
	 * @throws IOException 
	 */
	private void appendCsvHeat(Heat heat, BufferedWriter writer) throws IOException {
		/*
		 * id,SI card,Name,Club,Course,Rented,Class,Start time,Finish Time,
		 * Status,NC,IOA,bonus
		 */		
		String courseName = heat.getName();
		for (Runner runner : heat.getQualifiedRunners()) {
			String[] dataLine = new String[] {
					Integer.toString(runner.getStartnumber()),
					runner.getChipnumber(),
					runner.getName(),
					runner.getClub().getName(),
					courseName,
					"false",
					runner.getCategory().getShortname(),
					"0",
					"0",
					"0",
					"false",
					"",
					"",
			};
			writer.write(Util.join(dataLine, ";", new StringBuffer()));
			writer.write("\n");
		}
	}

	
	/* (non-Javadoc)
	 * @see valmo.geco.ui.EventRegistry.Listener#changed(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
	//		repaint();
	}

	private void refresh() {
		updateNames();
		heatlistModel.clear();
		for (HeatSet heatset : registry().getHeatSets() ) {
			heatlistModel.addElement(heatset);	
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
