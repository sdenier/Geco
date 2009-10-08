/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model.iocsv;

import valmo.geco.model.Club;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class ClubIO extends AbstractIO<Club> {

	public static String orFilename() {
		return "Clubs.csv";
	}
	
	public ClubIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
	}
	
	@Override
	public Club importTData(String[] record) {
		Club club = this.factory.createClub();
		club.setName(record[0]);
		return club;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractImporter#register(java.lang.Object, valmo.geco.csv.Registry)
	 */
	@Override
	public void register(Club data, Registry registry) {
		registry.addClub(data);
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#exportTData(java.lang.Object)
	 */
	@Override
	public String[] exportTData(Club c) {
		return new String[] {
			c.getName(),
		};
	}


}
