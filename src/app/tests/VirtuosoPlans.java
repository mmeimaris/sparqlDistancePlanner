package app.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VirtuosoPlans {

	public static HashMap<String, ArrayList<Integer>> queryMap = new HashMap<String, ArrayList<Integer>>();
	
	static {
		queryMap.put("query1.txt", new ArrayList(Arrays.asList(new Integer[]{2,1})));
		queryMap.put("query2.txt", new ArrayList(Arrays.asList(new Integer[]{5,2,6,4,3,1})));
		queryMap.put("query3.txt", new ArrayList(Arrays.asList(new Integer[]{2,1})));
		queryMap.put("query4.txt", new ArrayList(Arrays.asList(new Integer[]{2,1,3,4,5})));
		queryMap.put("query5.txt", new ArrayList(Arrays.asList(new Integer[]{2,1})));
		queryMap.put("query6.txt", new ArrayList(Arrays.asList(new Integer[]{1})));
		queryMap.put("query7.txt", new ArrayList(Arrays.asList(new Integer[]{4,2,3,1})));
		queryMap.put("query8.txt", new ArrayList(Arrays.asList(new Integer[]{4,2,3,1,5})));
		queryMap.put("query9.txt", new ArrayList(Arrays.asList(new Integer[]{3,5,4,2,6,1})));
		queryMap.put("query10.txt", new ArrayList(Arrays.asList(new Integer[]{2,1})));
		queryMap.put("query11.txt", new ArrayList(Arrays.asList(new Integer[]{2,1})));
		queryMap.put("query12.txt", new ArrayList(Arrays.asList(new Integer[]{4,2,3,1})));
		queryMap.put("query13.txt", new ArrayList(Arrays.asList(new Integer[]{2,1})));
		queryMap.put("query14.txt", new ArrayList(Arrays.asList(new Integer[]{1})));
		
		queryMap.put("chain_star_10.txt", new ArrayList(Arrays.asList(new Integer[]{6, 4, 7, 9, 5, 8, 3, 10, 1, 2})));
		queryMap.put("chain_star_12.txt", new ArrayList(Arrays.asList(new Integer[]{7, 2, 3, 4, 5, 6, 1, 12, 11, 10, 9, 8})));
		queryMap.put("chain_star_14.txt", new ArrayList(Arrays.asList(new Integer[]{11, 10, 12, 13, 14, 4, 5, 2, 3, 7, 8, 9, 1, 6})));
		
		queryMap.put("only_star_4.txt", new ArrayList(Arrays.asList(new Integer[]{3,4,1,2})));
		queryMap.put("only_star_6.txt", new ArrayList(Arrays.asList(new Integer[]{6,3,2,1,5,4})));
		queryMap.put("only_star_8.txt", new ArrayList(Arrays.asList(new Integer[]{6, 8, 1, 2, 4, 5, 7, 3})));
		queryMap.put("only_star_10.txt", new ArrayList(Arrays.asList(new Integer[]{9, 10, 1, 2, 3, 5, 6, 7, 8, 4})));
		
		queryMap.put("only_chain_4.txt", new ArrayList(Arrays.asList(new Integer[]{4,3,1,2})));
		queryMap.put("only_chain_6.txt", new ArrayList(Arrays.asList(new Integer[]{4,5,6,1,2,3})));
		queryMap.put("only_chain_8.txt", new ArrayList(Arrays.asList(new Integer[]{4,5,6,7,8,3,1,2})));
		queryMap.put("only_chain_10.txt", new ArrayList(Arrays.asList(new Integer[]{2, 4, 3, 5, 6, 7, 8, 9, 10, 1})));
								
		queryMap.put("cyclic_4.txt", new ArrayList(Arrays.asList(new Integer[]{3,1,4,2})));
		queryMap.put("cyclic_6.txt", new ArrayList(Arrays.asList(new Integer[]{5,2,6,4,3,1})));
		queryMap.put("cyclic_8.txt", new ArrayList(Arrays.asList(new Integer[]{5,7,4,6,2,3,1,8})));
		queryMap.put("cyclic_10.txt", new ArrayList(Arrays.asList(new Integer[]{3,4,2,10,8,5,6,9,1,7})));
					
		
	}
	
	
}
