package app.tests;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.transform.ReorderWeightedTransformation;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.tdb.TDBFactory;

public class QueryTester {

	static Model model; 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 String base = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#";
		 String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
		 Dataset dataset = TDBFactory.createDataset("C:/temp/TDB");
	     model = dataset.getDefaultModel();
	     String queryString = "", queryString2 = "";	     
	     
	     File file = new File("C:/temp/sub.txt");
	     File file2 =  new File("C:/temp/lubm-queries/lubm_original/query2.txt");
	   	 try {
	   		   byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
	   		   queryString = new String(encoded, StandardCharsets.UTF_8);
	   	 } catch (Exception e) {	e.printStackTrace(); }
	   	 try {
	   		   byte[] encoded = Files.readAllBytes(Paths.get(file2.getAbsolutePath()));
	   		   queryString2 = new String(encoded, StandardCharsets.UTF_8);
	   	 } catch (Exception e) {	e.printStackTrace(); }
	   	 Query q = QueryFactory.create(queryString);
	   	 Query q2 = QueryFactory.create(queryString2);
	   	CustomPlanner plan = new CustomPlanner(q2);
	    List<Var> vars = new ArrayList<>() ;
	    //vars.add(Var.alloc("X")) ;
	    //vars.add(Var.alloc("Y")) ;
	    //vars.add(Var.alloc("Z")) ;
	    vars = q2.getProjectVars();
       	
       	plan.createQuery();				
       	//Op newOp = new OpBGP(plan.createBP(VirtuosoPlans.queryMap.get(file.getName()))) ;
       	Op newOp = new OpBGP(plan.createBP(new ArrayList(Arrays.asList(new Integer[]{3,5,2,6,1,4})))) ;
       	OpProject newopp = new OpProject(newOp, vars);
	     /*Query q_sub = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?X ?Y ?Z WHERE { {SELECT ?X ?Y ?Z WHERE {?Y ub:subOrganizationOf <http://www.University0.edu> . ?Y rdf:type ub:Department . ?X ub:memberOf ?Y . ?X rdf:type ub:Student .  ?X ub:emailAddress ?Z} } }");
	     Query q = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> SELECT ?X ?Y ?Z WHERE { ?Y rdf:type ub:Department . ?Y ub:subOrganizationOf <http://www.University0.edu> . ?X ub:memberOf ?Y . ?X rdf:type ub:Student .  ?X ub:emailAddress ?Z }");*/
	 	Op op = Algebra.compile(q);
/*	 	Op op_sub = Algebra.compile(q_sub);
		OpProject opp_sub = (OpProject) op_sub;*/
		OpProject opp = (OpProject) op;		   	
		//OpProject opp2 = (OpProject) Transformer.transform(new ReorderWeightedTransformation(), opp);
		//			
		/*QueryExecution qexec = QueryExecutionFactory.create(q, model);
		ResultSet rs = qexec.execSelect();
		long start1 = System.nanoTime();
		while(rs.hasNext()){
			QuerySolution r = rs.next();
		}
		long elapsed1 = System.nanoTime() - start1;
		System.out.println("qexec: " + elapsed1);*/
		//opp_sub = (OpProject) Algebra.optimize(opp_sub);
		/*System.out.println("Weighted : " + opp_sub.toString());
		System.out.println(testQuery(opp_sub));*/
		
		System.out.println("Weighted : " + Algebra.optimize(opp).toString());
		System.out.println(testQuery(Algebra.optimize(opp)));
		
/*		System.out.println("Weighted : " + newopp.toString());
		System.out.println(testQuery(newopp));*/
						
		if(true) return;
	     //newBp.add(Triple.create(Var.alloc("Z"), p, o));
	     
	     BasicPattern jenaBp = new BasicPattern();
	     //query2
	    /* jenaBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Department")));
	     jenaBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(base+"subOrganizationOf"), Var.alloc("Y")));
	     jenaBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"University")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"memberOf"), Var.alloc("Z")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"GraduateStudent")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"undergraduateDegreeFrom"), Var.alloc("Y")));*/
	     /*jenaBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Faculty")));
	     jenaBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(base+"teacherOf"), Var.alloc("Z")));
	     jenaBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Course")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"advisor"), Var.alloc("Y")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Student")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"takesCourse"), Var.alloc("Z")));*/
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"undergraduateDegreeFrom"), Var.alloc("Y")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"GraduateStudent")));
	     jenaBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"memberOf"), Var.alloc("Z")));
	     jenaBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Department")));
	     jenaBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(base+"subOrganizationOf"), Var.alloc("Y")));
	     jenaBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"University")));
	     
	     
	     
	     
	     BasicPattern ourBp = new BasicPattern();
	     /*ourBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Faculty")));
	     ourBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(base+"teacherOf"), Var.alloc("Z")));
	     ourBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Course")));
	     ourBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"advisor"), Var.alloc("Y")));
	     ourBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Student")));
	     ourBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"takesCourse"), Var.alloc("Z")));*/
	     //query2
	     ourBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Department")));
	     ourBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(base+"subOrganizationOf"), Var.alloc("Y")));
	     ourBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"University")));
	     ourBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"undergraduateDegreeFrom"), Var.alloc("Y")));
	     ourBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"GraduateStudent")));
	     ourBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"memberOf"), Var.alloc("Z")));
	     
	     
	     /*newBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Department")));
	     newBp.add(new Triple(Var.alloc("Z"), NodeFactory.createURI(base+"subOrganizationOf"), Var.alloc("Y")));
	     newBp.add(new Triple(Var.alloc("Y"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"University")));
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"undergraduateDegreeFrom"), Var.alloc("Y")));
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"GraduateStudent")));
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"memberOf"), Var.alloc("Z")));*/
	     /*newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"worksFor"), NodeFactory.createURI("http://www.Department0.University0.edu")));
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"Professor")));
	     
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"emailAddress"), Var.alloc("Y1")));
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"telephone"), Var.alloc("Y2")));
	     newBp.add(new Triple(Var.alloc("X"), NodeFactory.createURI(base+"name"), Var.alloc("Y3")));*/
	     /*newBp.add(new Triple(Var.alloc("fullProf"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"FullProfessor")));
	     newBp.add(new Triple(Var.alloc("fullProf"), NodeFactory.createURI(base+"teacherOf"), Var.alloc("course")));
	     newBp.add(new Triple(Var.alloc("student"), NodeFactory.createURI(base+"takesCourse"), Var.alloc("course")));
	     newBp.add(new Triple(Var.alloc("student"), NodeFactory.createURI(base+"advisor"), Var.alloc("advisor")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"name"), Var.alloc("name")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"researchInterest"), Var.alloc("RI")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"undergraduateDegreeFrom"), Var.alloc("UG")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"mastersDegreeFrom"), Var.alloc("MD")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"doctoralDegreeFrom"), Var.alloc("DD")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"emailAddress"), Var.alloc("email")));*/
	    /* newBp.add(new Triple(Var.alloc("fullProf"), NodeFactory.createURI(rdf), NodeFactory.createURI(base+"FullProfessor")));
	     newBp.add(new Triple(Var.alloc("fullProf"), NodeFactory.createURI(base+"teacherOf"), Var.alloc("course")));	     
	     newBp.add(new Triple(Var.alloc("student"), NodeFactory.createURI(base+"takesCourse"), Var.alloc("course")));
	     newBp.add(new Triple(Var.alloc("student"), NodeFactory.createURI(base+"advisor"), Var.alloc("advisor")));
	     
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"mastersDegreeFrom"), Var.alloc("MD")));
	     
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"researchInterest"), Var.alloc("RI")));
	     
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"doctoralDegreeFrom"), Var.alloc("DD")));
	     
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"undergraduateDegreeFrom"), Var.alloc("UG")));
	     
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"emailAddress"), Var.alloc("email")));
	     newBp.add(new Triple(Var.alloc("advisor"), NodeFactory.createURI(base+"name"), Var.alloc("name")));*/
	     long jenaTime = 0, ourTime = 0, start = 0;
	     
	     
	     //System.out.println(newOp.toString());
	     
	     for(int i = 0; i < 10 ; i++){
	    	 
	    	/* Op jenaOp = new OpBGP(jenaBp) ;
	    	 start = System.nanoTime();	     
		     QueryIterator qIter = Algebra.exec(jenaOp, model.getGraph()) ;
		       
		        List<String> varNames = new ArrayList<String>() ;
		        varNames.add("X") ;
		        varNames.add("Y") ;
		        varNames.add("Z") ;
		        
		        ResultSet rs = new ResultSetStream(varNames, model, qIter);
		        //ResultSetFormatter.out(rs) ;
		        while(rs.hasNext()){
		        	QuerySolution qs = rs.next();
		        }
		        
		        jenaTime += System.nanoTime()-start;*/
	     //}
	    
	     
	     Op ourOp = new OpBGP(ourBp) ;
	     
	     //for(int i = 0; i < 21 ; i++){
	     start = System.nanoTime();	     
		 QueryIterator qIter2 = Algebra.exec(ourOp, model.getGraph()) ;
		       
		        List<String> varNames2 = new ArrayList<String>() ;
		        varNames2.add("X") ;
		        varNames2.add("Y") ;
		        varNames2.add("Z") ;
		        
		        ResultSet rs2 = new ResultSetStream(varNames2, model, qIter2);
		        //ResultSetFormatter.out(rs2) ;
		        while(rs2.hasNext()){
		        	QuerySolution qs = rs2.next();
		        }
		        ourTime += System.nanoTime()-start;
	     //}
		 
		 //ant 5601848409
		 //our 1727404438
		 //jena 4054725661 query 2
		 //our   994681936
		        
		 //jena 87499562
	      
	     }
	     //jenaTime = jenaTime/10;
	     //ourTime = ourTime/10;
	     System.out.println(jenaTime);
	     System.out.println(ourTime);
	     
	        model.close();
	        dataset.close();
	}
	
	public static long testQuery(Op op){
		
		 
		 QueryIterator qIter2 = Algebra.exec(op, model.getGraph()) ;
	       
	        List<String> varNames2 = new ArrayList<String>() ;
	        varNames2.add("X") ;
	        varNames2.add("Y") ;
	        varNames2.add("Z") ;
	        
	        ResultSet rs2 = new ResultSetStream(varNames2, model, qIter2);
	        //ResultSetFormatter.out(rs2) ;
	        while(rs2.hasNext()){
	        	QuerySolution qs = rs2.next();
	        	
	        }	
		long start = System.nanoTime();
			
	     
	    	 qIter2 = Algebra.exec(op, model.getGraph()) ;
		       		        
		        varNames2.add("X") ;
		        varNames2.add("Y") ;
		        varNames2.add("Z") ;
		        
		        rs2 = new ResultSetStream(varNames2, model, qIter2);
		        //ResultSetFormatter.out(rs2) ;
		        while(rs2.hasNext()){
		        	QuerySolution qs = rs2.next();
		        	
		        }		 					     
	     return (System.nanoTime()-start);
	     
	  }

}
