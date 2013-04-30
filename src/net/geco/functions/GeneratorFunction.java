/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.RunnerCreationException;
import net.geco.control.SIReaderHandler;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.ui.basics.SwingUtils;
import net.gecosi.dataframe.MockDataFrame;
import net.gecosi.dataframe.SiDataFrame;
import net.gecosi.dataframe.SiPunch;


/**
 * @author Simon Denier
 * @since Aug 23, 2010
 *
 */
public class GeneratorFunction extends GecoFunction {

	private static final int NoTimeFactor = 100;
	private static final int TimeDispersion = 5;
	private static final int EndRange = 60;
	private static final int StartRange = 30;

	private RunnerControl runnerControl;
	private SIReaderHandler siHandler;

	private Integer[] allControls;
	private Random random;
	private int mutationX;

	private Thread genThread;
	private JSpinner nbGeneration;
	private JSpinner genDelay;


	public GeneratorFunction(GecoControl gecoControl){
		super(gecoControl, FunctionCategory.BATCH);
		this.runnerControl = getService(RunnerControl.class);
		this.siHandler = getService(SIReaderHandler.class);
		this.mutationX = 40;
	}

	@Override
	public String toString() {
		return Messages.uiGet("GeneratorFunction.GeneratorTitle"); //$NON-NLS-1$
	}

	@Override
	public void execute() {
		if (genThread != null && genThread.isAlive()) {
			genThread.interrupt();
		} else {
			withRegistryControls();
			final int nb = ((Integer) nbGeneration.getValue()).intValue();
			final int delay = 1000 * ((Integer) genDelay.getValue()).intValue();
			genThread = new Thread(new Runnable() {
				public synchronized void run() {
					geco().log(Messages.uiGet("GeneratorFunction.GeneratingMessage1") + nb + Messages.uiGet("GeneratorFunction.GeneratingMessage2")); //$NON-NLS-1$ //$NON-NLS-2$
					try {
//						setMutationX(((Integer) mutationS.getValue()).intValue());
						for (int i = 1; i <= nb; i++) {
							if (i % 10 == 0) {
								geco().log(Integer.toString(i));
							}
							generateRunnerData();
							wait(delay);
						}
					} catch (InterruptedException e) {
					} catch (RunnerCreationException e) {
						e.printStackTrace();
					}
					geco().log(Messages.uiGet("GeneratorFunction.GeneratingStopMessage")); //$NON-NLS-1$
				}
			});
			genThread.start();
		}
	}

	@Override
	public String executeTooltip() {
		return Messages.uiGet("GeneratorFunction.ExecuteTooltip"); //$NON-NLS-1$
	}

	@Override
	public JComponent getParametersConfig() {
		
		nbGeneration = new JSpinner(new SpinnerNumberModel(10, 0, null, 5));
		nbGeneration.setToolTipText(Messages.uiGet("GeneratorFunction.GenerationNumberTooltip")); //$NON-NLS-1$
		nbGeneration.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		nbGeneration.setMaximumSize(nbGeneration.getPreferredSize());
		
		genDelay = new JSpinner(new SpinnerNumberModel(1, 0, null, 1));
		genDelay.setToolTipText(Messages.uiGet("GeneratorFunction.GenerationDelayTooltip")); //$NON-NLS-1$
		genDelay.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
		genDelay.setMaximumSize(genDelay.getPreferredSize());

		JPanel paramP = new JPanel(new GridLayout(2, 4, 5, 10));
		paramP.add(new JLabel(Messages.uiGet("GeneratorFunction.GenerationNumberLabel"))); //$NON-NLS-1$
		paramP.add(nbGeneration);
		paramP.add(new JLabel(Messages.uiGet("GeneratorFunction.GenerationDelayLabel"))); //$NON-NLS-1$
		paramP.add(genDelay);
		
//		final JSpinner mutationS = new JSpinner(new SpinnerNumberModel(40, 0, null, 5));
//		mutationS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
//		mutationS.setToolTipText("Mutation factor");
		
		JButton cUnknownB = new JButton(Messages.uiGet("GeneratorFunction.CreateUnknownLabel")); //$NON-NLS-1$
		cUnknownB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateUnknownData();
			}
		});
		JButton cOverwriteB = new JButton(Messages.uiGet("GeneratorFunction.CreateOverwritingLabel")); //$NON-NLS-1$
		cOverwriteB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateOverwriting();
			}
		});
		
		Box vBoxButtons = Box.createVerticalBox();
		vBoxButtons.add(cUnknownB);
		vBoxButtons.add(cOverwriteB);

		paramP.setMaximumSize(paramP.getPreferredSize());
		paramP.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		paramP.setAlignmentY(Component.TOP_ALIGNMENT);
		vBoxButtons.setAlignmentY(Component.TOP_ALIGNMENT);
		Box hBox = Box.createHorizontalBox();
		hBox.add(paramP);
		hBox.add(Box.createHorizontalStrut(50));
		hBox.add(vBoxButtons);
		
		return hBox;
	}

	public void setMutationX(int mutationX) {
		this.mutationX = mutationX;
	}
	
	public SiDataFrame generateRunnerData() throws RunnerCreationException {
		random = new Random();
		Runner runner = generateRunner();
		SiDataFrame card = generateCardData(runner);
		siHandler.handleEcard(card);
		return card;
	}
	
	public Runner generateRunner() throws RunnerCreationException {
		Runner runner = runnerControl.createAnonymousRunner(randomCourse());
		runnerControl.validateEcard(runner, runnerControl.newUniqueEcard());
		runner.setLastname(runner.getLastname() + runner.getStartId());
		return runner;
	}
	
	public SiDataFrame generateCardData(Runner runner) {
		return generateCardData(runner.getEcard(), runner.getCourse());
	}

	public SiDataFrame generateCardData(String chipNumber, Course course) {
		long checkTime = SiDataFrame.NO_TIME;
		long startTime = stage().getZeroHour() + randomTime();
		long finishTime = startTime + randomTime();
		SiDataFrame card = new MockDataFrame(chipNumber, checkTime, startTime, finishTime, generateRandomPunchesFor(course, mutationX));
		return card;
	}

	
	public SiDataFrame generateUnknownData() {
		random = new Random();
		withRegistryControls();
		SiDataFrame cardData = generateCardData(runnerControl.newUniqueEcard(), randomCourse());
		siHandler.handleEcard(cardData);
		return cardData;
	}
	
	public SiDataFrame generateOverwriting() {
		random = new Random();
		withRegistryControls();
		List<Runner> runnersFromCourse = registry().getRunnersFromCourse(randomCourse());
		if( ! runnersFromCourse.isEmpty() ){
			Runner runner = runnersFromCourse.get(random.nextInt(runnersFromCourse.size()));
			SiDataFrame cardData = generateCardData(runner);
			siHandler.handleEcard(cardData);
			return cardData;
		}
		return null;
	}


	private Course randomCourse() {
		List<String> courses = registry().getCourseNames();
		Course course = registry().findCourse(courses.get(random.nextInt(courses.size())));
		return course;
	}

	private long randomTime() {
		return randomTime(StartRange, EndRange, TimeDispersion, NoTimeFactor);
	}
	private long randomTime(int startRange, int endRange, float timeDis, int noTimeFreq) {
		if (random.nextInt(noTimeFreq) == 1) {
			return SiDataFrame.NO_TIME;
		}

		float meanTime = (endRange - startRange) / 2f + startRange;
		double nextGaussian = random.nextGaussian() / (meanTime / timeDis);
		int minutes = (int) ((nextGaussian + 1) * meanTime);
		try {
			return TimeManager.userParse(minutes + ":" + random.nextInt(60)).getTime(); //$NON-NLS-1$
		} catch (ParseException e) {
			e.printStackTrace();
			return SiDataFrame.NO_TIME;
		}
	}

	
	private SiPunch[] generateRandomPunchesFor(Course course, int mutationX) {
		List<SiPunch> punches = normalTrace(course.getCodes());
		int mutations = (int) randomByPowerLaw(mutationX, 0, mutationX, random);
		for (int i = 0; i < mutations; i++) {
			mutate(punches);
		}
		return punches.toArray(new SiPunch[0]);
	}

	private void mutate(List<SiPunch> punches) {
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

	private void mutateInvertPunch(List<SiPunch> punches, int pos) {
		if( pos < punches.size()-1 ) {
			SiPunch punch = punches.get(pos);
			punches.set(pos, punches.get(pos + 1));
			punches.set(pos + 1, punch);
		}
	}
	private void mutateAddPunch(List<SiPunch> punches, int pos) {
		punches.add(pos, new SiPunch(randomControl(0), SiDataFrame.NO_TIME));
	}
	private void mutateSubsPunch(List<SiPunch> punches, int pos) {
		punches.set(pos, new SiPunch(randomControl(punches.get(pos).code()), SiDataFrame.NO_TIME));
	}
	private void mutateMissingPunch(List<SiPunch> punches, int pos) {
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
	
	public GeneratorFunction withRegistryControls() {
		Set<Integer> controls = new HashSet<Integer>();
		for (Course c : registry().getCourses()) {
			for (int i : c.getCodes()) {
				controls.add(i);
			}
		}
		this.allControls = controls.toArray(new Integer[0]);
		return this;
	}

	private List<SiPunch> normalTrace(int[] codes) {
		List<SiPunch> punches = new ArrayList<SiPunch>(codes.length);
		for (int i = 0; i < codes.length; i++) {
			punches.add(new SiPunch(codes[i], SiDataFrame.NO_TIME));
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
	
	
//	public static void main(String[] args) {
//		testRandomTime(new GecoControl("demo/belfield"));
//		displayPowerLawRandow();
//		GecoControl c = new GecoControl("demo/belfield");
//		for (int i = 0; i < 20; i++) {
//			displayOne(c);
//		}
//		System.exit(0);
//	}

//	public static void testRandomTime(GecoControl c) {
//		Generator generator = new Generator(c, new RunnerControl(c), new SIReaderHandler(c, null));
//		generator.random = new Random();
//		for (int i = 0; i < 20; i++) {
//			long time = generator.randomTime(30, 60, 10, 100);
//			System.out.println(time + " --> " + TimeManager.time(time));
//		}
//	}
	
//	public static void displayOne(GecoControl c) {
//		Generator generator = new Generator(c, new RunnerControl(c), new SIReaderHandler(c, null));
//		try {
//			SiDataFrame data = generator.generateRunnerData();
//			RunnerRaceData rData = c.registry().findRunnerData(data.getSiIdent());
//			System.out.println(rData.infoString());
//			System.out.println(rData.getResult().formatTrace());
//			System.out.println();
//		} catch (RunnerCreationException e) {
//			e.printStackTrace();
//		}
//	}

//	public static void displayPowerLawRandow() {
//		int y0 = 0;
//		int y1 = 0;
//		int m = 0;
//		for (int i = 0; i < 20; i++) {
//			int x = (int) randomByPowerLaw(50, 0, 50, new Random());
//			if( x==0 ) y0++;
//			if( x==1 ) y1++;
//			m = Math.max(m, x);
//			System.out.println(x);
//		}
//		System.out.println(y0);
//		System.out.println(y1);
//		System.out.println(m);
//	}

}
