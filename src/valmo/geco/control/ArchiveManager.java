/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import valmo.geco.core.Announcer.StageListener;
import valmo.geco.model.Archive;
import valmo.geco.model.ArchiveRunner;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Runner;
import valmo.geco.model.Stage;
import valmo.geco.model.iocsv.CsvReader;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveManager extends Control implements StageListener {
	
	private Archive archive;
	
//	private String archiveFilename;
	
	private File archiveFile;
	
	public ArchiveManager(GecoControl gecoControl) {
		super(ArchiveManager.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}
	
	public Archive archive() {
		if( archive==null ) {
			if( archiveFile==null ) {
				archive = new Archive();
			} else {
				try {
					loadArchiveFrom(archiveFile);
				} catch (IOException e) {
					geco().debug(e.getLocalizedMessage());
					archive = new Archive();
				}
			}
		}
		return this.archive;
	}
	
	public void loadArchiveFrom(File archiveFile) throws IOException {
		this.archiveFile = archiveFile;
		archive = new Archive();
		CsvReader reader = new CsvReader(";", archiveFile.getAbsolutePath());
		String[] record = reader.readRecord(); // bypass first line with headers
		record = reader.readRecord();
		while( record!=null ) {
			importInArchive(record);
			record = reader.readRecord();
		}
	}
	
	public String getArchiveName() {
		return ( archiveFile==null )? " " : archiveFile.getName();
	}
	
	public String archiveLastModified() {
		return ( archiveFile==null )? "" : 
									DateFormat.getDateInstance().format(new Date(archiveFile.lastModified()));
	}
	
	public Runner insertRunner(ArchiveRunner runner) {
		return null; // TODO: new ecard
	}
	
	public Runner findAndInsertRunner(String ecard) {
		ArchiveRunner arkRunner = archive().findRunner(ecard);
		if( arkRunner==null ){
			return null;
		}
		return insertRunner(arkRunner);
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

	@Override
	public void changed(Stage previous, Stage current) {
		archive = null; // discard old archive
		try {
			this.archiveFile = new File( stage().getProperties().getProperty(archiveFileProperty()) );
		} catch (NullPointerException e) {
			this.archiveFile = null;
		}
	}
	@Override
	public void saving(Stage stage, Properties properties) {
		if( archiveFile!=null ) {
			properties.setProperty(archiveFileProperty(), archiveFile.getAbsolutePath());
		}
	}
	@Override
	public void closing(Stage stage) {	}
	
	public static String archiveFileProperty() {
		return "ArchiveFile";
	}
	
}
