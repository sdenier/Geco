/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import valmo.geco.control.ArchiveManager;
import valmo.geco.control.GecoControl;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.SIReaderHandler;
import valmo.geco.core.Html;
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
	private JCheckBox autoDnsB;

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
		geco().log("Not working yet");
		// TODO: add "Started" status (same as "No Data" actually)
		// TODO: add "Running" StatItem = no data + started

//		something to handle timeout?
		String[] ecards = getService(SIReaderHandler.class).downloadBackupMemory();

		for (String ecard : ecards) {
			Runner runner = registry().findRunnerByChip(ecard);
			if( runner==null ) {
				if( autoInsertB.isSelected() ){
					runner = getService(ArchiveManager.class).findAndCreateRunner(ecard); // default course
					if( runner==null ){
						runner = runnerControl().buildAnonymousRunner(ecard, registry().anyCourse());
					}
					runnerControl().registerNewRunner(runner);
					runnerControl().validateStatus(registry().findRunnerData(runner), Status.Started);
				// 	TODO: need a flag to tell sireaderhandler to detect course when reading this ecard
				}
			} else {
				RunnerRaceData runnerData = registry().findRunnerData(runner);
				RunnerResult result = runnerData.getResult();
				if( result.is(Status.NDA) ){
					runnerControl().validateStatus(runnerData, Status.Started);
				} else
				if( result.is(Status.DNS) ){
					geco().log("Inconsistency: detected a running e-card flagged as DNS");
				}
			}
		}
		if( autoDnsB.isSelected() ){
			for (RunnerRaceData runnerData : registry().getRunnersData()) {
				if( runnerData.getResult().is(Status.NDA) ){
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
		config.add(new JLabel("Place Start, Check, or Clear stations on the master station. " +
				"Function reads backup memory to detect:"));
		Html help = new Html();
		help.open("ul");
		help.tag("li", "registered runners which have started but not yet arrived");
		help.tag("li", "registered runners which did not start");
		help.tag("li", "unregistered e-cards");
		help.close("ul");
		config.add(new JLabel(help.close()));
		config.add(new JLabel(Html.htmlTag("font", "color=\"red\"", "Warning! Only use this function if station memories have been erased before the race.")));
		config.add(Box.createVerticalStrut(10));
		
		autoInsertB = new JCheckBox("Insert unknown e-cards");
		autoInsertB.setToolTipText("If an unknown e-card is detected in station memory, add a new entry with e-card. Function will also fill in data from archive if available");
		config.add(autoInsertB);
		
		Html tooltipHtml = new Html();
		tooltipHtml.contents("Set remaining e-cards with No Data as DNS. ");
		tooltipHtml.tag("font", "color=\"red\"", "Only set this option when reading the last station");
		autoDnsB = new JCheckBox(tooltipHtml.close());
		autoDnsB.setToolTipText("After reading all backup memories, entries with No Data are set to DNS");
		config.add(autoDnsB);
		return config;
	}

	@Override
	public void updateUI() {	}

}
