/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.functions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.geco.basics.TimeManager;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.ecardmodes.ECardMode;
import net.geco.model.iocsv.CsvReader;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.ResultData;

/**
 * @author Simon Denier
 * @since Jun 11, 2012
 *
 */
public class ECardLogImporter extends Control {

	public ECardLogImporter(GecoControl gecoControl) {
		super(gecoControl);
	}

	public void processECardData(ECardMode processor, File file) throws IOException {
		processECardData(processor, new CsvReader(";", file.getAbsolutePath()));
	}

	public void processECardData(ECardMode processor, CsvReader reader) throws IOException {
		String[] record = reader.readRecord(); // bypass headers
		record = reader.readRecord();
		while( record!=null ){
			try {
				processor.processECard(convertRecord(record));
			} catch (IndexOutOfBoundsException e) {
				geco().info("Wrong record " + record, true);
			}
			record = reader.readRecord();
		}
	}
	
	public ResultData convertRecord(String[] record) {
/*		No.;read at;SI-Card;St no;cat.;First name;name;club;country;sex;year-op;Email;mobile;city;street;zip;
 * 		CLR_CN;CLR_DOW;clear time;CHK_CN;CHK_DOW;check time;ST_CN;ST_DOW;start time;FI_CN;FI_DOW;Finish time;No. of punches;
 * 		1.CN;1.DOW;1.Time;2.CN;2.DOW;2.Time;3.CN;3.DOW;3.Time;4.CN;4.DOW;4.Time;5.CN;5.DOW;5.Time;6.CN;6.DOW;6.Time;
 */
		ResultData card = new ResultData();
		card.setSiIdent(record[2]);
		card.setClearTime(TimeManager.safeParse(record[18]).getTime());
		card.setCheckTime(TimeManager.safeParse(record[21]).getTime());
		card.setStartTime(TimeManager.safeParse(record[24]).getTime());
		card.setFinishTime(TimeManager.safeParse(record[27]).getTime());
			
		int size = Integer.parseInt(record[28]);
		ArrayList<PunchObject> punches = new ArrayList<PunchObject>(size);
		for (int i = 0; i < size; i++) {
			punches.add( card.newPunch(Integer.parseInt(record[i*3 + 29]), TimeManager.safeParse(record[i*3 + 31]).getTime()));
		};
		card.setPunches(punches);

		// Punch times are already fixed in log file
		card.setCourseZeroTime(0);
		card.evaluateTimes();

		return card;
	}

}
