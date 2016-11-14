package arqaco.statistics.impl;

import arqaco.utility.OntologyOperation;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author E. Guzel Kalayci
 * 
 */
public class CardinalityCalculator {

	public static Model model;
	
	public static long getEstimatedCardinality(Triple triple, long ontSize) {
		return getMINCardinality(triple, ontSize);
	}

	private static long getMINCardinality(Triple triple, long ontSize) {
		if (triple.getPredicate().getLocalName().equals("subClassOf"))
			return ontSize;

		long minCar = ontSize;
		//System.out.println(triple.toString() + " model " + model.size());
		long cardinality = -1;/*OntologyOperation
				.getModel()
				.getGraph()
				.getStatisticsHandler()
				.getStatistic(triple.getSubject(), triple.getPredicate(),
						triple.getObject());*/

		if (cardinality == -1) {
			
			cardinality = getStat(triple, model);
				if (cardinality < minCar)
					minCar = cardinality;
			
			cardinality = minCar;
		}
		return cardinality;
	}
	
	private static long getStat(Triple triple, Model model) {
		// TODO Auto-generated method stub
		Query qry = null ;
		long stat = 0;
		/*System.out.println(triple.getSubject());
		System.out.println(triple.getPredicate());
		System.out.println(triple.getObject());*/
		if(!triple.getSubject().toString().equals("ANY") && !triple.getSubject().isVariable())
			qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { <"+triple.getSubject().getURI()+"> ?y ?z }");
		if(!triple.getPredicate().toString().equals("ANY") && !triple.getPredicate().isVariable())
			qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?x <"+triple.getPredicate().getURI()+"> ?z }");
		if(!triple.getObject().toString().equals("ANY") && !triple.getObject().isVariable())			
			qry = QueryFactory.create("SELECT (count(*) as ?count) WHERE { ?x ?y <"+triple.getObject().getURI()+"> }");
		QueryExecution qe = QueryExecutionFactory.create(qry, model);
	
	    ResultSet rs = qe.execSelect();
	
	    while(rs.hasNext())
	    {
	        QuerySolution sol = rs.nextSolution();
	        
	        stat = sol.getLiteral("count").getLong();
	        
	    }
	    qe.close();	
	    
	    return stat;
	}

}
