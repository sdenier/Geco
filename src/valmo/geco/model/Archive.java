/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class Archive {
	
	private Map<String, Club> clubs;

	private Map<String, Category> categories;
	
	private Map<Integer, ArchiveRunner> runnersById;
	
	private Map<String, ArchiveRunner> runnersByECard;

	
	public Archive() {
		clubs = new HashMap<String, Club>();
		categories = new HashMap<String, Category>();
		runnersById = new HashMap<Integer, ArchiveRunner>();
		runnersByECard = new HashMap<String, ArchiveRunner>();
	}
	
	public void addClub(Club club) {
		clubs.put(club.getName(), club);
	}
	
	public Club findClub(String name) {
		return clubs.get(name);
	}
	
	public Collection<Club> clubs() {
		return clubs.values();
	}
	
	public void addCategory(Category cat) {
		categories.put(cat.getName(), cat);
	}
	
	public Category findCategory(String name) {
		return categories.get(name);
	}
	
	public Collection<Category> categories() {
		return categories.values();
	}
	
	public void addRunner(ArchiveRunner runner) {
		runnersById.put(runner.getArchiveId(), runner);
		String ecard = runner.getChipnumber();
		if( !ecard.isEmpty() ) {
			runnersByECard.put(ecard, runner);
		}
	}
	
	public ArchiveRunner findRunner(String ecard) {
		return runnersByECard.get(ecard);
	}
	
	public Collection<ArchiveRunner> runners() {
		return runnersById.values();
	}
	
}
