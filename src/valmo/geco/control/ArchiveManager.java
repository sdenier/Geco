/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.IOException;

import valmo.geco.model.Archive;
import valmo.geco.model.ArchiveRunner;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.iocsv.CsvReader;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveManager extends Control {
	
	private Archive archive;
	
	public ArchiveManager(GecoControl gecoControl) {
		super(gecoControl);
		// TODO Auto-generated constructor stub
	}
	
	public Archive loadArchiveFrom(String filepath) throws IOException {
		archive = new Archive();
		CsvReader reader = new CsvReader(";", filepath);
		String[] record = reader.readRecord(); // bypass first line with headers
		record = reader.readRecord();
		while( record!=null ) {
			importInArchive(record);
			record = reader.readRecord();
		}
		return archive;
	}

	private void importInArchive(String[] record) {
//		[0-5] Ident. base de données;Puce;Nom;Prénom;Né;S;
//		[6-9] N° club;Nom;Ville;Nat;
//		[10-15] N° cat.;Court;Long;Num1;Num2;Num3;
//		E_Mail;Texte1;Texte2;Texte3;Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée
		Club club = ensureClub(record[7], record[8]);
		Category cat = ensureCategory(record[11], record[12]);
		importRunner(record, club, cat);
	}

	private void importRunner(String[] record, Club club, Category cat) {
//		[0-5] Ident. base de données;Puce;Nom;Prénom;Né;S;
		ArchiveRunner runner = geco().factory().createArchiveRunner();
		runner.setArchiveId(new Integer(record[0]));
		runner.setChipnumber(record[1]);
		runner.setLastname(trimQuotes(record[2]));
		runner.setFirstname(trimQuotes(record[3]));
		runner.setBirthYear(record[4]);
		runner.setSex(record[5]);
		runner.setClub(club);
		runner.setCategory(cat);
		archive.addRunner(runner);
	}


	private String trimQuotes(String record) { // remove " in "record"
		return record.substring(1, record.length() - 1);
	}


	private Club ensureClub(String shortName, String longName) {
		Club club = archive.findClub(longName);
		if( club==null ) {
			club = geco().factory().createClub();
			club.setName(trimQuotes(longName));
			club.setShortname(shortName);
			archive.addClub(club);
		}
		return club;
	}

	private Category ensureCategory(String shortName, String longName) {
		Category cat = archive.findCategory(shortName);
		if( cat==null ) {
			cat = geco().factory().createCategory();
			cat.setShortname(shortName);
			cat.setLongname(longName);
			archive.addCategory(cat);
		}
		return cat;
	}
	
}
