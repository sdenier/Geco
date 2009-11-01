/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model.iocsv;

import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;

/**
 * @author Simon Denier
 * @since Jan 3, 2009
 *
 */
public class RunnerIO extends AbstractIO<Runner> {
	
	public static String sourceFilename() {
		return "Competitors.csv";
	}
	
	public RunnerIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
		if( this.reader!=null )
			this.reader.setCsvSep(";");
		if( this.writer!=null )
			this.writer.setCsvSep(";");
	}

	@Override
	public Runner importTData(String[] record) {
		/*
		 * id,SI card,Name,Club,Course,Rented,Class,Start time,Finish Time,
		 * Status,NC,IOA,bonus
		 */
		Runner runner = this.factory.createRunner();
		runner.setStartnumber(new Integer(record[0]));
		runner.setChipnumber(record[1]);
		int i = record[2].lastIndexOf(" ");
		if(i==-1) {
			runner.setLastname(record[2]);
		} else {
			runner.setFirstname(record[2].substring(0, i));
			runner.setLastname(record[2].substring(i+1));			
		}
		Club club = registry.findClub(record[3]);
		if( club == null ) {
			runner.setClub(registry.findClub("[None]"));
		} else {
			runner.setClub(club);
		}
		runner.setCourse(registry.findCourse(record[4]));
		Category cat = registry.findCategory(record[6]);
		if( cat == null ) {
			runner.setCategory(this.factory.createCategory());
			runner.getCategory().setShortname(""); // TODO temporary hack, use default category like club
		} else {
			runner.setCategory(cat);	
		}
		runner.setNC(new Boolean(record[10]));
//		runner.setStarttime(record[7]);
		return runner;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractImporter#register(java.lang.Object, valmo.geco.csv.Registry)
	 */
	@Override
	public void register(Runner data, Registry registry) {
		registry.addRunner(data);
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#exportTData(java.lang.Object)
	 */
	@Override
	public String[] exportTData(Runner r) {
		/*
		 * id,SI card,Name,Club,Course,Rented,Class,Start time,Finish Time,
		 * Status,NC,IOA,bonus
		 */
		return new String[] {
				Integer.toString(r.getStartnumber()),
				r.getChipnumber(),
				r.getName(),
				r.getClub().getName(),
				r.getCourse().getName(),
				"false",
				r.getCategory().getShortname(),
				"", "",
				"1",//				0=not started,1=started,2=ok,3=dnf,4=mp,5=disq
				new Boolean(r.isNC()).toString(),
				"","0","",
//				61;11211;Mark Young;CNOC;Short Course;true;M14;36000000;-2;2;false;;0;
		};
	}


}
