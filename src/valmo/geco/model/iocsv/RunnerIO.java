/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.iocsv;

import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
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
			runner.setFirstname("");
			runner.setLastname(record[2]);
		} else {
			runner.setFirstname(record[2].substring(0, i));
			runner.setLastname(record[2].substring(i+1));			
		}
		Club club = registry.findClub(record[3]);
		if( club == null ) {
			runner.setClub(registry.noClub());
			System.err.println("Unknown club " + record[3] + " for runner " + runner.idString());
		} else {
			runner.setClub(club);
		}
		Course course = registry.findCourse(record[4]);
		if( course == null ) {
			runner.setCourse(registry.anyCourse());
			System.err.println("Unknown course " + record[4] + " for runner " + runner.idString());
		} else {
			runner.setCourse(course);
		}
		Category cat = registry.findCategory(record[6]);
		if( cat == null ) {
			runner.setCategory(registry.noCategory());
			System.err.println("Unknown category " + record[6] + " for runner " + runner.idString());
		} else {
			runner.setCategory(cat);	
		}
		runner.setNC(new Boolean(record[10]));
		if( record[11].equals("") ) {
			runner.setArchiveId(null);
		} else {
			runner.setArchiveId(new Integer(record[11]));
		}
		return runner;
	}

	@Override
	public void register(Runner data, Registry registry) {
		registry.addRunner(data);
	}

	@Override
	public String[] exportTData(Runner r) {
		/*
		 * id,SI card,Name,Club,Course,Rented,Class,Start time,Finish Time,
		 * Status,NC,IOA,bonus,archiveid
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
				(r.getArchiveId()==null) ? "" : r.getArchiveId().toString(),
				"0","",
//				61;11211;Mark Young;CNOC;Short Course;true;M14;36000000;-2;2;false;;0;;
		};
	}


}
