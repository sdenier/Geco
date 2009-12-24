/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.model.Club;
import valmo.geco.model.Factory;
import valmo.geco.model.Runner;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class StageControl extends Control {
	
	private Geco geco;
	private Announcer announcer;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer 
	 */
	public StageControl(Factory factory, Stage stage, Geco geco, Announcer announcer) {
		super(factory, stage, announcer);
		this.geco = geco;
		this.announcer = announcer;
	}
	
	private Announcer announcer() {
		return this.announcer;
	}
	
	public Club createClub() {
		Club club = factory().createClub();
		club.setName("Unknown");
		club.setShortname("");
		stage().registry().addClub(club);
		announcer().announceClubsChanged();
		return club;
	}

	public void updateName(Club club, String newName) {
		if( !club.getName().equals(newName) ) {
			registry().updateClubname(club, newName);
			announcer().announceClubsChanged();
		}
	}
	
	public void updateShortname(Club club, String newName) {
		if( !club.getShortname().equals(newName) ) {
			club.setShortname(newName);
			announcer().announceClubsChanged();
		}
	}
	
	public boolean removeClub(Club club) {
		if( canRemoveClub(club) ) {
			stage().registry().removeClub(club);
			announcer().announceClubsChanged();
			return true;
		}
		return false;
	}
	
	public boolean canRemoveClub(Club club) {
		boolean clubHasRunners = false;
		for (Runner runner : registry().getRunners()) {
			clubHasRunners |= (runner.getClub() == club);
		}
		return !clubHasRunners;
	}
	
}
