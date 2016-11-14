package app.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

public class JaccardCoefficient {

	
	 public double similarity(Integer[] x, Integer[] y) {
	        double sim=0.0d;
	        if ( (x!=null && y!=null) && (x.length>0 || y.length>0)) {
	                        sim = similarity(Arrays.asList(x), Arrays.asList(y)); 
	        } else {
	                throw new IllegalArgumentException("The arguments x and y must be not NULL and either x or y must be non-empty.");
	        }
	        return sim;
	    }
	 
	 public double similarity(int[] x, int[] y) {
	       
	        return similarity(ArrayUtils.toObject(x), ArrayUtils.toObject(y));
	    }
	    
	    private double similarity(List<Integer> x, List<Integer> y) {
	        
	        if( x.size() == 0 || y.size() == 0 ) {
	            return 0.0;
	        }
	        
	        Set<Integer> unionXY = new HashSet<Integer>(x);
	        unionXY.addAll(y);
	        
	        Set<Integer> intersectionXY = new HashSet<Integer>(x);
	        intersectionXY.retainAll(y);

	        return (double) intersectionXY.size() / (double) unionXY.size(); 
	    }
	    
}
