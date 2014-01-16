/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.File;
import java.io.IOException;

import net.geco.basics.CsvReader;


/**
 * @author Simon Denier
 * @since Nov 17, 2010
 *
 */
public abstract class OEImporter extends Control {

	private RunnerControl runnerControl;
	private StageControl stageControl;

	protected OEImporter(GecoControl gecoControl) {
		super(gecoControl);
		runnerControl = geco().getService(RunnerControl.class);
		stageControl = geco().getService(StageControl.class);
	}

	protected OEImporter(Class<? extends Control> clazz, GecoControl gecoControl) {
		super(clazz, gecoControl);
		runnerControl = geco().getService(RunnerControl.class);
		stageControl = geco().getService(StageControl.class);
	}

	public void loadArchiveFrom(File importFile) throws IOException {
		CsvReader reader = new CsvReader(";", importFile.getAbsolutePath()); //$NON-NLS-1$
		String[] record = reader.readRecord(); // bypass first line with headers
		record = reader.readRecord();
		while( record!=null ) {
			importRunnerRecord(record);
			record = reader.readRecord();
		}
	}
	
	protected abstract void importRunnerRecord(String[] record);

	protected RunnerControl runnerControl() {
		return runnerControl;
	}

	protected StageControl stageControl() {
		return stageControl;
	}

}