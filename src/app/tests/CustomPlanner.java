package app.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class CustomPlanner {
	
	Query query ;	
	ElementPathBlock triplePathBlock ;
	public HashMap<Triple, Integer> tripleIndex = new HashMap<>();
	public HashMap<Integer, Triple> reverseTripleIndex = new HashMap<>();
	public HashMap<LinkedHashSet<Integer>, HashSet<Node>> existingNodes = new HashMap<>();
	public HashMap<Node, HashSet<Triple>> nodeTripleIndex = new HashMap<>();
	public HashMap<Node, Integer> nodeIndex;
	public HashMap<Node, Integer> existingNodesCount = new HashMap<Node, Integer>();
	
	public CustomPlanner(Query q){
		this.query = q;
	}
	public void createQuery(){
		
		Element pattern = query.getQueryPattern();
		
		ElementGroup g = (ElementGroup) pattern ;
		
		triplePathBlock = (ElementPathBlock) g.getElements().get(0);
						
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
			
			nextIndex++;
			if(!nodeIndex.containsKey(triple.getSubject()))
				nodeIndex.put(triple.getSubject(), nextNodeIndex++);
			
			if(!nodeIndex.containsKey(triple.getPredicate()))
				nodeIndex.put(triple.getPredicate(), nextNodeIndex++);
			
			if(!nodeIndex.containsKey(triple.getObject()))
				nodeIndex.put(triple.getObject(), nextNodeIndex++);
			
		}
	}
	
	public BasicPattern createBP(ArrayList<Integer> finalSet){
		
		BasicPattern newBp = new BasicPattern();
		 		 				 
		for(Integer i : finalSet){			 	
			  newBp.add(reverseTripleIndex.get(i-1));			 		
	    }
		 					 		 
		return newBp;
	}

}
