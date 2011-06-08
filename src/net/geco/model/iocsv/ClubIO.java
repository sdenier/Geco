/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import net.geco.model.Club;
import net.geco.model.Factory;
import net.geco.model.Registry;

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
		club.setShortname(record[1]);
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
			c.getShortname()
		};
	}


}
