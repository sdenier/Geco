/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import net.geco.basics.TimeManager;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Dec 1, 2010
 *
 */
public class CardDataIO extends AbstractIO<RunnerRaceData> {
	
	public static String sourceFilename() {
		return "CardData.csv"; //$NON-NLS-1$
	}

	public CardDataIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
		if( this.reader!=null )
			this.reader.setCsvSep(";"); //$NON-NLS-1$
		if( this.writer!=null )
			this.writer.setCsvSep(";"); //$NON-NLS-1$
	}

	@Override
	public RunnerRaceData importTData(String[] record) {
		/*
		 * SI number,read time,clear time,check time,start time,finish time,
		 * control, time, ...
		 */
		Runner runner = this.registry.findRunnerByEcard(record[0]);
		if( runner==null ){
			throw new Error("Error in race data " + sourceFilename() +"! " //$NON-NLS-1$ //$NON-NLS-2$
							+ "Can't find runner with e-card " + record[0] //$NON-NLS-1$
							+ ". Use a backup"); //$NON-NLS-1$
		}
		
		RunnerRaceData data = this.factory.createRunnerRaceData();
		// this is the time since midnight, day of the race.
		data.setReadtime(TimeManager.safeParse(record[1]));
		data.setErasetime(TimeManager.safeParse(record[2]));
		data.setControltime(TimeManager.safeParse(record[3]));
		data.setStarttime(TimeManager.safeParse(record[4]));
		data.setFinishtime(TimeManager.safeParse(record[5]));
		data.setRunner(runner);
		
		Punch[] punches = new Punch[(record.length - 6) / 2];
		for (int i = 0; i < punches.length; i++) {
			punches[i] = this.factory.createPunch();
			punches[i].setCode(new Integer(record[2*i + 6]));
			punches[i].setTime(TimeManager.safeParse(record[2*i + 7]));
		};
		data.setPunches(punches);

		return data;
	}


	@Override
	public void register(RunnerRaceData data, Registry registry) {
		registry.addRunnerData(data);
	}

	@Override
	public String[] exportTData(RunnerRaceData d) {
		/*
		 * SI number,read time,clear time,check time,start time,finish time,
		 * control, time, ...
		 */
		Punch[] punches = d.getPunches();
		String [] record = new String[6 + 2 * punches.length];
		record[0] = d.getRunner().getEcard();
		record[1] = TimeManager.fullTime(d.getReadtime());
		record[2] = TimeManager.fullTime(d.getErasetime());
		record[3] = TimeManager.fullTime(d.getControltime());
		record[4] = TimeManager.fullTime(d.getStarttime());
		record[5] = TimeManager.fullTime(d.getFinishtime());
		for (int i = 0; i < punches.length; i++) {
			record[2*i+6] = Integer.toString(punches[i].getCode());
			record[2*i+7] = TimeManager.fullTime(punches[i].getTime());
		}
		return record;
	}
	
}
