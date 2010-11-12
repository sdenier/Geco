/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.iocsv;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Jan 11, 2009
 *
 */
public class RaceDataIO extends AbstractIO<RunnerRaceData> {
	
	public static String sourceFilename() {
		return "results.csv"; //$NON-NLS-1$
	}

	public RaceDataIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
		if( this.reader!=null )
			this.reader.setCsvSep(";"); //$NON-NLS-1$
		if( this.writer!=null )
			this.writer.setCsvSep(";"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractImporter#importTData(java.lang.String[])
	 */
	@Override
	public RunnerRaceData importTData(String[] record) {
		/*
		 * SI number [,clear time, check time,start time,finish time,]
		 * raw clear time,raw check time,raw start time,raw finish time,
		 * control, time, ...
		 */
		RunnerRaceData data = this.factory.createRunnerRaceData();
		
		// this is the time since midnight, day of the race.						
		data.setErasetime(TimeManager.safeParse(record[1]));
		data.setControltime(TimeManager.safeParse(record[2]));
		data.setStarttime(TimeManager.safeParse(record[3]));
		data.setFinishtime(TimeManager.safeParse(record[4]));
		Runner runner = this.registry.findRunnerByChip(record[0]);
		if( runner==null ){
			throw new Error("Error in race data " + sourceFilename() +"! " //$NON-NLS-1$ //$NON-NLS-2$
							+ "Can't find runner with e-card " + record[0] //$NON-NLS-1$
							+ ". Use a backup"); //$NON-NLS-1$
		}
		data.setRunner(runner);
		
		Punch[] punches = new Punch[(record.length - 5) / 2];
		for (int i = 0; i < punches.length; i++) {
			punches[i] = this.factory.createPunch();
			punches[i].setCode(new Integer(record[2*i + 5]));
			punches[i].setTime(TimeManager.safeParse(record[2*i + 6]));
		};
		data.setPunches(punches);

		return data;
	}


	@Override
	public void register(RunnerRaceData data, Registry registry) {
		registry.addRunnerData(data);
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#exportTData(java.lang.Object)
	 */
	@Override
	public String[] exportTData(RunnerRaceData d) {
		/*
		 * SI number [,clear time, check time,start time,finish time,]
		 * raw clear time,raw check time,raw start time,raw finish time,
		 * control, time, ...
		 */
		Punch[] punches = d.getPunches();
		String [] record = new String[5 + 2 * punches.length];
		record[0] = d.getRunner().getChipnumber();
		record[1] = TimeManager.fullTime(d.getErasetime());
		record[2] = TimeManager.fullTime(d.getControltime());
		record[3] = TimeManager.fullTime(d.getStarttime());
		record[4] = TimeManager.fullTime(d.getFinishtime());
		for (int i = 0; i < punches.length; i++) {
			record[2*i+5] = Integer.toString(punches[i].getCode());
			record[2*i+6] = TimeManager.fullTime(punches[i].getTime());
		}
		return record;
	}


}
