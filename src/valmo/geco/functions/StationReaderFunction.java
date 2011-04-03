/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import valmo.geco.basics.Html;
import valmo.geco.control.GecoControl;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.RunnerCreationException;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Nov 19, 2010
 *
 */
public class StationReaderFunction extends GecoFunction {

	private JCheckBox autoInsertB;
	private JButton setDnsB;

	public StationReaderFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return "Read Station Memory";
	}
	
	private RunnerControl runnerControl() {
		return getService(RunnerControl.class);
	}

	@Override
	public void execute() {

//		something to handle timeout?
		String[] ecards = null;
//		ecards = getService(SIReaderHandler.class).downloadBackupMemory();

		for (String ecard : ecards) {
			Runner runner = registry().findRunnerByChip(ecard);
			if( runner==null ) {
				if( autoInsertB.isSelected() ){
//					runner = getService(ArchiveManager.class).findAndCreateRunner(ecard); // default course
					if( runner==null ){
						try {
							runner = runnerControl().buildAnonymousRunner(ecard, registry().anyCourse());
						} catch (RunnerCreationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					runnerControl().registerNewRunner(runner);
					runnerControl().validateStatus(registry().findRunnerData(runner), Status.RUN);
				// 	TODO: need a flag to tell sireaderhandler to detect course when reading this ecard
				}
			} else {
				RunnerRaceData runnerData = registry().findRunnerData(runner);
				RunnerResult result = runnerData.getResult();
				if( result.is(Status.NOS) ){
					runnerControl().validateStatus(runnerData, Status.RUN);
				} else
				if( result.is(Status.DNS) ){
					geco().log("Inconsistency: detected a running e-card flagged as DNS");
				}
			}
		}
		if( setDnsB.isSelected() ){
			for (RunnerRaceData runnerData : registry().getRunnersData()) {
				if( runnerData.getResult().is(Status.NOS) ){
					runnerControl().validateStatus(runnerData, Status.DNS);
				}
			}
		}
	}

	@Override
	public String executeTooltip() {
		return "Read E-cards from station memory and update status of found and not found e-cards";
	}

	@Override
	public JComponent getParametersConfig() {
		Box config = Box.createVerticalBox();
		config.add(new JLabel(Html.htmlTag("font", "color=\"red\"", "Warning! Only use this function if station memories have been erased before the race.")));
		config.add(new JLabel("Place Start, Check, or Clear stations on the master station. " +
				"Function reads backup memory to detect:"));
		Html help = new Html();
		help.open("ul");
		help.tag("li", "registered runners which have started but not yet arrived");
		help.tag("li", "registered runners which did not start");
		help.tag("li", "unregistered e-cards");
		help.close("ul");
		config.add(new JLabel(help.close()));
		
		autoInsertB = new JCheckBox("Insert unregistered e-cards (look up in archive)");
		autoInsertB.setToolTipText("If an unknown e-card is detected in station memory, add a new entry with e-card. Function will also fill in data from archive if available");
		config.add(autoInsertB);

		JLabel setDnsL = new JLabel("After reading all stations, you can ");
		setDnsB = new JButton("mark Not Started entries as DNS");
		Box dnsBox = Box.createHorizontalBox();
		dnsBox.setAlignmentX(Box.LEFT_ALIGNMENT);
		dnsBox.add(setDnsL);
		dnsBox.add(setDnsB);
		config.add(dnsBox);
		return config;
	}

	@Override
	public void updateUI() {	}

}
