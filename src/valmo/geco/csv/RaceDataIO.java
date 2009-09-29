/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.csv;

import valmo.geco.control.TimeManager;
import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.Registry;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Jan 11, 2009
 *
 */
public class RaceDataIO extends AbstractIO<RunnerRaceData> {
	
	public static String sourceFilename() {
		return "results.csv";
	}

	public RaceDataIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
		if( this.reader!=null )
			this.reader.setCsvSep(";");
		if( this.writer!=null )
			this.writer.setCsvSep(";");
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
		// TODO: if null, look for start time?
		data.setStarttime(TimeManager.safeParse(record[3]));
		// TODO: if finish time is null, it's a MP right?
		data.setFinishtime(TimeManager.safeParse(record[4]));
		data.setRunner(this.registry.findRunnerByChip(record[0]));
		
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
			record[2*i+5] = new Integer(punches[i].getCode()).toString();
			record[2*i+6] = TimeManager.fullTime(punches[i].getTime());
		}
		return record;
	}


}
