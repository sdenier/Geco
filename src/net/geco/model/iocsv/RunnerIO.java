/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import net.geco.basics.TimeManager;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Runner;

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
//		id,Ecard,First Name,Last Name,Club,Course,Rented,Class,Start Time,Finish Time,Status,NC,Archive
		Runner runner = this.factory.createRunner();
		try {
			runner.setStartId(Integer.valueOf(record[0]));
		} catch (NumberFormatException e) {
			runner.setStartId(uniqueStartIdFromDerivedEcard(record[0]));
		}
		runner.setEcard(record[1]);
		runner.setFirstname(record[2]);
		runner.setLastname(record[3]);
		Club club = registry.findClub(record[4]);
		if( club == null ) {
			runner.setClub(registry.noClub());
			System.err.println("Unknown club " + record[4] + " for runner " + runner.idString()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			runner.setClub(club);
		}
		Course course = registry.findCourse(record[5]);
		if( course == null ) {
			runner.setCourse(registry.anyCourse());
			System.err.println("Unknown course " + record[5] + " for runner " + runner.idString()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			runner.setCourse(course);
		}
		Category cat = registry.findCategory(record[7]);
		if( cat == null ) {
			runner.setCategory(registry.noCategory());
			System.err.println("Unknown category " + record[7] + " for runner " + runner.idString()); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			runner.setCategory(cat);
		}
		runner.setRegisteredStarttime( TimeManager.absoluteTime(TimeManager.safeParse(record[8]), zeroTime) );
		runner.setRentedEcard(Boolean.parseBoolean(record[6]));
		runner.setNC(Boolean.parseBoolean(record[11]));
		if( record[12].equals("") ) { //$NON-NLS-1$
			runner.setArchiveId(null);
		} else {
			runner.setArchiveId(new Integer(record[12]));
		}
		return runner;
	}

	@Override
	public void register(Runner data, Registry registry) {
		registry.addRunner(data);
	}

	@Override
	public String[] exportTData(Runner r) {
//		id,Ecard,First Name,Last Name,Club,Course,Rented,Class,Start Time,Finish Time,Status,NC,Archive
		return new String[] {
				r.getStartId().toString(),
				r.getEcard(),
				r.getFirstname(),
				r.getLastname(),
				r.getClub().getName(),
				r.getCourse().getName(),
				Boolean.toString(r.rentedEcard()),
				r.getCategory().getShortname(),
				( r.getRegisteredStarttime().equals(TimeManager.NO_TIME) ) ?
						"" :													 //$NON-NLS-1$
						TimeManager.fullTime(
								TimeManager.relativeTime(r.getRegisteredStarttime(), zeroTime) ),
				"", //$NON-NLS-1$
				"", //0=not started,1=started,2=ok,3=dnf,4=mp,5=disq //$NON-NLS-1$
				Boolean.toString(r.isNC()),
				(r.getArchiveId()==null) ? "" : r.getArchiveId().toString(), //$NON-NLS-1$
		};
	}

	public Integer uniqueStartIdFromDerivedEcard(String startId) {
		int suffix = startId.indexOf('a');
		String prefix = startId.substring(0, suffix);
		int nbA = startId.substring(suffix).length();
		int start = Integer.parseInt(prefix);
		start *= 1000;
		for (int i = 0; i < nbA; i++) {
			start *= 10;
			start++;
		}
		return Integer.valueOf(start);
	}


}
