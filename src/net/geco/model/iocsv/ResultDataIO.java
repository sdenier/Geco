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
	
	@Override
	public String[] exportTData(RunnerRaceData d) {
		// ecard, status, racetime
		return new String[] {
				d.getRunner().getStartId().toString(),
				d.getResult().getStatus().name(),
				TimeManager.fullTime(d.getResult().getRacetime()),
		};
	}

	@Override
	public RunnerRaceData importTData(String[] record) {
		RunnerResult result = factory.createRunnerResult();
		result.setStatus(Enum.valueOf(Status.class, record[1]));
		result.setRacetime(TimeManager.safeParse(record[2]).getTime());
		RunnerRaceData data = registry.findRunnerData(Integer.valueOf(record[0]));
		if( data==null ) {
			throw new Error("Error in race data " + sourceFilename() +"! " //$NON-NLS-1$ //$NON-NLS-2$
					+ "Can't find runner with start id " + record[0] //$NON-NLS-1$
					+ ". Use a backup");	 //$NON-NLS-1$
		}
		data.setResult(result);
		return data;
	}
	
	@Override
	public void register(RunnerRaceData data, Registry registry) {
	}


}
