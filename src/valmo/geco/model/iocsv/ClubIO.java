/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
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
		return "Clubs.csv"; //$NON-NLS-1$
	}
	
	public ClubIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
	}
	
	@Override
	public Club importTData(String[] record) {
		Club club = this.factory.createClub();
		club.setName(record[0]);
		club.setShortname(""); //$NON-NLS-1$
		return club;
	}

	@Override
	public void register(Club data, Registry registry) {
		registry.addClub(data);
	}

	@Override
	public String[] exportTData(Club c) {
		return new String[] {
			c.getName(),
		};
	}


}
