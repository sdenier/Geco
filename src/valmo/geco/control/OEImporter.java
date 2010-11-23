/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.File;
import java.io.IOException;

import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.iocsv.CsvReader;

/**
 * @author Simon Denier
 * @since Nov 17, 2010
 *
 */
public abstract class OEImporter extends Control {

	protected OEImporter(Class<? extends Control> clazz, GecoControl gecoControl) {
		super(clazz, gecoControl);
	}

	public void loadArchiveFrom(File importFile) throws IOException {
		CsvReader reader = new CsvReader(";", importFile.getAbsolutePath(), true); //$NON-NLS-1$
		String[] record = reader.readRecord(); // bypass first line with headers
		record = reader.readRecord();
		while( record!=null ) {
			importRunnerRecord(record);
			record = reader.readRecord();
		}
	}
	
	protected abstract void importRunnerRecord(String[] record);

	protected RunnerControl runnerControl() {
		return geco().getService(RunnerControl.class);
	}

	private StageControl stageControl() {
		return geco().getService(StageControl.class);
	}

	protected Club ensureClubInRegistry(String clubname, String shortname) {
		Club rClub = registry().findClub(clubname);
		if( rClub==null ) {
			rClub = stageControl().createClub(clubname, shortname);
		}
		return rClub;
	}

	protected Category ensureCategoryInRegistry(String categoryname, String longname) {
		Category rCat = registry().findCategory(categoryname);
		if( rCat==null ){
			rCat = stageControl().createCategory(categoryname, longname);
		}
		return rCat;
	}

	protected String trimQuotes(String record) { // remove " in "record"
		if( record.charAt(0)=='"' ){
			return record.substring(1, record.length() - 1);
		} else {
			return record;
		}
	}

}