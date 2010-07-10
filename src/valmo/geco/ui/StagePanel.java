/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
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
//		setBorder(BorderFactory.createLineBorder(Color.gray));

		GridBagConstraints c = Util.compConstraint(	GridBagConstraints.RELATIVE,
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

		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(panel);
	}

	private JPanel titlePanel(JPanel panel, String title) {
		JPanel embed = Util.embed(panel);
		embed.setBorder(BorderFactory.createTitledBorder(title));
		return embed;
	}

	private JPanel stageConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Stage name:"), c);
		final JTextField stagenameF = new JTextField(geco().stage().getName());
		stagenameF.setColumns(12);
		stagenameF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stage().setName(stagenameF.getText());
				((GecoWindow) frame()).updateWindowTitle();
			}
		});
		panel.add(stagenameF, c);
		c.gridy = 1;
		panel.add(new JLabel("Previous stage:"), c);
		JTextField previousF = new JTextField(geco().getPreviousStageDir());
		previousF.setEditable(false);
		previousF.setToolTipText("Edit 'stages.prop' in parent folder to change stage order");
		panel.add(previousF, c);
		c.gridy = 2;
		panel.add(new JLabel("Next stage:"), c);
		JTextField nextF = new JTextField(geco().getNextStageDir());
		nextF.setEditable(false);
		nextF.setToolTipText("Edit 'stages.prop' in parent folder to change stage order");
		panel.add(nextF, c);
		return titlePanel(panel, "Stage");
	}

	private JPanel checkerConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("MP limit:"), c);
		int mpLimit = geco().checker().getMPLimit();
		final JTextField mplimitF = new JTextField(new Integer(mpLimit).toString());
		mplimitF.setColumns(7);
		mplimitF.setToolTipText("Number of missing punches authorized before marking the runner as MP.");
		mplimitF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					geco().checker().setMPLimit(new Integer(mplimitF.getText()));
					int confirm = JOptionPane.showConfirmDialog(frame(), "Recheck statuses for runners", 
																"Recheck all?", JOptionPane.YES_NO_OPTION);
					if( confirm==JOptionPane.YES_OPTION ) {
						geco().runnerControl().recheckAllRunners();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(frame(), "Not an integer. Reverting.");
					int mpLimit = geco().checker().getMPLimit();
					mplimitF.setText(new Integer(mpLimit).toString());
				}
			}
		});
		panel.add(mplimitF, c);
		
		c.gridy = 1;
		panel.add(new JLabel("Time penalty:"), c);
		long penalty = geco().checker().getMPPenalty() / 1000;
		final JTextField penaltyF = new JTextField(new Long(penalty).toString());
		penaltyF.setColumns(7);
		penaltyF.setToolTipText("Time penalty per missing punch in seconds");
		penaltyF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					geco().checker().setMPPenalty(1000 * new Long(penaltyF.getText()));
					int confirm = JOptionPane.showConfirmDialog(frame(), "Recheck statuses for runners",
																"Recheck all?", JOptionPane.YES_NO_OPTION);
					if( confirm==JOptionPane.YES_OPTION ) {
						geco().runnerControl().recheckAllRunners();
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(frame(), "Not an integer. Reverting.");
					long penalty = geco().checker().getMPPenalty();
					penaltyF.setText(new Long(penalty).toString());
				}				
			}
		});
		panel.add(penaltyF, c);
		
		return titlePanel(panel, "Orientshow");
	}
	
	private JPanel sireaderConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Station port:"), c);
		final JTextField stationPortF = new JTextField(geco().siHandler().getPortName());
		stationPortF.setColumns(12);
		stationPortF.setToolTipText("Serial port for the SI station (COMx on Windows, /dev/ttyX on Linux/Mac)");
		panel.add(stationPortF, c);
		stationPortF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().siHandler().setPortName(stationPortF.getText());
			}
		});
		
		c.gridy = 1;
		panel.add(new JLabel("Zero hour:"), c);
		final SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		final JTextField zerohourF = new JTextField(formatter.format(geco().siHandler().getZeroTime()));
		zerohourF.setColumns(7);
		zerohourF.setToolTipText("Zero hour of the competition");
		panel.add(zerohourF, c);
		zerohourF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Date zeroTime = formatter.parse(zerohourF.getText());
					geco().siHandler().setZeroTime(zeroTime.getTime());
				} catch (ParseException e1) {
					geco().info("Bad time format", true);
				}
			}
		});
		
		return titlePanel(panel, "SI Reader");
	}

	
	private JPanel clubConfigPanel() {
		final ConfigTablePanel<Club> panel = new ConfigTablePanel<Club>(geco(), frame());
		
		final ConfigTableModel<Club> tableModel = 
			new ConfigTableModel<Club>(new String[] {"Short name", "Long name"}) {
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
							    "This club can not be deleted because some runners are registered with it.",
							    "Action cancelled",
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize("Club", tableModel, addAction, removeAction);
		return panel;
	}

	private JPanel courseConfigPanel() {
		final ConfigTablePanel<Course> panel = new ConfigTablePanel<Course>(geco(), frame());
		
		final ConfigTableModel<Course> tableModel = 
			new ConfigTableModel<Course>(new String[] {"Name", "Nb controls"}) {
				@Override
				public Object getValueIn(Course course, int columnIndex) {
					switch (columnIndex) {
					case 0: return course.getName();
					case 1: return course.getCodes().length;
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
							"This course can not be deleted because " + e1.getMessage(),
							"Action cancelled",
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
				chooser.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "XML files";
					}
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".xml");
					}
				});
				int answer = chooser.showDialog(frame(), "Import");
				if( answer==JFileChooser.APPROVE_OPTION ) {
					String file = chooser.getSelectedFile().getAbsolutePath();
					try {
						Vector<Course> courses = CourseSaxImporter.importFromXml(file, geco().stageControl().factory());
						for (Course course : courses) {
							geco().stageControl().addCourse(course);	
						}
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame(),
								e1.getMessage(),
								"Error loading XML",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		
		JButton editB = new JButton("...");
		editB.setToolTipText("Edit course");
		editB.addActionListener(editAction);
		
		JButton importB = new JButton("XML");
		importB.setToolTipText("Import courses from XML");
		importB.addActionListener(importAction);

		panel.initialize("Course", tableModel, addAction, removeAction, editB, importB);
		return panel;
	}	

	
	private JPanel categoryConfigPanel() {
		final ConfigTablePanel<Category> panel = new ConfigTablePanel<Category>(geco(), frame());
	
		final ConfigTableModel<Category> tableModel = 
			new ConfigTableModel<Category>(new String[] {"Short name", "Long name"}) {
				@Override
				public Object getValueIn(Category cat, int columnIndex) {
					switch (columnIndex) {
					case 0: return cat.getShortname();
					case 1: return cat.getLongname();
//					case 2: return (cat.getCourse()==null) ? "" : cat.getCourse().getName();
					default: return super.getValueIn(cat, columnIndex);
					}
				}
				@Override
				public void setValueIn(Category cat, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateShortname(cat, (String) value); break;
					case 1: geco().stageControl().updateName(cat, (String) value); break;
					default: break;
					}
				}
		};
		tableModel.setData(registry().getSortedCategories());
		
		geco().announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
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
								"This category can not be deleted because " + e2.getMessage(),
								"Action cancelled",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize("Category", tableModel, addAction, removeAction);
		return panel;
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
