/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.control;

import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Dec 7, 2008
 *
 */
public class PenaltyChecker extends PunchChecker {	
	
	public static class Trace implements Cloneable {
		private String code;
		private Date time;
		
		public Trace(Punch punch) {
			this(Integer.toString(punch.getCode()), punch.getTime());
		}
		
		public Trace(String code, Date time) {
			this.code = code;
			this.time = time;
		}
		
		public Trace clone() {
			try {
				Trace clone = (Trace) super.clone();
				clone.time = (Date) time.clone();
				return clone;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public String getCode() {
			return code;
		}
		public Date getTime() {
			return time;
		}
	}
	
	private long MPPenalty;

	private int MPLimit;
	
	private int nbMP;

	private Vector<Trace> trace;
	
	
	/**
	 * @param factory 
	 * @param stage 
	 * 
	 */
	public PenaltyChecker(Factory factory) {
		super(factory);
		setMPLimit(defaultMPLimit());
		setMPPenalty(defaultMPPenalty());
	}

	protected void postInitialize(Stage stage) {
		setStage(stage);
		setNewProperties();
	}
	
	
	@Override
	public void check(RunnerRaceData data) {
		super.check(data);
		data.getResult().setNbMPs(this.nbMP);
		data.getResult().setTrace(this.trace.toArray(new PenaltyChecker.Trace[0]));
	}
	
	public void buildTrace(RunnerRaceData data) {
		checkCodes(data.getCourse().getCodes(), data.getPunches());
		data.getResult().setTrace(this.trace.toArray(new Trace[0]));
	}


	/**
	 * This algorithm is able to compute an exact number of MPs for any course configuration 
	 * including butterflies, and for any MP event (jumping a control, inverting controls, missing
	 * a central control in a loop).
	 */
	@Override
	public Status checkCodes(int[] codes, Punch[] punches) {
		this.nbMP = 0;

		int[][] matrix = lcssMatrix(codes, punches);
		this.nbMP = codes.length - matrix[punches.length][codes.length];
		this.trace = trace2(codes, punches, matrix);
		return (this.nbMP <= getMPLimit()) ? Status.OK : Status.MP;
	}
	
	/**
	 * This is based on the same principles as the edit distance algorithms (Levenshtein), except it 
	 * computes the longest common (non-contiguous) subsequence rather than the edit distance (for
	 * intuitive reason). The number of MPs is then computed as the difference between the number of
	 * course codes and the length of this longest subsequence.
	 * 
	 * @param codes
	 * @param punches
	 * @return
	 */
	private int[][] lcssMatrix(int[] codes, Punch[] punches) {
		int n = codes.length;
		int m = punches.length;
		int[][] matrix = new int[m+1][n+1];
		
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix[0].length; j++) {
				int commonLength = matrix[i-1][j-1];
				if( punches[i-1].getCode() != codes[j-1] || lengthLimit(commonLength, i, j) ) {
					// codes diff, or length exceeds index potential
					matrix[i][j] = max(matrix[i-1][j-1], matrix[i-1][j], matrix[i][j-1]);
				} else {
					// longest sequence (in context) len += 1
					matrix[i][j] = commonLength + 1;
				}
			}
		}
		return matrix;
	}
	
	private boolean lengthLimit(int cLength, int i, int j) {
		return cLength > Math.min(i, j); // cLength > minimal sequence length
	}
	
	private int max(int a, int b, int c) {
		return Math.max(a, Math.max(b, c));
	}

	private void showMatrix(int[] codes, Punch[] punches, int[][] matrix) {
		String f = "%4d";
		System.out.print("\n        ");
		for (int i = 0; i < codes.length; i++) {
			System.out.format(f, codes[i]);
		}
		System.out.println();
		System.out.print("    ");

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.format(f, matrix[i][j]);				
			}
			if( i < matrix.length -1 ) {
				System.out.format("\n%4s", punches[i].getCode());
			}
		}
		System.out.println();
	}
	
	
	public String[] explainTrace(int[] codes, Punch[] punches, boolean showMatrix) {
		int[][] matrix = lcssMatrix(codes, punches);
		if( showMatrix ) {
			showMatrix(codes, punches, matrix);
			System.out.println();			
		}

		String[] path = trace(codes, punches, matrix);
		for (int i = 0; i < path.length; i++) {
			System.out.format(":%4s", path[i]);
		}
		System.out.println();
		return path;
	}
	

	public String[] trace(int[] codes, Punch[] punches, int[][] matrix) {
		StringBuffer path = new StringBuffer();
		int i = codes.length - 1;
		int j = punches.length - 1;
		
		while( i>=0 && j>=0 ) {
			if( codes[i]==punches[j].getCode() ) {
				path.insert(0, punches[j].getCode());
				i--;
				j--;
			} else {
				int max = max(matrix[j][i], matrix[j+1][i], matrix[j][i+1]);
				choice: {
					if( max==matrix[j][i] ) {
						path.insert(0, "-" + codes[i] + "+" + punches[j].getCode());
						i--;
						j--;
						break choice;
					}
					if( max==matrix[j][i+1] ) {
						path.insert(0, "+" + punches[j].getCode());
						j--;
						break choice;
					}
					if( max==matrix[j+1][i] ) {
						path.insert(0, "-" + codes[i]);
						i--;
						break choice;
					}
				}
			}
			path.insert(0, ",");
		}
		while( i>=0 ) {
			path.insert(0, ",-" + codes[i]);
			i--;			
		}
		while( j>=0 ) {
			path.insert(0, ",+" + punches[j].getCode());
			j--;
		}
		
		return path.substring(1).split(",");
	}

	public Vector<Trace> trace2(int[] codes, Punch[] punches, int[][] matrix) {
		Vector<Trace> path = new Vector<Trace>();
		int i = codes.length - 1;
		int j = punches.length - 1;
		
		while( i>=0 && j>=0 ) {
			if( codes[i]==punches[j].getCode() ) {
				path.add(0, new Trace(punches[j]));
				i--;
				j--;
			} else {
				int max = max(matrix[j][i], matrix[j+1][i], matrix[j][i+1]);
				choice: {
					if( max==matrix[j][i] ) {
						Trace t = new Trace("-" + codes[i] + "+" + punches[j].getCode(), punches[j].getTime());
						path.add(0, t);
						i--;
						j--;
						break choice;
					}
					if( max==matrix[j][i+1] ) {
						path.add(0, new Trace("+" + punches[j].getCode(), punches[j].getTime()));
						j--;
						break choice;
					}
					if( max==matrix[j+1][i] ) {
						path.add(0, new Trace("-" + codes[i], new Date(0)));
						i--;
						break choice;
					}
				}
			}
//			path.insert(0, ",");
		}
		while( i>=0 ) {
			path.add(0, new Trace("-" + codes[i], new Date(0)));
			i--;			
		}
		while( j>=0 ) {
			path.add(0, new Trace("+" + punches[j].getCode(), punches[j].getTime()));
			j--;
		}
		return path;
//		return path.substring(1).split(",");
	}

	@Override
	public long computeRaceTime(RunnerRaceData data) {
		long time = super.computeRaceTime(data);
		time += timePenalty(this.nbMP);
		return time;
	}


	public long timePenalty(int nbMPs) {
		return nbMPs * getMPPenalty();
	}
	
	public int defaultMPLimit() {
		return 0;
	}

	public long defaultMPPenalty() {
		return 0;
	}


	public long getMPPenalty() {
		return MPPenalty;
	}

	public void setMPPenalty(long penalty) {
		MPPenalty = penalty;
	}

	public int getMPLimit() {
		return MPLimit;
	}

	public void setMPLimit(int limit) {
		MPLimit = limit;
	}

	protected void setNewProperties() {
		String limit = stage().getProperties().getProperty("MPLimit");
		if( limit!=null ) {
			try {
				setMPLimit(new Integer(limit));				
			} catch (NumberFormatException e) {
				setMPLimit(defaultMPLimit());
				System.err.println(e);
			}
		} else {
			setMPLimit(defaultMPLimit());
		}
		String penalty = stage().getProperties().getProperty("MPPenalty");
		if( penalty!=null ) {
			try {
				setMPPenalty(new Long(penalty));				
			} catch (NumberFormatException e) {
				setMPPenalty(defaultMPPenalty());
				System.err.println(e);
			}
		} else {
			setMPPenalty(defaultMPPenalty());
		}		
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		super.changed(previous, next);
		setNewProperties();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		super.saving(stage, properties);
		// TODO: properties MPlimit and MPPenalty
	}
	
}
