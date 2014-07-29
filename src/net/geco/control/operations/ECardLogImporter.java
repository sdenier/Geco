/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.operations;

import static net.geco.basics.Util.safeTrimQuotes;

import java.io.File;
import java.io.IOException;

import net.geco.basics.CsvReader;
import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.ecardmodes.ECardMode;
import net.gecosi.dataframe.MockDataFrame;
import net.gecosi.dataframe.SiDataFrame;
import net.gecosi.dataframe.SiPunch;

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
		processECardData(processor, new CsvReader(";", file.getAbsolutePath())); //$NON-NLS-1$
	}

	public void processECardData(ECardMode processor, CsvReader reader) throws IOException {
		String[] record = reader.readRecord(); // bypass headers
		record = reader.readRecord();
		while( record!=null ){
			try {
				processor.processECard(convertRecord(record));
			} catch (Exception e) {
				geco().info("Wrong record (" + e.toString() + ") " + //$NON-NLS-1$ //$NON-NLS-2$
							Util.join(record, ",", new StringBuilder()), true); //$NON-NLS-1$
			}
			record = reader.readRecord();
		}
	}
	
	public SiDataFrame convertRecord(String[] record) {
/*		No.;read at;SI-Card;St no;cat.;First name;name;club;country;sex;year-op;Email;mobile;city;street;zip;
 * 		CLR_CN;CLR_DOW;clear time;CHK_CN;CHK_DOW;check time;ST_CN;ST_DOW;start time;FI_CN;FI_DOW;Finish time;No. of punches;
 * 		1.CN;1.DOW;1.Time;2.CN;2.DOW;2.Time;3.CN;3.DOW;3.Time;4.CN;4.DOW;4.Time;5.CN;5.DOW;5.Time;6.CN;6.DOW;6.Time;
 */
		String siNumber = safeTrimQuotes(record[2]);
		long checkTime = TimeManager.safeParse(safeTrimQuotes(record[21])).getTime();
		long startTime = TimeManager.safeParse(safeTrimQuotes(record[24])).getTime();
		long finishTime = TimeManager.safeParse(safeTrimQuotes(record[27])).getTime();
			
		int nbPunches = Integer.parseInt(safeTrimQuotes(record[28]));
		SiPunch[] punches = new SiPunch[nbPunches];
		for (int i = 0; i < nbPunches; i++) {
			punches[i] = new SiPunch(Integer.parseInt(safeTrimQuotes(record[i*3 + 29])),
									TimeManager.safeParse(safeTrimQuotes(record[i*3 + 31])).getTime());
		};

		return new MockDataFrame(siNumber, checkTime, startTime, finishTime, punches);
	}

}
