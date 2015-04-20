/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import static net.geco.basics.Util.safeTrimQuotes;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import net.geco.basics.Announcer.StageListener;
import net.geco.model.Archive;
import net.geco.model.ArchiveRunner;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveManager extends OEImporter implements StageListener {
	
	private Archive archive;
	
	private File archiveFile;

	private int defaultArchiveCounter = 999000000;

	public ArchiveManager(GecoControl gecoControl) {
		super(ArchiveManager.class, gecoControl);
		geco().announcer().registerStageListener(this);
		changed(null, null);
	}
	
	public Archive archive() throws IOException {
		if( archive==null ) {
			if( archiveFile==null ) {
				archive = new Archive();
			} else {
				try {
					loadArchiveFrom(archiveFile);
				} catch (IOException e) {
					archive = new Archive();
					throw e;
				}
			}
		}
		return this.archive;
	}
	
	public void loadArchiveFrom(File archiveFile) throws IOException {
		this.archiveFile = archiveFile;
		archive = new Archive();
		super.loadArchiveFrom(archiveFile);
	}

	public File getArchiveFile() {
		return this.archiveFile;
	}
	
	public void setArchiveFile(File archiveFile) {
		this.archiveFile = archiveFile;
		archive = null;
	}
	
	public String getArchiveName() {
		return ( archiveFile==null )? " " : archiveFile.getName(); //$NON-NLS-1$
	}
	
	public String archiveLastModified() {
		return ( archiveFile==null )? "" :  //$NON-NLS-1$
									DateFormat.getDateInstance().format(new Date(archiveFile.lastModified()));
	}
	
	@Override
	protected void importRunnerRecord(String[] record) {
//		[0-5] Ident. base de données;Puce;Nom;Prénom;Né;S;
//		[6-9] N° club;Nom;Ville;Nat;
//		[10-15] N° cat.;Court;Long;Num1;Num2;Num3;
//		E_Mail;Texte1;Texte2;Texte3;Adr. nom;Rue;Ligne2;Code Post.;Ville;Tél.;Fax;E-mail;Id/Club;Louée
		Club club = ensureClubInArchive(safeTrimQuotes(record[7]), safeTrimQuotes(record[8]));
		Category cat = ensureCategoryInArchive(safeTrimQuotes(record[11]), safeTrimQuotes(record[12]));
		importRunner(record, club, cat);
	}

	private void importRunner(String[] record, Club club, Category cat) {
//		[0-5] Ident. base de données;Puce;Nom;Prénom;Né;S;
		ArchiveRunner runner = geco().factory().createArchiveRunner();
		runner.setArchiveId(ensureArchiveId(record[0]));
		runner.setEcard(safeTrimQuotes(record[1]));
		runner.setLastname(safeTrimQuotes(record[2]));
		runner.setFirstname(safeTrimQuotes(record[3]));
		runner.setBirthYear(safeTrimQuotes(record[4]));
		runner.setSex(safeTrimQuotes(record[5]));
		runner.setClub(club);
		runner.setCategory(cat);
		archive.addRunner(runner);
	}

	private Integer ensureArchiveId(String archiveId) {
		try {
			return new Integer(archiveId);
		} catch (NumberFormatException e) {
			return new Integer(defaultArchiveCounter++);
		}
	}


	private Club ensureClubInArchive(String shortName, String longName) {
		Club club = archive.findClub(longName);
		if( club==null ) {
			club = geco().factory().createClub();
			club.setName(longName);
			club.setShortname(shortName);
			archive.addClub(club);
		}
		return club;
	}

	private Category ensureCategoryInArchive(String shortName, String longName) {
		Category cat = archive.findCategory(shortName);
		if( cat==null ) {
			cat = geco().factory().createCategory();
			cat.setShortname(shortName);
			cat.setLongname(longName);
			archive.addCategory(cat);
		}
		return cat;
	}
	
	public Runner findAndBuildRunner(String ecard) {
		try {
			ArchiveRunner arkRunner = archive().findRunner(ecard);
			if( arkRunner == null ) {
				return null;
			}
			Category rCat = ensureCategoryInRegistry(arkRunner.getCategory());
			Course course = registry().getDefaultCourseOrAutoFor(rCat);
			return buildRunner(arkRunner, ecard, course);
		} catch (IOException e) {
			geco().log(e.toString());
			return null;
		}
	}
	
	public Runner insertRunner(ArchiveRunner arkRunner) {
		Category rCat = ensureCategoryInRegistry(arkRunner.getCategory());
		Course course = registry().getDefaultCourseOrAutoFor(rCat);
		Runner runner = buildRunner(arkRunner, arkRunner.getEcard(), course);
		runnerControl().registerNewRunner(runner);
		return runner;
	}
	
	private Category ensureCategoryInRegistry(Category category) {
		return stageControl().ensureCategoryInRegistry(category.getName(), category.getLongname());
	}

	public Runner buildRunner(ArchiveRunner arkRunner, String ecard, Course course) {
		Club club = arkRunner.getClub();
		Club rClub = stageControl().ensureClubInRegistry(club.getName(), club.getShortname());
		Category category = arkRunner.getCategory();
		Category rCat = stageControl().ensureCategoryInRegistry(category.getName(), category.getLongname());
		if( ecard.equals("") ){ //$NON-NLS-1$
			geco().log(Messages.getString("ArchiveManager.NoMatchingEcardWarning") + arkRunner.getName()); //$NON-NLS-1$
		}
		Runner runner = runnerControl().buildBasicRunner(ecard); // ensure unique ecard
		runner.setArchiveId(arkRunner.getArchiveId());
		runner.setFirstname(arkRunner.getFirstname());
		runner.setLastname(arkRunner.getLastname());
		runner.setClub(rClub);
		runner.setCategory(rCat);
		runner.setCourse(course);
		runner.setBirthYear(arkRunner.getBirthYear());
		return runner;
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
