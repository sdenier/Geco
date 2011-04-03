/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.Date;

import valmo.geco.basics.TimeManager;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Messages;
import valmo.geco.model.Runner;


/**
 * @author Simon Denier
 * @since Nov 17, 2010
 *
 */
public class StartlistImporter extends OEImporter {
	
	private SIReaderHandler siHandler;

	public StartlistImporter(GecoControl gecoControl) {
		super(StartlistImporter.class, gecoControl);
		siHandler = getService(SIReaderHandler.class);
	}

	@Override
	protected void importRunnerRecord(String[] record) {
		// [0-4] N° dép.;Puce;Ident. base de données;Nom;Prénom;
		// [5-12] Né;S;Plage;nc;Départ;Arrivée;Temps;Evaluation;
		// [13-19] N° club;Nom;Ville;Nat;N° cat.;Court;Long;
		// Num1;Num2;Num3;Text1;Text2;Text3;Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée;Engagement;Payé
		// ;1061511;10869;DENIER;Simon;80;H;;;00:46:00;;;;5906;5906NO;VALMO;France;11;H21A;H21A

		
		String ecard = record[1];
		String lastName = trimQuotes(record[3]);
		String firstName = trimQuotes(record[4]);
		if( ecard.equals("") ){ //$NON-NLS-1$
			geco().log(Messages.getString("StartlistImporter.NoEcardWarning") + firstName + " " + lastName); //$NON-NLS-1$ //$NON-NLS-2$
			ecard = runnerControl().newUniqueChipnumber();
			// TODO: an e-card is required for the registry, however it would be good to get past that REQ
			// part of the move to startnumber as id
		}
		
		Runner runner = runnerControl().buildBasicRunner(ecard); // ensure unique ecard
		if( ! record[0].equals("") ){ //$NON-NLS-1$
			runner.setStartnumber(new Integer(record[0])); // we do not ensure unique start number here
		}
		runner.setArchiveId(new Integer(record[2]));
		runner.setLastname(lastName);
		runner.setFirstname(firstName);
		runner.setNC(record[8].equals("X")); //$NON-NLS-1$
		
		Date relativeTime = TimeManager.safeParse(record[9]); // ! Time since zero hour
		runner.setRegisteredStarttime( TimeManager.absoluteTime(relativeTime, siHandler.getZeroTime()) );

		Club club = ensureClubInRegistry(record[15], record[14]);
		Category cat = ensureCategoryInRegistry(record[18], record[19]);
		runner.setClub(club);
		runner.setCategory(cat);
		runner.setCourse(registry().getDefaultCourseOrAnyFor(cat));
		
		runnerControl().registerNewRunner(runner);
	}

	
}
