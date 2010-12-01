/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.iocsv;

import valmo.geco.core.TimeManager;
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
		return "Competitors.csv"; //$NON-NLS-1$
	}

	private long zeroTime;
	
	public RunnerIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry, long zeroTime) {
		super(factory, reader, writer, registry);
		if( this.reader!=null )
			this.reader.setCsvSep(";"); //$NON-NLS-1$
		if( this.writer!=null )
			this.writer.setCsvSep(";"); //$NON-NLS-1$
		this.zeroTime = zeroTime;
	}

	@Override
	public Runner importTData(String[] record) {
		/*
		 * id,SI card,First Name,Last Name,Club,Course,Rented,Class,Start time,Finish Time,
		 * Status,NC,IOA,bonus
		 */
		Runner runner = this.factory.createRunner();
		runner.setStartnumber(new Integer(record[0]));
		runner.setChipnumber(record[1]);
		
		// MIGR11: split first name and last name
		int s;
		if( record.length==14 ){
			s = 1;
			runner.setFirstname(record[2]);
			runner.setLastname(record[3]);
		} else {
			s = 0;
			int i = record[2].lastIndexOf(" "); //$NON-NLS-1$
			if(i==-1) {
				runner.setFirstname(""); //$NON-NLS-1$
				runner.setLastname(record[2]);
			} else {
				runner.setFirstname(record[2].substring(0, i));
				runner.setLastname(record[2].substring(i+1));			
			}			
		}
		Club club = registry.findClub(record[3 + s]);
		if( club == null ) {
			runner.setClub(registry.noClub());
			System.err.println("Unknown club " + record[3 + s] + " for runner " + runner.idString()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			runner.setClub(club);
		}
		Course course = registry.findCourse(record[4 + s]);
		if( course == null ) {
			runner.setCourse(registry.anyCourse());
			System.err.println("Unknown course " + record[4 + s] + " for runner " + runner.idString()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			runner.setCourse(course);
		}
		Category cat = registry.findCategory(record[6 + s]);
		if( cat == null ) {
			runner.setCategory(registry.noCategory());
			System.err.println("Unknown category " + record[6 + s] + " for runner " + runner.idString()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			runner.setCategory(cat);
		}
		runner.setRegisteredStarttime( TimeManager.absoluteTime(TimeManager.safeParse(record[7 + s]), zeroTime) );
		runner.setRentedEcard(new Boolean(record[5 + s]));
		runner.setNC(new Boolean(record[10 + s]));
		if( record[11 + s].equals("") ) { //$NON-NLS-1$
			runner.setArchiveId(null);
		} else {
			runner.setArchiveId(new Integer(record[11 + s]));
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
		 * id,SI card,First Name,Last Name,Club,Course,Rented,Class,Start time,Finish Time,
		 * Status,NC,IOA,bonus,archiveid
		 */
		return new String[] {
				Integer.toString(r.getStartnumber()),
				r.getChipnumber(),
				r.getFirstname(),
				r.getLastname(),
				r.getClub().getName(),
				r.getCourse().getName(),
				new Boolean(r.rentedEcard()).toString(),
				r.getCategory().getShortname(),
				( r.getRegisteredStarttime().equals(TimeManager.NO_TIME) ) ?
						"" :													 //$NON-NLS-1$
						TimeManager.fullTime(
								TimeManager.relativeTime(r.getRegisteredStarttime(), zeroTime) ),
				"", //$NON-NLS-1$
				"1",//				0=not started,1=started,2=ok,3=dnf,4=mp,5=disq //$NON-NLS-1$
				new Boolean(r.isNC()).toString(),
				(r.getArchiveId()==null) ? "" : r.getArchiveId().toString(), //$NON-NLS-1$
				"0","", //$NON-NLS-1$ //$NON-NLS-2$
//				61;11211;Mark;Young;CNOC;Short Course;true;M14;36000000;-2;2;false;;0;;
		};
	}


}
