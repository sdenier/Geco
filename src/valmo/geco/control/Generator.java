/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.martin.sireader.common.PunchObject;
import org.martin.sireader.common.ResultData;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Aug 23, 2010
 *
 */
public class Generator extends Control {

	private static final int NoTimeFactor = 100;
	private static final int TimeDispersion = 5;
	private static final int EndRange = 60;
	private static final int StartRange = 30;

	private RunnerControl runnerControl;
	private SIReaderHandler siHandler;

	private Random random;
	private int mutationX;

	private Integer[] allControls;


	public Generator(GecoControl gecoControl, RunnerControl runnerControl, SIReaderHandler siHandler) {
		super(gecoControl);
		this.runnerControl = runnerControl;
		this.siHandler = siHandler;
		this.mutationX = 40;
	}
	
	public void setMutationX(int mutationX) {
		this.mutationX = mutationX;
	}
	
	public ResultData generateRunnerData() throws RunnerCreationException {
		random = new Random();
		Runner runner = generateRunner();
		ResultData card = generateCardData(runner);
		siHandler.newCardRead(card);
		return card;
	}
	
	public Runner generateRunner() throws RunnerCreationException {
		Runner runner = runnerControl.createAnonymousRunner(randomCourse());
		runner.setLastname(runner.getLastname() + runner.getStartnumber());
		return runner;
	}
	
	public ResultData generateCardData(Runner runner) {
		return generateCardData(runner.getChipnumber(), runner.getCourse());
	}

	public ResultData generateCardData(String chipNumber, Course course) {
		ResultData card = new ResultData();
		card.setSiIdent(chipNumber);
		long startTime = siHandler.getZeroTime() + randomTime();
		card.setStartTime(startTime);
		card.setFinishTime(startTime + randomTime());
		card.setPunches(generateRandomPunchesFor(course, mutationX));
		card.evaluateTimes();
		return card;
	}

	
	public ResultData generateUnknownData() {
		random = new Random();
		ResultData cardData = generateCardData(runnerControl.newUniqueChipnumber(), randomCourse());
		siHandler.newCardRead(cardData);
		return cardData;
	}
	
	public ResultData generateOverwriting() {
		random = new Random();
		List<Runner> runnersFromCourse = registry().getRunnersFromCourse(randomCourse());
		Runner runner = runnersFromCourse.get(random.nextInt(runnersFromCourse.size()));
		ResultData cardData = generateCardData(runner);
		siHandler.newCardRead(cardData);
		return cardData;
	}


	private Course randomCourse() {
		Vector<String> courses = registry().getCoursenames();
		Course course = registry().findCourse(courses.get(random.nextInt(courses.size())));
		return course;
	}

	private long randomTime() {
		return randomTime(StartRange, EndRange, TimeDispersion, NoTimeFactor);
	}
	private long randomTime(int startRange, int endRange, float timeDis, int noTimeFreq) {
		if (random.nextInt(noTimeFreq) == 1) {
			return PunchObject.INVALID;
		}

		float meanTime = (endRange - startRange) / 2f + startRange;
		double nextGaussian = random.nextGaussian() / (meanTime / timeDis);
		int minutes = (int) ((nextGaussian + 1) * meanTime);
		try {
			return TimeManager.userParse(minutes + ":" + random.nextInt(60)).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return PunchObject.INVALID;
		}
	}

	
	private ArrayList<PunchObject> generateRandomPunchesFor(Course course, int mutationX) {
		ArrayList<PunchObject> punches = normalTrace(course.getCodes());
		int mutations = (int) randomByPowerLaw(mutationX, 0, mutationX, random);
		for (int i = 0; i < mutations; i++) {
			mutate(punches);
		}
		return punches;
	}

	private void mutate(ArrayList<PunchObject> punches) {
		int pos = random.nextInt(punches.size());
		int op = random.nextInt(10);
		if( op<5 ) {
			mutateMissingPunch(punches, pos);
			return;
		}
		if( op<7 ) {
			mutateSubsPunch(punches, pos);
			return;
		}
		if( op<9 ) {
			mutateAddPunch(punches, pos);
			return;
		}
		mutateInvertPunch(punches, pos);
	}

	private void mutateInvertPunch(ArrayList<PunchObject> punches, int pos) {
		if( pos<punches.size()-1 ) {
			PunchObject punch = punches.get(pos);
			punches.set(pos, punches.get(pos + 1));
			punches.set(pos + 1, punch);
		}
	}
	private void mutateAddPunch(ArrayList<PunchObject> punches, int pos) {
		punches.add(pos, new PunchObject(randomControl(0), PunchObject.NO_TIME));
	}
	private void mutateSubsPunch(ArrayList<PunchObject> punches, int pos) {
		punches.set(pos, new PunchObject(randomControl(punches.get(pos).getCode()), PunchObject.NO_TIME));
	}
	private void mutateMissingPunch(ArrayList<PunchObject> punches, int pos) {
		punches.remove(pos);
	}

	private int randomControl(int excludeCode) {
		if( allControls==null ) {
			if( excludeCode==0 )
				return random.nextInt(300) + 1;
			else
				return 301 - excludeCode;
		} else {
			int pos = 0;
			do {
				pos = random.nextInt(allControls.length);
			} while( allControls[pos]==excludeCode );
			return allControls[pos];
		}
	}
	
	public Generator withRegistryControls() {
		Set<Integer> controls = new HashSet<Integer>();
		for (Course c : registry().getCourses()) {
			for (int i : c.getCodes()) {
				controls.add(i);
			}
		}
		this.allControls = controls.toArray(new Integer[0]);
		return this;
	}

	private ArrayList<PunchObject> normalTrace(int[] codes) {
		ArrayList<PunchObject> punches = new ArrayList<PunchObject>(codes.length);
		for (int i = 0; i < codes.length; i++) {
			punches.add(new PunchObject(codes[i], PunchObject.NO_TIME));
		}
		return punches;
	}


	/*
	 * [(x1^(n+1) - x0^(n+1))*y + x0^(n+1)]^(1/(n+1))
	 */
	private static double randomByPowerLaw(double power, double startRange, double endRange, Random random) {
		return endRange - Math.pow(((Math.pow(endRange, power + 1) - Math.pow(startRange, power + 1)) * random.nextDouble() 
			+ Math.pow(startRange, power + 1) ), 1.0 / (power + 1));
	}
	
	
	public static void main(String[] args) {
		testRandomTime(new GecoControl("demo/belfield"));
//		displayPowerLawRandow();
//		GecoControl c = new GecoControl("demo/belfield");
//		for (int i = 0; i < 20; i++) {
//			displayOne(c);
//		}
		System.exit(0);
	}

	public static void testRandomTime(GecoControl c) {
		Generator generator = new Generator(c, new RunnerControl(c), new SIReaderHandler(c, null));
		generator.random = new Random();
		for (int i = 0; i < 20; i++) {
			long time = generator.randomTime(30, 60, 10, 100);
			System.out.println(time + " --> " + TimeManager.time(time));
		}
	}
	
	public static void displayOne(GecoControl c) {
		Generator generator = new Generator(c, new RunnerControl(c), new SIReaderHandler(c, null));
		try {
			ResultData data = generator.generateRunnerData();
			RunnerRaceData rData = c.registry().findRunnerData(data.getSiIdent());
			System.out.println(rData.infoString());
			System.out.println(rData.getResult().formatTrace());
			System.out.println();
		} catch (RunnerCreationException e) {
			e.printStackTrace();
		}
	}

	public static void displayPowerLawRandow() {
		int y0 = 0;
		int y1 = 0;
		int m = 0;
		for (int i = 0; i < 20; i++) {
			int x = (int) randomByPowerLaw(50, 0, 50, new Random());
			if( x==0 ) y0++;
			if( x==1 ) y1++;
			m = Math.max(m, x);
//			System.out.println(x);
		}
		System.out.println(y0);
		System.out.println(y1);
		System.out.println(m);
	}


}
