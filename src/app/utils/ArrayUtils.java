package app.utils;

public class ArrayUtils {

	
	public static void printGrid(double[][] grid)
	 {
	    for(int i = 0; i < grid.length; i++)
	    {
	       for(int j = 0; j < grid[0].length; j++)
	       {
	          System.out.printf("%5f ", grid[i][j]);
	       }
	       System.out.println();
	    }
	 }
	 
	 public static int FindSumforRow(int list[][], int rowIndex) {
		 int sum = 0;
		 int[] row = list[rowIndex];
	        for (int value : row) {
	            sum  = sum + value;  
	        }
	        return sum;
	        
	    }
	 
	 public static int FindSumforRowAbs(int list[][], int rowIndex) {
		 int sum = 0;
		 int[] row = list[rowIndex];
	        for (int value : row) {
	            sum  = sum + Math.abs(value);  
	        }
	        return sum;
	        
	    }
	 
	 public static void printGrid(int[][] grid)
	 {
	    for(int i = 0; i < grid.length; i++)
	    {
	       for(int j = 0; j < grid[0].length; j++)
	       {
	          System.out.printf("%5d ", grid[i][j]);
	       }
	       System.out.println();
	    }
	 }
	
}
