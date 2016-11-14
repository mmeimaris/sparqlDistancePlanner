package app.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import app.utils.ArrayUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class MyPlanner {

	Query query ;
	Model model ; 
	ElementPathBlock triplePathBlock ;
	public HashMap<Triple, Integer> tripleIndex = new HashMap<>();
	public HashMap<Integer, Triple> reverseTripleIndex = new HashMap<>();
	public HashMap<LinkedHashSet<Integer>, HashSet<Node>> existingNodes = new HashMap<>();
	public HashMap<Node, HashSet<Triple>> nodeTripleIndex = new HashMap<>();
	public HashMap<Node, Integer> nodeIndex;
	public HashMap<Node, Integer> existingNodesCount = new HashMap<Node, Integer>();
	int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
	double[] d = null;
	int[][] map;
	Integer[] sorted;
	boolean opt_chain = true;
	double pushUpParameter = 0.0001;
	//[2, 4, 3, 1, 8, 6, 5, 7]
	public MyPlanner(Query query, Model model){
		
		this.query = query ;
		
		this.model = model ;
		
	}
	
	public int[][] buildQueryMap() throws Exception {
			
		Element pattern = query.getQueryPattern();
		
		ElementGroup g = (ElementGroup) pattern ;
		
		triplePathBlock = (ElementPathBlock) g.getElements().get(0);
		
		long varCard = model.size();
		
		nodeIndex = new HashMap<>();
		
		int nextIndex = 0, nextNodeIndex = 0;

		for(TriplePath triplePath : triplePathBlock.getPattern().getList()){
			
			Triple triple = triplePath.asTriple();	
			//System.out.println("\nTriple: " + triple);
			HashSet<Triple> tripleSetA = new HashSet<Triple>();
			tripleSetA.add(triple);			
			if(nodeTripleIndex.containsKey(triple.getSubject())) {
				HashSet<Triple> newSet = new HashSet<Triple>();
				newSet.addAll(nodeTripleIndex.get(triple.getSubject()));
				newSet.add(triple);
				nodeTripleIndex.put(triple.getSubject(), newSet);
				//nodeTripleIndex.get(triple.getSubject()).add(triple);
			}
							
			else nodeTripleIndex.put(triple.getSubject(), tripleSetA);
			
			if(nodeTripleIndex.containsKey(triple.getPredicate())) 
			{
				HashSet<Triple> newSet = new HashSet<Triple>();
				newSet.addAll(nodeTripleIndex.get(triple.getPredicate()));
				newSet.add(triple);
				nodeTripleIndex.put(triple.getPredicate(), newSet);
				//nodeTripleIndex.get(triple.getPredicate()).add(triple);
			}
							
			else nodeTripleIndex.put(triple.getPredicate(), tripleSetA);
			
			if(nodeTripleIndex.containsKey(triple.getObject())) 
			{
				HashSet<Triple> newSet = new HashSet<Triple>();
				newSet.addAll(nodeTripleIndex.get(triple.getObject()));
				newSet.add(triple);
				nodeTripleIndex.put(triple.getObject(), newSet);
				//nodeTripleIndex.get(triple.getObject()).add(triple);
			}
				
			else nodeTripleIndex.put(triple.getObject(), tripleSetA);
			
			//System.out.println("Node triple index:" + nodeTripleIndex);
			tripleIndex.put(triple, nextIndex);
			reverseTripleIndex.put(nextIndex, triple);
			existingNodesCount = addVariableNodes(existingNodesCount, triple);
			nextIndex++;
			if(!nodeIndex.containsKey(triple.getSubject()))
				nodeIndex.put(triple.getSubject(), nextNodeIndex++);
			
			if(!nodeIndex.containsKey(triple.getPredicate()))
				nodeIndex.put(triple.getPredicate(), nextNodeIndex++);
			
			if(!nodeIndex.containsKey(triple.getObject()))
				nodeIndex.put(triple.getObject(), nextNodeIndex++);
			
		}
		
		
	
		int[][] map = new int[tripleIndex.size()][nodeIndex.size()];
		//if(opt_chain){
			for(int i = 0 ; i < map.length ; i++)
				for(int j = 0 ; j < map[0].length ; j++)
					//map[i][j] = (int) varCard*map[0].length;
					map[i][j] = (int) ((int) -1*varCard);
					//map[i][j] = 0 ;
		//}
		//varCard = 1;
		
		for(Triple triple : tripleIndex.keySet()){
			
			Node subject = triple.getSubject();
			
			Node predicate = triple.getPredicate();
			
			Node object = triple.getObject();
			
			if(subject.isURI() && predicate.isURI() && object.isURI()) {}
			
			int countUnbound = 1;
			/*if(subject.isVariable())
				countUnbound++;
			if(predicate.isVariable())
				countUnbound++;
			if(object.isVariable())
				countUnbound++;*/
			
			if(subject.isVariable()) {
								
				map[tripleIndex.get(triple)][nodeIndex.get(subject)] = (int) (long) varCard*countUnbound;
			}
			
			if(predicate.isVariable()) {
								
				map[tripleIndex.get(triple)][nodeIndex.get(predicate)] = (int) (long) varCard*countUnbound;
			}
			
			if(object.isVariable()) {
								
				map[tripleIndex.get(triple)][nodeIndex.get(object)] = (int) (long) varCard*countUnbound;
			}
			
			if(predicate.isURI() ) {
				
				Query qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?x <"+predicate.getURI()+"> ?y }");

				if(object.isURI()){
					if(subject.isURI()){
						qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { <"+subject.getURI()+"> <"+predicate.getURI()+"> <"+object.getURI()+">  }");
					}
					else
						qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?x <"+predicate.getURI()+"> <"+object.getURI()+">  }");					
				}
				else if(object.isLiteral()){
					if(subject.isURI()){
						qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { <"+subject.getURI()+"> <"+predicate.getURI()+"> <"+object.getLiteral()+">  }");
					}
					else
						qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?x <"+predicate.getURI()+"> \""+object.getLiteral()+"\"  }");					
				}
				else {
					if(subject.isURI()){
						qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { <"+subject.getURI()+"> <"+predicate.getURI()+"> ?y  }");
					}
					else
						qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?x <"+predicate.getURI()+"> ?y }");
				}
				
				QueryExecution qe = QueryExecutionFactory.create(qry, model);
				
			    ResultSet rs = qe.execSelect();
			    
			    while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			        
			        map[tripleIndex.get(triple)][nodeIndex.get(predicate)] = sol.getLiteral("count").getInt()*countUnbound;
			       /* if(subject.isVariable()) 
			        	map[tripleIndex.get(triple)][nodeIndex.get(subject)] = (int) (varCard / sol.getLiteral("count").getInt()*countUnbound);
			        if(object.isVariable())
			        map[tripleIndex.get(triple)][nodeIndex.get(object)] = (int) (varCard / sol.getLiteral("count").getInt()*countUnbound);*/
			        //map[tripleIndex.get(triple)][nodeIndex.get(predicate)] = (int) ((int)-1*varCard);
			        
			    }
			    if(!object.isURI() ){
			    	if(!predicate.getURI().contains("type")){
			    		qry = QueryFactory.create("ASK { ?x <"+predicate.getURI()+"> ?y1 ; "
				    			+ "<"+predicate.getURI()+"> ?y2 "
				    					+ "FILTER(?y1!=?y2) }");
						
						qe = QueryExecutionFactory.create(qry, model);
						
					    if(qe.execAsk()){
					        map[tripleIndex.get(triple)][nodeIndex.get(predicate)] *= tripleIndex.size()/2;//Math.log(2*tripleIndex.size())/2;//tripleIndex.size()/2;//
					        //System.out.println(triple.toString());
					        //System.out.println(Math.log(tripleIndex.size()));
					        //map[tripleIndex.get(triple)][nodeIndex.get(predicate)] = (int) ((int)-1*varCard);
					        //System.out.println(triple.toString());
					    }
			    	}
			    	
			    }
			    			    			   
			    qe.close();	
				
			}			
			
			if(subject.isURI()) {
				
				Query qry ;

				qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { <"+subject.getURI()+"> ?y ?z }");
				
				QueryExecution qe = QueryExecutionFactory.create(qry, model);
			
			    ResultSet rs = qe.execSelect();
			
			    while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			        map[tripleIndex.get(triple)][nodeIndex.get(subject)] = sol.getLiteral("count").getInt()*countUnbound;
			        //map[tripleIndex.get(triple)][nodeIndex.get(subject)] = Math.min(map[tripleIndex.get(triple)][nodeIndex.get(predicate)], sol.getLiteral("count").getInt()*countUnbound);// 
			        //map[tripleIndex.get(triple)][nodeIndex.get(predicate)] = Math.min(map[tripleIndex.get(triple)][nodeIndex.get(predicate)], sol.getLiteral("count").getInt()*countUnbound);
			        
			    }
			    qe.close();	
			}
			
			if(object.isURI()) {
				
				Query qry ;

				qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?y ?z <"+object.getURI()+"> }");
				
				QueryExecution qe = QueryExecutionFactory.create(qry, model);
				
			    ResultSet rs = qe.execSelect();

			    while(rs.hasNext())
			    {
			        QuerySolution sol = rs.nextSolution();
			    
			        map[tripleIndex.get(triple)][nodeIndex.get(object)] = sol.getLiteral("count").getInt()*countUnbound;
			        //map[tripleIndex.get(triple)][nodeIndex.get(object)] = Math.min(map[tripleIndex.get(triple)][nodeIndex.get(predicate)], sol.getLiteral("count").getInt()*countUnbound);
			        //map[tripleIndex.get(triple)][nodeIndex.get(predicate)] = Math.min(map[tripleIndex.get(triple)][nodeIndex.get(predicate)], sol.getLiteral("count").getInt()*countUnbound);
			        
			    }
			    qe.close();	
			}
			/*if(predicate.isURI() && object.isURI()){
				System.out.println(triple.toString());
				System.out.println(map[tripleIndex.get(triple)][nodeIndex.get(subject)]);
				System.out.println(map[tripleIndex.get(triple)][nodeIndex.get(predicate)]);
				System.out.println(map[tripleIndex.get(triple)][nodeIndex.get(object)]);
			}*/
		}
		
		return map;
		
	}
	
	public Op getPlan() throws Exception{
				
		
		//ArrayUtils.printGrid(map);
		
		d = this.distanceMap(map);
		
		final Integer[] idx = new Integer[d.length];
		
		for(int i = 0 ; i < idx.length ; i++) {
			idx[i] = i;
		}
		
		Arrays.sort(idx, new Comparator<Integer>() {
		    @Override public int compare(final Integer o1, final Integer o2) {
		        return Double.compare(d[o1], d[o2]);
		    }
		});
		
		sorted = new Integer[(int) ((idx.length-Math.sqrt(idx.length))/2)]; 
		
		for(int i = 0; i < (idx.length-Math.sqrt(idx.length))/2 ; i++) {
			sorted[i] = idx[i];
		}
						
		 LinkedHashSet<LinkedHashSet<Integer>> sets = sortIndexes();
		
		 LinkedHashSet<Integer> finalSet = new LinkedHashSet<>() ;
		 
		 LinkedHashSet<Integer> previousSet = null;
		 
		 System.out.println("sorted sets: " + sets.toString());
		 
		 boolean notFunny = false;
		 
		 ArrayList<LinkedHashSet<Integer>> sets11 = new ArrayList<LinkedHashSet<Integer>>();
		 for(LinkedHashSet<Integer> set : sets){

			 LinkedHashSet<Integer> set1 = new LinkedHashSet<Integer>();
			 set1.addAll(set);
			 sets11.add(set1);
		 }
		 ArrayList<LinkedHashSet<Integer>> sets112 = new ArrayList<LinkedHashSet<Integer>>();
		 boolean sets112changed = false;
		 for(LinkedHashSet<Integer> set : sets11){
			 for(Integer i : set){
				 if(map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getPredicate())] < pushUpParameter*model.size() ){
					 sets112.add(set);
					 sets112changed = true;
					 break;
				 }
			 }			 			 
		 }
		 for(LinkedHashSet<Integer> set : sets11){
			 if(!sets112.contains(set))
				 sets112.add(set);
		 }		 
		 if(!sets112.isEmpty() && sets112changed ) {System.out.println("sets 112: " + sets112.toString());sets.clear(); sets.addAll(sets112);}
		 else{
			 ArrayList<LinkedHashSet<Integer>> sets1122 = new ArrayList<LinkedHashSet<Integer>>();
			 HashMap<Long, LinkedHashSet<Integer>> costMap = new HashMap<Long, LinkedHashSet<Integer>>();
			 ArrayList<Long> costs = new ArrayList<Long>();
			 for(LinkedHashSet<Integer> set : sets11){			 
				 long cost = costOfSet(set);
				 costMap.put(cost, set);
				 costs.add(cost);
			 }
			 Collections.sort(costs);
			 
			 System.out.println(costMap.toString());
			 for(Long cost : costs){			 
					 sets1122.add(costMap.get(cost));
			 }		 
			 if(!sets1122.isEmpty() ) {System.out.println("sets 1122: " + sets1122.toString());sets.clear(); sets.addAll(sets1122);}
			 //[5, 4, 8, 9, 7, 2, 3, 6, 1, 14, 13, 12, 11, 10]
			 
			 //oi malakies ksekinoun apo edw
			 
			 int ir = 0;
			 HashMap<Long, LinkedHashSet<Integer>> costMap2 = new HashMap<Long, LinkedHashSet<Integer>>();
			 ArrayList<Long> costs2 = new ArrayList<Long>();
			 while(sets1122.size() > 2) {
				 LinkedHashSet<Integer> set = sets1122.get(ir);
				 System.out.println("set 1: " + set.toString()); 
				 int j = ir+1;				 
				 LinkedHashSet<Integer> set2 = sets1122.get(j);
				 System.out.println("set 2: " + set2.toString());
				 long newCost = Math.min(costOfSet(set), costOfSet(set2));
				 
				 existingNodes.clear();
				 existingNodes = addVariableNodes(existingNodes, set);	
				 existingNodes = addVariableNodes(existingNodes, set2);	
				 ArrayList<Integer> asList = new ArrayList<Integer>( prioritizeSet(set2, set)    );	
				 set2.clear();
				 set2.addAll(asList);
				 System.out.println("new set 2: " + set2.toString());
				 sets1122.remove(ir);						 			
				 costMap2.put(newCost, set2);
				 costs2.add(newCost);
				 ir++;
			}
				 
			 System.out.println("sets 1122: " + sets1122.toString());
			 ArrayList<LinkedHashSet<Integer>> sets11222 = new ArrayList<LinkedHashSet<Integer>>();
			 
			 for(LinkedHashSet<Integer> set : sets1122){
				 if(costMap2.containsValue(set)) continue;
				 long cost = costOfSet(set);
				 costMap2.put(cost, set);
				 costs2.add(cost);
			 }
			 Collections.sort(costs2);
			 System.out.println(costMap2.toString());
			 for(Long cost : costs2){			 
					 sets11222.add(costMap2.get(cost));
			 }	
			 if(!sets11222.isEmpty() && sets11222.size() < 4) {System.out.println("sets 11222: " + sets11222.toString());sets.clear(); sets.addAll(sets11222);}
			 //oi malakies teleiwnoun edw
		 }
		 
			 
		 ArrayList<LinkedHashSet<Integer>> sets1 = new ArrayList<LinkedHashSet<Integer>>();
		 for(LinkedHashSet<Integer> set : sets){

			 LinkedHashSet<Integer> set1 = new LinkedHashSet<Integer>();
			 set1.addAll(set);
			 sets1.add(set1);
			 
			 //System.out.println("next set: " + set.toString());
			 //System.out.println("previous set: " + previousSet);
			 existingNodes = addVariableNodes(existingNodes, set);				
			 
			 if(previousSet != null ) { //
				 if(!previousSet.isEmpty()){
					 ArrayList<Integer> asList = new ArrayList<Integer>(prioritizeSet(set, previousSet));					 
					 finalSet.addAll(asList);
				 }
				 else{					
					 notFunny = true;
					 break;
					 
				 }
								 			 				
			 }			 			
			 previousSet = set;
			 
		 }
		 //notFunny = true;
		 
		 if(sets.size() > 2) notFunny = true;
		 if(notFunny){		
			 finalSet.clear();
			 ArrayList<LinkedHashSet<Integer>> sets2 = new ArrayList<LinkedHashSet<Integer>>();
			 sets2.addAll(sets1);
			
			 int extcount = 0;
			 LinkedHashSet<Integer> last = new LinkedHashSet<Integer>();
			 boolean changed = true;
			 //for(LinkedHashSet<Integer> set : sets1){
			 while(changed){			 
				 
				 changed = false;
				 LinkedHashSet<Integer> set = sets1.get(extcount);
				 existingNodes.clear();
				 existingNodes = addVariableNodes(existingNodes, set);
				 //System.out.println(extcount);
				 int intcount = 0;
				 //System.out.println("outer set: " + set);
				 for(LinkedHashSet<Integer> set2 : sets2){
					existingNodes = addVariableNodes(existingNodes, set2);
					if(intcount==extcount) {
						
						intcount++;
						continue;
						
					}
					
					if(areJoinedAnywhere(set, set2)){
						
						changed = true;
						
						ArrayList<Integer> asList = new ArrayList<Integer>( prioritizeSet(set2, set) );
						
						last.addAll(asList);
						
						if(!finalSet.isEmpty()){
							existingNodes.clear();
							existingNodes = addVariableNodes(existingNodes, last);
							existingNodes = addVariableNodes(existingNodes, finalSet);
							asList = new ArrayList<Integer>( prioritizeSet(last, finalSet) );
						
						}
						
						finalSet.addAll(asList);
											
					}
					
					intcount++;
				 }
				 if(!changed) extcount++;
			 }
			 
		 }
		// System.out.println(sets.toString());
		 if(finalSet.isEmpty()){
			 for(LinkedHashSet<Integer> set : sets){
				 finalSet.addAll(set);
			 }
		 }
		 if(previousSet!=null && notFunny == false){
			 ArrayList<Integer> asList = new ArrayList<Integer>(previousSet);
			 finalSet.addAll(asList);
		 }
		 System.out.println("final set: " + finalSet.toString());	
		 
		 //HashMap<Integer, Triple> reverseTripleIndex = this.reverseTripleIndex;
		 
		 
		 //finalSet.clear();
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{1,2,5,6,4,3}))); 
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{3,2,1}))); //virtuoso sp 1
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{5,2,1,7,3,6,8,9,4}))); //virtuoso sp 2
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{1,2}))); //virtuoso sp 3a
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{2,1}))); //virtuoso sp 3b
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{2,1}))); //virtuoso sp 3c
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{5, 6, 4, 3, 2, 1}))); //virtuoso sp 5a		 
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{2,1,4,3,5}))); //virtuoso sp 5b
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{1,2,4,5,3}))); //virtuoso sp 6a
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{3, 4, 2,10,8,5,6, 9, 1, 7}))); //
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{3,4,2,10,8,5,6,9,1,7}))); //virtuoso cyclic 10		 
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{7, 5, 4, 6, 1, 8, 3, 2}))); //7, 5, 6, 1, 8, 3, 4, 2
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{5,7,4,6,2,3,1,8}))); //virtuoso cyclic 8
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{5,2,6,4,3,1}))); //virtuoso cyclic 6
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{3,1,4,2}))); //virtuoso cyclic 4
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{7, 2, 3, 4, 5, 6, 1, 12, 11, 10, 9, 8}))); //virtuoso chainstar 12
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{6, 4, 7, 9, 5, 8, 3, 10, 1, 2}))); //virtuoso chainstar 10
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{11, 10, 12, 13, 14, 4, 5, 2, 3, 7, 8, 9, 1, 6}))); //virtuoso chainstar 14		 
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{3,4,1,2}))); //virtuoso only star 4
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{6, 3,2,1,5,4}))); //virtuoso only star 6
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{6, 8, 1, 2, 4, 5, 7, 3}))); //virtuoso only star 8		 
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{9, 10, 1, 2, 3, 5, 6, 7, 8, 4}))); //virtuoso only star 10
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{10, 9, 5, 2, 3, 1, 6, 7, 8, 4 }))); //only star 10 best ever?
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{4,5,6,7,8,3,1,2}))); //virtuoso only chain 8		 
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{4,3,1,2}))); //virtuoso only chain 4
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{2, 4, 3, 5, 6, 7, 8, 9, 10, 1}))); //virtuoso only chain 10
		 //finalSet.addAll(new ArrayList(Arrays.asList(new Integer[]{4,5,6,1,2,3}))); //virtuoso only chain 6
		 
		 LinkedHashSet<Integer> newfinal = pushUp(finalSet);
		 		 		 
		 Op newOp = new OpBGP(createBP(newfinal, finalSet)) ;
		 
		 return newOp;
	}
	
	private LinkedHashSet<Integer> pushUp(LinkedHashSet<Integer> finalSet){
		
		LinkedHashSet<Integer> newfinal = new LinkedHashSet<Integer>();		 
		 for(Integer i : finalSet){			 
			 if(Math.min(Math.min(map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getSubject())], map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getObject())]), map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getPredicate())]) < pushUpParameter*model.size() ){
				 newfinal.add(i);					
			 }
		 }
		 
		 
		return newfinal;
	}
	
	private BasicPattern createBP(LinkedHashSet<Integer> newfinal, LinkedHashSet<Integer> finalSet){
		
		BasicPattern newBp = new BasicPattern();
		 System.out.println("final set: " + newfinal.toString());	
		 for(Integer i : newfinal){
			 newBp.add(reverseTripleIndex.get(i-1));
			 //System.out.println(i);
		 }		 
		 
		 for(Integer i : finalSet){
			if(!newfinal.contains(i)){ 		
			  newBp.add(reverseTripleIndex.get(i-1));
			  //System.out.println(i);
			}
	     }
		 	
		 //if no optimization took place, maintain original order
		 if(finalSet.isEmpty()){			 		 
			 for(TriplePath triplePath : triplePathBlock.getPattern().getList()){
					
					Triple triple = triplePath.asTriple();			
					newBp.add(triple);
			 }
		 }
		 
		 return newBp;
	}
	
	private LinkedHashSet<Integer> prioritizeSet(LinkedHashSet<Integer> set, LinkedHashSet<Integer> previousSet) {
		
		
		HashSet<Node> theSet = existingNodes.get(set); //the nodes in the current set
		
		HashSet<Node> thePrevious = existingNodes.get(previousSet); //the nodes in the previous set
		
		HashSet<Node> priorities = new HashSet<>();
		
		for(Node node : theSet){
			//System.out.println("Node: " + node.toString());
			if(thePrevious.contains(node)){
				priorities.add(node);
				//System.out.println("adding node: " + node.toString());
			}
		}
		//System.out.println("Priorities: " + priorities.toString());
		//LinkedHashSet<Integer> thePrioritySet = new LinkedHashSet<>();	
		for(Node node : priorities){
			HashSet<Triple> tripleSet = nodeTripleIndex.get(node);
			//System.out.println("Node: " + node + ", triples: " + tripleSet.toString());
			for(Triple triple : tripleSet){
				if(!previousSet.isEmpty()){
					
					if(!set.contains(tripleIndex.get(triple)+1)) {						
						continue;
					}
					set.remove(tripleIndex.get(triple)+1);					
					previousSet.add(tripleIndex.get(triple)+1);
					/*System.out.println("---------------------------------------------");
					System.out.println("set: " + set.toString());
					System.out.println("previous set: " + previousSet.toString());
					System.out.println("---------------------------------------------");*/
				}
				
			}
			
			existingNodes.clear();
			existingNodes = addVariableNodes(existingNodes, set);
			existingNodes = addVariableNodes(existingNodes, previousSet);
			
			if(set.isEmpty()) break;			
			if(!areJoined(set, previousSet))
				prioritizeSet(set, previousSet);
		}
		//add remaining elements (that are not joined with previous set)
		previousSet.addAll(set);
		return previousSet;
	}

	private boolean areJoined(LinkedHashSet<Integer> set,
			LinkedHashSet<Integer> previousSet) {
		
		List<Integer> setList = new ArrayList<Integer>(set);
		List<Integer> previousSetList = new ArrayList<Integer>(previousSet);
		
		Triple t1 = reverseTripleIndex.get(previousSetList.get(previousSetList.size()-1)-1);
		Triple t2 = reverseTripleIndex.get(setList.get(0)-1);		
		if(t1.getSubject().equals(t2.getSubject())) return true;
		if(t1.getSubject().equals(t2.getObject())) return true;
		if(t2.getSubject().equals(t1.getSubject())) return true;
		if(t2.getSubject().equals(t1.getObject())) return true;
		if(t2.getObject().equals(t1.getObject())) return true;
		return false;
	}
	
	private boolean areJoinedAnywhere(LinkedHashSet<Integer> set,
			LinkedHashSet<Integer> previousSet) {
		
		List<Integer> setList = new ArrayList<Integer>(set);
		List<Integer> previousSetList = new ArrayList<Integer>(previousSet);
		//int extcount = 0;
		for(Integer setListIndex : setList){
			//int intcount = 0;
			for(Integer previousSetListIndex : previousSetList){
				
				Triple t1 = reverseTripleIndex.get(previousSetListIndex-1);
				Triple t2 = reverseTripleIndex.get(setListIndex-1);		
				if(t1.getSubject().equals(t2.getSubject())) return true;
				if(t1.getSubject().equals(t2.getObject())) return true;
				if(t2.getObject().equals(t1.getObject())) return true;
				if(t2.getSubject().equals(t1.getObject())) return true;
				//intcount++;
			}
			//extcount++;
		}
		
		return false;
	}

	public static double distance(int[] point1, int[] point2) throws Exception {
	    return Math.sqrt(distance2(point1, point2));
	  }
	
	 private static double distance2(int[] point1, int[] point2) throws Exception {
		    if (point1.length == point2.length) {
		      Double sum = 0D;
		      for (int i = 0; i < point1.length; i++) {
		        sum = sum
		            + (Integer.valueOf(point2[i]).doubleValue() - Integer.valueOf(point1[i]).doubleValue())
		            * (Integer.valueOf(point2[i]).doubleValue() - Integer.valueOf(point1[i]).doubleValue());
		      }
		      return sum;
		    }
		    else {
		      throw new Exception("Exception in Euclidean distance: array lengths are not equal");
		    }
		  }
	
	public double[] distanceMap(int[][] map) throws Exception{
		
		//JaccardCoefficient jaccard = new JaccardCoefficient();
		//Cosine cosine = new Cosine();
		double[] d = new double[map.length*map.length];
		int x = 0;
		for(int i = 0; i < map.length ; i++){
			
			for(int j = 0; j < map.length ; j++){
				
				if(j>=i) {d[x++] = Double.MAX_VALUE; continue;}
				d[x++] = distance(map[i], map[j]);
				//d[x++] = jaccard.similarity(map[i], map[j]);
				//d[x++] = cosine.cosineDistance(map[i], map[j]);
			}
			
		}
		
		return d;
		
	}
	
	public LinkedHashSet<LinkedHashSet<Integer>> sortIndexes(){
		
		LinkedHashSet<LinkedHashSet<Integer>> orderedSets = new LinkedHashSet<LinkedHashSet<Integer>>();
		
		HashMap<Integer, LinkedHashSet<Integer>> sets = new HashMap<>();
		
		for(int i = 0 ; i < sorted.length ; i++) {
			
			System.out.println(d[sorted[i]] + ": " + "("+(int)(sorted[i]/map.length+1) + ", " + (int)(sorted[i]%map.length+1)+")");
			
			int ta = (int)(sorted[i]/map.length+1);
			
			int tb = (int)(sorted[i]%map.length+1);
			
			Triple taTriple = reverseTripleIndex.get(sorted[i]/map.length);
			Triple tbTriple = reverseTripleIndex.get(sorted[i]%map.length);
			
			
			if(sets.isEmpty()){
				
				LinkedHashSet<Integer> set = new LinkedHashSet<>();
				
				//sumA = getSum(existingNodesCount, taTriple);
				//sumB = getSum(existingNodesCount, tbTriple);
				/*System.out.println("triple " + ta);
				System.out.println(Arrays.toString(map[ta-1]));
				System.out.println("sum 1 ta : " + ArrayUtils.FindSumforRowAbs(map, ta-1));
				System.out.println("triple " + tb);
				System.out.println(Arrays.toString(map[tb-1]));
				System.out.println("sum 1 tb : " + ArrayUtils.FindSumforRowAbs(map, tb-1));		*/		
//				if(ArrayUtils.FindSumforRowAbs(map, ta-1) < ArrayUtils.FindSumforRowAbs(map, tb-1)){
				if(Math.min(map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getPredicate())], Math.min(map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getSubject())], map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getObject())])) < 
						Math.min(map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getPredicate())], Math.min(map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getSubject())], map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getObject())]))){	
					set.add(ta);
					set.add(tb);
				}
				else{
					set.add(tb);
					set.add(ta);
				}
				sets.put(ta, set);
				sets.put(tb, set);
				orderedSets.add(set);
			}
			else{
				
				//if(sets.size()==sorted.length) break;
				if(sets.size()==reverseTripleIndex.size()) break;
								
				if(sets.containsKey(ta) || sets.containsKey(tb)){	
					
					if(sets.containsKey(ta) && !sets.containsKey(tb)){
						
						sets.get(ta).add(tb);
						sets.put(tb, sets.get(ta));																	
						continue;
					}
					if(sets.containsKey(tb) && !sets.containsKey(ta) ){
												
						sets.get(tb).add(ta);
						sets.put(ta, sets.get(tb));
										
						continue;
					}					
				}
				else{
					
					LinkedHashSet<Integer> set = new LinkedHashSet<>();					
					
					/*if( map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getPredicate())] >
						map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getPredicate())] ){ 		*/
					//if(ArrayUtils.FindSumforRowAbs(map, ta-1) < ArrayUtils.FindSumforRowAbs(map, tb-1)){
					if(Math.min(map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getPredicate())], Math.min(map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getSubject())], map[tripleIndex.get(taTriple)][nodeIndex.get(taTriple.getObject())])) < 
							Math.min(map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getPredicate())], Math.min(map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getSubject())], map[tripleIndex.get(tbTriple)][nodeIndex.get(tbTriple.getObject())]))){
						set.add(ta);
						set.add(tb);
					}
					else{
						set.add(tb);
						set.add(ta);
					}	
					sets.put(ta, set);
					sets.put(tb, set);
					orderedSets.add(set);
				}				
				
			}
													
			
		}
		
		return orderedSets;
		
	}
	

	/*private int getSum(HashMap<Node, Integer> existingNodesCount2,
			Triple taTriple) {
		int sumA = 0;
		if(existingNodesCount2.containsKey(taTriple.getSubject()))
			sumA += existingNodesCount.get(taTriple.getSubject());
		if(existingNodesCount2.containsKey(taTriple.getPredicate()))
			sumA += existingNodesCount.get(taTriple.getPredicate());
		if(existingNodesCount2.containsKey(taTriple.getObject()))
			sumA += existingNodesCount.get(taTriple.getObject());
		
		return sumA;
	}*/

	private HashMap<Node, Integer> addVariableNodes(
			HashMap<Node, Integer> existingNodesCount2, Triple taTriple) {
		
		if(taTriple.getSubject().isVariable()){
			if(existingNodesCount.keySet().contains(taTriple.getSubject())){
				existingNodesCount.put(taTriple.getSubject(), existingNodesCount.get(taTriple.getSubject())+1);
			}
			else 
				existingNodesCount.put(taTriple.getSubject(), 1);
		}
		if(taTriple.getPredicate().isVariable()){
			if(existingNodesCount.keySet().contains(taTriple.getPredicate()))
				existingNodesCount.put(taTriple.getPredicate(), existingNodesCount.get(taTriple.getPredicate())+1);
			else 
				existingNodesCount.put(taTriple.getPredicate(), 1);
		}
		if(taTriple.getObject().isVariable()){
			if(existingNodesCount.keySet().contains(taTriple.getObject()))
				existingNodesCount.put(taTriple.getObject(), existingNodesCount.get(taTriple.getObject())+1);
			else 
				existingNodesCount.put(taTriple.getObject(), 1);
		}
		
		
		return existingNodesCount;
	}
	
	
	private HashMap<LinkedHashSet<Integer>, HashSet<Node>> addVariableNodes(
			HashMap<LinkedHashSet<Integer>, HashSet<Node>> existingNodes2,
			LinkedHashSet<Integer> linkedHashSet) {
						
		
		for(Integer ind : linkedHashSet){
			
			Triple taTriple = reverseTripleIndex.get(ind-1);
			if(taTriple.getSubject().isVariable()){
				if(existingNodes.containsKey(linkedHashSet)){
					existingNodes.get(linkedHashSet).add(taTriple.getSubject());
				}
				else {
					HashSet<Node> newSet = new HashSet<>();
					newSet.add(taTriple.getSubject());
					existingNodes.put(linkedHashSet, newSet);
				}
				nodeTripleIndex.get((taTriple.getSubject())).add(taTriple);
			}
			if(taTriple.getPredicate().isVariable()){
				if(existingNodes.keySet().contains(linkedHashSet)){
					existingNodes.get(linkedHashSet).add(taTriple.getPredicate());
				}
				else {
					HashSet<Node> newSet = new HashSet<>();
					newSet.add(taTriple.getPredicate());
					existingNodes.put(linkedHashSet, newSet);
				}
				nodeTripleIndex.get((taTriple.getPredicate())).add(taTriple);
			}
			if(taTriple.getObject().isVariable()){
				if(existingNodes.keySet().contains(linkedHashSet)){
					existingNodes.get(linkedHashSet).add(taTriple.getObject());
				}
				else {
					HashSet<Node> newSet = new HashSet<>();
					newSet.add(taTriple.getObject());
					existingNodes.put(linkedHashSet, newSet);
				}
				nodeTripleIndex.get((taTriple.getObject())).add(taTriple);
			}
		}
		
		
		
		return existingNodes;
	}
	
	private long costOfSet(LinkedHashSet<Integer> set){
		
		long cost = -1;
		for(Integer i : set){
			//cost += ArrayUtils.FindSumforRowAbs(map, i-1);
			cost += Math.min(map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getPredicate())], 
					Math.min(map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getSubject())], 
							map[i-1][nodeIndex.get(reverseTripleIndex.get(i-1).getObject())]));
		}
		return cost;
		
	}
}
