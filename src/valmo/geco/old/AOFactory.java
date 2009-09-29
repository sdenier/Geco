/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.model;

import java.sql.SQLException;

import net.java.ao.EntityManager;
import valmo.geco.model.RunnerRaceData.Punch;
import valmo.geco.model.RunnerRaceData.RunnerResult;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class AOFactory implements Factory {
	
	private EntityManager manager;

	public void initializeManager(String serverUrl, String login, String password) {
		this.manager = new EntityManager(serverUrl, login, password);
	}
	
	public EntityManager manager() {
		return this.manager;
	}

	public Category createCategory() throws SQLException {
		return manager().create(Category.class);
	}
	
	public Club createClub() throws SQLException {
		return manager().create(Club.class);
	}
	
	public Course createCourse() throws SQLException {
		return manager().create(Course.class);
	}
	
	public Event createEvent() {
		return new EventImpl();
	}
	
	public Punch createPunch() {
		return new RunnerRaceData.Punch();
	}
	
	public Race createRace() {
		return new Race();
	}
	
	public Runner createRunner() {
		return new Runner();
	}
	
	public RunnerRaceData createRunnerRaceData() {
		return new RunnerRaceData();
	}
	
	public RunnerResult createRunnerResult() {
		return new RunnerRaceData.RunnerResult();
	}
	
	public Stage createStage() {
		return new Stage();
	}
	
	public StartList createStartList() {
		return new StartList();
	}

	public Result createResult() {
		return new Result();
	}

}
