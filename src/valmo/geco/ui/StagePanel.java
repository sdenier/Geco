/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import valmo.geco.Geco;
import valmo.geco.control.SIReaderHandler.SerialPort;
import valmo.geco.control.SingleSplitPrinter;
import valmo.geco.core.Announcer;
import valmo.geco.core.Html;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Messages;
import valmo.geco.model.Stage;
import valmo.geco.model.xml.CourseSaxImporter;

/**
 * @author Simon Denier
 * @since Feb 8, 2009
 *
 */
public class StagePanel extends TabPanel {

	public StagePanel(Geco geco, JFrame frame) {
		super(geco, frame);
		refresh();
	}
	
	public void refresh() {
		this.removeAll();
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = SwingUtils.compConstraint(
													GridBagConstraints.RELATIVE,
													0,
													GridBagConstraints.BOTH,
													GridBagConstraints.NORTH);
		c.insets = new Insets(10, 10, 0, 0);
		panel.add(stageConfigPanel(), c);
		panel.add(checkerConfigPanel(), c);
		panel.add(sireaderConfigPanel(), c);

		c.gridy = 1;
		panel.add(clubConfigPanel(), c);
		panel.add(courseConfigPanel(), c);
		panel.add(categoryConfigPanel(), c);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
	}

	private JPanel titlePanel(JPanel panel, String title) {
		JPanel embed = SwingUtils.embed(panel);
		embed.setBorder(BorderFactory.createTitledBorder(title));
		return embed;
	}

	private JPanel stageConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(Messages.uiGet("StagePanel.StageNameLabel")), c); //$NON-NLS-1$
		final JTextField stagenameF = new JTextField(geco().stage().getName());
		stagenameF.setColumns(12);
		stagenameF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateStagename(stagenameF);
			}
		});
		stagenameF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				return verifyStagename(stagenameF.getText());
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				return validateStagename(stagenameF);
			}
		});
		panel.add(stagenameF, c);
		c.gridy = 1;
		panel.add(new JLabel(Messages.uiGet("StagePanel.PreviousStageLabel")), c); //$NON-NLS-1$
		JTextField previousF = new JTextField(geco().getPreviousStageDir());
		previousF.setEditable(false);
		previousF.setToolTipText(Messages.uiGet("StagePanel.PreviousStageTooltip")); //$NON-NLS-1$
		panel.add(previousF, c);
		c.gridy = 2;
		panel.add(new JLabel(Messages.uiGet("StagePanel.NextStageLabel")), c); //$NON-NLS-1$
		JTextField nextF = new JTextField(geco().getNextStageDir());
		nextF.setEditable(false);
		nextF.setToolTipText(Messages.uiGet("StagePanel.NextStageTooltip")); //$NON-NLS-1$
		panel.add(nextF, c);
		return titlePanel(panel, Messages.uiGet("StagePanel.StageConfigTitle")); //$NON-NLS-1$
	}
	
	private boolean verifyStagename(String text) {
		return ! text.trim().isEmpty();
	}
	private boolean validateStagename(JTextField stagenameF) {
		if( verifyStagename(stagenameF.getText()) ){
			geco().stage().setName(stagenameF.getText().trim());
			((GecoWindow) frame()).updateWindowTitle();
			return true;					
		} else {
			geco().info(Messages.uiGet("StagePanel.StageNameEmptyWarning"), true); //$NON-NLS-1$
			stagenameF.setText(geco().stage().getName());
			return false;
		}	
	}

	private JPanel checkerConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(Messages.uiGet("StagePanel.MPLimitLabel")), c); //$NON-NLS-1$
		int mpLimit = geco().checker().getMPLimit();
		final JSpinner mplimitS = new JSpinner(new SpinnerNumberModel(mpLimit, 0, null, 1));
		mplimitS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		mplimitS.setToolTipText(Messages.uiGet("StagePanel.MPLimitTooltip")); //$NON-NLS-1$
		mplimitS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int oldLimit = geco().checker().getMPLimit();
				int newLimit = ((Integer) mplimitS.getValue()).intValue();
				if( oldLimit!=newLimit ) {
					geco().checker().setMPLimit(newLimit);
				}
			}
		});
		panel.add(SwingUtils.embed(mplimitS), c);
		
		c.gridy = 1;
		panel.add(new JLabel(Messages.uiGet("StagePanel.TimePenaltyLabel")), c); //$NON-NLS-1$
		long penalty = geco().checker().getMPPenalty() / 1000;
		final JSpinner penaltyS = new JSpinner(new SpinnerNumberModel(penalty, 0l, null, 10));
		penaltyS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		penaltyS.setToolTipText(Messages.uiGet("StagePanel.TimePenaltyTooltip")); //$NON-NLS-1$
		penaltyS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				long oldPenalty = geco().checker().getMPPenalty();
				long newPenalty = 1000 * ((Long) penaltyS.getValue()).longValue();
				if( oldPenalty!=newPenalty ) {
					geco().checker().setMPPenalty(newPenalty);
				}
			}
		});
		panel.add(SwingUtils.embed(penaltyS), c);

		c.gridy = 2;
		c.gridwidth = 2;
		String helpL = new Html()
			.open("i") //$NON-NLS-1$
			.contents(Messages.uiGet("StagePanel.MPConfigHelp1")) //$NON-NLS-1$
			.tag("b", Messages.uiGet("StagePanel.MPConfigHelp2")) //$NON-NLS-1$ //$NON-NLS-2$
			.contents(Messages.uiGet("StagePanel.MPConfigHelp3")) //$NON-NLS-1$
			.close("i").close(); //$NON-NLS-1$
		panel.add(new JLabel(helpL), c);
		
		return titlePanel(panel, Messages.uiGet("StagePanel.OrientshowConfigTitle")); //$NON-NLS-1$
	}
	
	
	private JPanel sireaderConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel(Messages.uiGet("StagePanel.StationPortLabel")), c); //$NON-NLS-1$
		final JComboBox stationPortCB = new JComboBox(geco().siHandler().listPorts());
		stationPortCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		stationPortCB.setToolTipText(Messages.uiGet("StagePanel.StationPortTooltip")); //$NON-NLS-1$
		panel.add(stationPortCB, c);
		stationPortCB.setSelectedItem(geco().siHandler().getPort());
		stationPortCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().siHandler().setPort( (SerialPort) stationPortCB.getSelectedItem() );
			}
		});
		
		c.gridy = 1;
		panel.add(new JLabel(Messages.uiGet("StagePanel.ZeroHourLabel")), c); //$NON-NLS-1$
		final SimpleDateFormat formatter = new SimpleDateFormat("H:mm"); //$NON-NLS-1$
		formatter.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		final JTextField zerohourF = new JTextField(formatter.format(geco().siHandler().getZeroTime()));
		zerohourF.setColumns(7);
		zerohourF.setToolTipText(Messages.uiGet("StagePanel.ZeroHourTooltip")); //$NON-NLS-1$
		panel.add(zerohourF, c);
		zerohourF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateZeroHour(formatter, zerohourF);
			}
		});
		zerohourF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				try {
					formatter.parse(zerohourF.getText());
					return true;
				} catch (ParseException e) {
					return false;
				}
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				return validateZeroHour(formatter, zerohourF);
			}
		});
	
		c.gridy = 2;
		panel.add(new JLabel(Messages.uiGet("StagePanel.SplitPrinterLabel")), c); //$NON-NLS-1$
		final JComboBox printersCB = new JComboBox(geco().splitPrinter().listPrinterNames());
		printersCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		printersCB.setSelectedItem(geco().splitPrinter().getSplitPrinterName());
		panel.add(printersCB, c);
		printersCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().splitPrinter().setSplitPrinterName((String) printersCB.getSelectedItem());
			}
		});
		
		c.gridy = 3;
		panel.add(new JLabel(Messages.uiGet("StagePanel.SplitFormatLabel")), c); //$NON-NLS-1$
		final JComboBox splitFormatCB = new JComboBox(SingleSplitPrinter.SplitFormat.values());
		splitFormatCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		splitFormatCB.setSelectedItem(geco().splitPrinter().getSplitFormat());
		panel.add(splitFormatCB, c);
		splitFormatCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().splitPrinter().setSplitFormat((SingleSplitPrinter.SplitFormat) splitFormatCB.getSelectedItem());
			}
		});
		
		return titlePanel(panel, Messages.uiGet("StagePanel.SIReaderConfigTitle")); //$NON-NLS-1$
	}
	
	private boolean validateZeroHour(SimpleDateFormat formatter, JTextField zerohourF) {
		try {
			long oldTime = geco().siHandler().getZeroTime();
			long zeroTime = formatter.parse(zerohourF.getText()).getTime();
			geco().siHandler().setNewZeroTime(zeroTime);
			geco().runnerControl().updateRegisteredStarttimes(zeroTime, oldTime);
			return true;
		} catch (ParseException e1) {
			geco().info(Messages.uiGet("StagePanel.ZeroHourBadFormatWarning"), true); //$NON-NLS-1$
			zerohourF.setText(formatter.format(geco().siHandler().getZeroTime()));
			return false;
		}
	}

	
	private JPanel clubConfigPanel() {
		final ConfigTablePanel<Club> panel = new ConfigTablePanel<Club>(geco(), frame());
		
		final ConfigTableModel<Club> tableModel = 
			new ConfigTableModel<Club>(new String[] {
											Messages.uiGet("StagePanel.ClubShortNameHeader"), //$NON-NLS-1$
											Messages.uiGet("StagePanel.ClubLongNameHeader")}) { //$NON-NLS-1$
				@Override
				public Object getValueIn(Club club, int columnIndex) {
					switch (columnIndex) {
					case 0: return club.getShortname();
					case 1: return club.getName();
					default: return super.getValueIn(club, columnIndex);
					}
				}
				@Override
				public void setValueIn(Club club, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateShortname(club, (String) value); break;
					case 1: geco().stageControl().updateName(club, (String) value); break;
					default: break;
					}
				}
		};
		tableModel.setData(registry().getSortedClubs());
		geco().announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
			public void clubsChanged() {
				tableModel.setData(registry().getSortedClubs());
			}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createClub();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Club club = panel.getSelectedData();
				if( club!=null ) {
					boolean removed = geco().stageControl().removeClub(club);
					if( !removed ) {
						JOptionPane.showMessageDialog(frame(),
							    Messages.uiGet("StagePanel.ClubNoDeletionWarning"), //$NON-NLS-1$
							    Messages.uiGet("StagePanel.ActionCancelledTitle"), //$NON-NLS-1$
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize(
				Messages.uiGet("StagePanel.ClubConfigTitle"), //$NON-NLS-1$
				tableModel,
				addAction,
				removeAction);
		return panel;
	}

	private JPanel courseConfigPanel() {
		final ConfigTablePanel<Course> panel = new ConfigTablePanel<Course>(geco(), frame());
		
		final ConfigTableModel<Course> tableModel = 
			new ConfigTableModel<Course>(new String[] {
												Messages.uiGet("StagePanel.CourseNameHeader"), //$NON-NLS-1$
												Messages.uiGet("StagePanel.CourseNbControlsHeader")}) { //$NON-NLS-1$
				@Override
				public Object getValueIn(Course course, int columnIndex) {
					switch (columnIndex) {
					case 0: return course.getName();
					case 1: return course.nbControls();
					default: return super.getValueIn(course, columnIndex);
					}
				}
				@Override
				public boolean isCellEditable(int row, int col) {
					return col == 0;
				}
				@Override
				public void setValueIn(Course course, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateName(course, (String) value); break;
					default: break;
					}
				}
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					switch (columnIndex) {
					case 1: return Integer.class;
					default: return super.getColumnClass(columnIndex);
					}

				}
		};
		tableModel.setData(registry().getSortedCourses());
		
		geco().announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {
				tableModel.setData(new Vector<Course>(registry().getSortedCourses()));
			}
			public void clubsChanged() {}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createCourse();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Course course = panel.getSelectedData();
				if( course!=null ) {
					try {
						geco().stageControl().removeCourse(course);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame(),
							Messages.uiGet("StagePanel.CourseNoDeletionWarning") + e1.getMessage(), //$NON-NLS-1$
							Messages.uiGet("StagePanel.ActionCancelledTitle"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};
		ActionListener editAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Course course = panel.getSelectedData();
				if( course!=null ) {
					new CourseControlDialog(frame(), course);
				}
			}
		};
		ActionListener importAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setSelectedFile(new File(geco().getCurrentStagePath()));
				chooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return Messages.uiGet("StagePanel.XMLFilesLabel"); //$NON-NLS-1$
					}
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".xml"); //$NON-NLS-1$
					}
				});
				int answer = chooser.showDialog(frame(), Messages.uiGet("StagePanel.CourseImportXMLLabel")); //$NON-NLS-1$
				if( answer==JFileChooser.APPROVE_OPTION ) {
					String file = chooser.getSelectedFile().getAbsolutePath();
					try {
						Vector<Course> courses = CourseSaxImporter.importFromXml(file, geco().stageControl().factory());
						for (Course course : courses) {
							geco().stageControl().addCourse(course);	
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(frame(),
								e1.getMessage(),
								Messages.uiGet("StagePanel.XMLLoadError"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		ActionListener refreshAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Course course = panel.getSelectedData();
				if( course!=null ) {
					geco().runnerControl().recheckRunnersFromCourse(course);
				}
			}
		};
		
		JButton editB = new JButton(Messages.uiGet("StagePanel.CourseEditLabel")); //$NON-NLS-1$
		editB.setToolTipText(Messages.uiGet("StagePanel.CourseEditTooltip")); //$NON-NLS-1$
		editB.addActionListener(editAction);
		
		JButton importB = new JButton(Messages.uiGet("StagePanel.CourseXMLLabel")); //$NON-NLS-1$
		importB.setToolTipText(Messages.uiGet("StagePanel.CourseXMLTooltip")); //$NON-NLS-1$
		importB.addActionListener(importAction);

		JButton refreshB = new JButton(Messages.uiGet("StagePanel.CourseRecheckLabel")); //$NON-NLS-1$
		refreshB.setToolTipText(Messages.uiGet("StagePanel.CourseRecheckTooltip")); //$NON-NLS-1$
		refreshB.addActionListener(refreshAction);

		panel.initialize(
				Messages.uiGet("StagePanel.CourseConfigTitle"), //$NON-NLS-1$
				tableModel,
				addAction,
				removeAction,
				editB,
				importB,refreshB);
		return panel;
	}	

	
	private JPanel categoryConfigPanel() {
		final ConfigTablePanel<Category> panel = new ConfigTablePanel<Category>(geco(), frame());
		final JComboBox coursesCB = new JComboBox();
		
		final ConfigTableModel<Category> tableModel = 
			new ConfigTableModel<Category>(new String[] {
												Messages.uiGet("StagePanel.CategoryShortNameHeader"), //$NON-NLS-1$
												Messages.uiGet("StagePanel.CategoryLongNameHeader"), //$NON-NLS-1$
												"Course" }) {
				@Override
				public Object getValueIn(Category cat, int columnIndex) {
					switch (columnIndex) {
					case 0: return cat.getShortname();
					case 1: return cat.getLongname();
					case 2: return (cat.getCourse()==null) ? "" : cat.getCourse().getName();
					default: return super.getValueIn(cat, columnIndex);
					}
				}
				@Override
				public void setValueIn(Category cat, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateShortname(cat, (String) value); break;
					case 1: geco().stageControl().updateName(cat, (String) value); break;
					case 2: setCourse(cat, (String) value); break;
					default: break;
					}
				}
				private void setCourse(Category cat, String coursename) {
					if( coursename.equals("") ){
						cat.setCourse(null);
					} else {
						cat.setCourse(registry().findCourse(coursename));
					}
				}
		};
		tableModel.setData(registry().getSortedCategories());
		
		geco().announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {
				updateCoursesCBforCategories(coursesCB);
			}
			public void clubsChanged() {}
			public void categoriesChanged() {
				tableModel.setData(registry().getSortedCategories());
			}
		});
	
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createCategory();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Category cat = panel.getSelectedData();
				if( cat!=null ) {
					try {
						geco().stageControl().removeCategory(cat);	
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(frame(),
								Messages.uiGet("StagePanel.CategoryNoDeletionWarning") + e2.getMessage(), //$NON-NLS-1$
								Messages.uiGet("StagePanel.ActionCancelledTitle"), //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize(
				Messages.uiGet("StagePanel.CategoryConfigTitle"), //$NON-NLS-1$
				tableModel,
				addAction,
				removeAction);
		updateCoursesCBforCategories(coursesCB);
		panel.table().getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(coursesCB));
		return panel;
	}

	private void updateCoursesCBforCategories(final JComboBox coursesCB) {
		Vector<String> sortedCoursenames = new Vector<String>( registry().getSortedCoursenames() );
		sortedCoursenames.add(0, "");
		coursesCB.setModel(new DefaultComboBoxModel(sortedCoursenames));
	}
		
	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
		frame().repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		super.saving(stage, properties);
	}	

}
