/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import net.geco.basics.TimeManager;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Feb 12, 2009
 *
 */
public class ResultDataIO extends AbstractIO<RunnerRaceData> {

	public static String sourceFilename() {
		return "ResultData.csv"; //$NON-NLS-1$
	}

	public ResultDataIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#exportTData(java.lang.Object)
	 */
	@Override
	public String[] exportTData(RunnerRaceData d) {
		// ecard, status, racetime
		return new String[] {
				d.getRunner().getEcard(),
				d.getResult().getStatus().name(),
				TimeManager.fullTime(d.getResult().getRacetime()),
		};
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#importTData(java.lang.String[])
	 */
	@Override
	public RunnerRaceData importTData(String[] record) {
		RunnerResult result = factory.createRunnerResult();
		// MIGR11
		Status en = ( record[1].equals("NDA") || record[1].equals("Unknown") || record[1].equals("REG") ) ? //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Status.NOS :
				Enum.valueOf(Status.class, record[1]);
		result.setStatus(en);
		result.setRacetime(TimeManager.safeParse(record[2]).getTime());
		RunnerRaceData data = registry.findRunnerData(record[0]);
		if( data==null ) {
			throw new Error("Error in race data " + sourceFilename() +"! " //$NON-NLS-1$ //$NON-NLS-2$
					+ "Can't find runner with e-card " + record[0] //$NON-NLS-1$
					+ ". Use a backup");	 //$NON-NLS-1$
		}
		data.setResult(result);
		return data;
	}
	
	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#register(java.lang.Object, valmo.geco.control.Registry)
	 */
	@Override
	public void register(RunnerRaceData data, Registry registry) {
	}


}
