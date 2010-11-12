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

import valmo.geco.core.Messages;
import valmo.geco.core.Announcer.StageListener;
import valmo.geco.model.Archive;
import valmo.geco.model.ArchiveRunner;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
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
		CsvReader reader = new CsvReader(";", archiveFile.getAbsolutePath()); //$NON-NLS-1$
		String[] record = reader.readRecord(); // bypass first line with headers
		record = reader.readRecord();
		while( record!=null ) {
			importInArchive(record);
			record = reader.readRecord();
		}
	}
	
	public String getArchiveName() {
		return ( archiveFile==null )? " " : archiveFile.getName(); //$NON-NLS-1$
	}
	
	public String archiveLastModified() {
		return ( archiveFile==null )? "" :  //$NON-NLS-1$
									DateFormat.getDateInstance().format(new Date(archiveFile.lastModified()));
	}
	
	public Runner findAndCreateRunner(String ecard, Course course) {
		ArchiveRunner arkRunner = archive().findRunner(ecard);
		if( arkRunner==null ){
			return null;
		}
		return createRunner(arkRunner, course);
	}
	
	public Runner insertRunner(ArchiveRunner arkRunner) {
		Runner runner = createRunner(arkRunner, registry().anyCourse());
		runnerControl().registerNewRunner(runner);
		return runner;
	}
	private Runner createRunner(ArchiveRunner arkRunner, Course course) {
		Club rClub = ensureClubInRegistry(arkRunner.getClub());
		Category rCat = ensureCategoryInRegistry(arkRunner.getCategory());
		String ecard = arkRunner.getChipnumber();
		if( ecard.equals("") ){ //$NON-NLS-1$
			geco().log(Messages.getString("ArchiveManager.NoMatchingEcardWarning") + arkRunner.getName()); //$NON-NLS-1$
			ecard = runnerControl().newUniqueChipnumber();
			// TODO: an e-card is required for the registry, however it would be good to get past that REQ
			// part of the move to startnumber as id
		}
		Runner runner = runnerControl().buildBasicRunner(ecard); // ensure unique ecard
		runner.setArchiveId(arkRunner.getArchiveId());
		runner.setFirstname(arkRunner.getFirstname());
		runner.setLastname(arkRunner.getLastname());
		runner.setClub(rClub);
		runner.setCategory(rCat);
		runner.setCourse(course);
		return runner;
	}
	private Club ensureClubInRegistry(Club club) {
		Club rClub = registry().findClub(club.getName());
		if( rClub==null ) {
			rClub = stageControl().createClub(club.getName(), club.getShortname());
		}
		return rClub;
	}
	private Category ensureCategoryInRegistry(Category category) {
		Category rCat = registry().findCategory(category.getName());
		if( rCat==null ){
			rCat = stageControl().createCategory(category.getShortname(), category.getLongname());
		}
		return rCat;
	}
	private RunnerControl runnerControl() {
		return geco().getService(RunnerControl.class);
	}
	private StageControl stageControl() {
		return geco().getService(StageControl.class);
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
		return "ArchiveFile"; //$NON-NLS-1$
	}
	
}
