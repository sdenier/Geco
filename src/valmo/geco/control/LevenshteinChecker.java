/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.model.Factory;
import valmo.geco.model.Punch;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Dec 7, 2008
 *
 */
public class LevenshteinChecker extends PunchChecker {
	
	private long MPPenalty;

	private int MPLimit;
	
	private int nbMP;
	
	
	/**
	 * @param factory 
	 * @param stage 
	 * 
	 */
	public LevenshteinChecker(Factory factory) {
		super(factory);
	}

	
	/*

def pm_matrix(other)    
    a, b = self, other
    n, m = a.length, b.length
    current = []
    current[0] = [*0..n]
    1.upto(m) do |i|
      current[i] = [i]+[0]*n
    end

    1.upto(m) do |i|
      1.upto(n) do |j|
        add, delete = current[i-1][j]+0, current[i][j-1]+1
        current[i][j] = current[i-1][j-1]
        current[i][j] = [add, delete].min if a[j-1] != b[i-1]
# Levenshtein original
#         change = current[i-1][j-1]
#         change += 1 if a[j-1] != b[i-1]
#         current[i][j] = [add, delete, change].min
      end
    end
    return current
  end

	 */

	@Override
	public Status checkCodes(int[] codes, Punch[] punches) {
		this.nbMP = 0;

		int n = codes.length;
		int m = punches.length;
		int[][] matrix = new int[m+1][n+1];
		for (int j = 0; j <= n; j++) {
			matrix[0][j] = j;
		}
		for (int i = 0; i <= m; i++) {
			matrix[i][0] = i;
		}
		
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix[0].length; j++) {
				int d = ( punches[i-1].getCode() == codes[j-1] ) ? 0 : 1;
//					matrix[i][j] = matrix[i-1][j-1];	
				int add = matrix[i-1][j];
				int del = matrix[i][j-1] + d;
				int sub = matrix[i-1][j-1] + d;
				matrix[i][j] = Math.min(add, Math.min(del, sub));
			}
		}
		
		showMatrix(codes, punches, matrix);
		
		this.nbMP = matrix[m][n];
		return (this.nbMP < getMPLimit()) ? Status.OK : Status.MP;
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
		System.out.println("\n\n=========");
	}
	
	
	@Override
	public long computeRaceTime(RunnerRaceData data) {
		long time = super.computeRaceTime(data);
		time += timePenalty(this.nbMP);
		System.out.println(time);
		return time;
	}


	public long timePenalty(int nbMPs) {
		return nbMPs * getMPPenalty();
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
	
}
