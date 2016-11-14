package app.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.model.BloomFilter;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public class MiscTests {

	public static void main(String[] args) {
		
		
		Dataset dataset = TDBFactory.createDataset("C:/temp/TDB");
	    Model model = dataset.getDefaultModel();
	    String queryString = "";
	   /* try {
			FileOutputStream fos = new FileOutputStream(new File("C:/temp/tdb_export.rdf"));
			model.write(fos);
			fos.close();
			if(true) return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
	    File file =  new File("C:/temp/lubm-queries/lubm_original/query2.txt");
	   	 try {
	   		   byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
	   		   queryString = new String(encoded, StandardCharsets.UTF_8);
	   	 } catch (Exception e) {	e.printStackTrace(); }
	   	 
	   	//queryString = " SELECT ?s ?p WHERE {?s ?p ?o} GROUP BY ?s ?p ORDER BY ?s  ";
	   	queryString = " SELECT ?s WHERE {?s <"+RDF.type.getURI()+"> ?o ; <http://swat.cse.lehigh.edu/onto/univ-bench.owl#researchInterest> ?o2 } GROUP BY ?s ?p ORDER BY ?s  ";
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qexec = QueryExecutionFactory.create(query, model);
	    ResultSet rs = qexec.execSelect();
	    RDFNode prevs = null;
	    HashMap<Resource, BloomFilter<Resource>> filters = new HashMap<Resource, BloomFilter<Resource>>();
	    BloomFilter<Resource> bf = new BloomFilter<Resource>(0.01d, 10);
	    long start = System.nanoTime();
	    while(rs.hasNext()){
	    	QuerySolution sol = rs.next();
	    	/*if(!sol.get("s").equals(prevs)){	    		
	    		//System.out.println("s: " + sol.get("s"));
	    		if(prevs != null){
	    			filters.put(sol.get("s").asResource(), bf);
	    			bf = new BloomFilter<Resource>(0.01d, 10);	    			
	    		}
	    	}
	    	//System.out.println("p: " + sol.get("p"));
	    	bf.add(sol.get("p").asResource());
	    	prevs = sol.get("s");*/
	    	
	    }
	    System.out.println(System.nanoTime()- start);
	    if(true) return;
	    //13652141319
	    //15532783166
	    //15532783166
	    //446113031
	    //3216339006
	    //matches 5715
	    List<Resource> qlist = new ArrayList<Resource>();
	    qlist.add(ResourceFactory.createResource(RDF.type.getURI()));
	    //qlist.add(ResourceFactory.createResource(RDFS.subClassOf.getURI()));	    
	    qlist.add(ResourceFactory.createResource("http://swat.cse.lehigh.edu/onto/univ-bench.owl#researchInterest"));
	    boolean is = false;
	    int c = 0;
	    start = System.nanoTime();
	   for(RDFNode s : filters.keySet()){
		   //System.out.println("subject: " + s);
		   
		   for(Resource p : qlist){
			   if(!filters.get(s).contains(p)){
				   is = false;
				   break;
			   }
			   else is = true;
		   }
		   if(is){
			   c++;
		   }
	   }
	   System.out.println(System.nanoTime()- start);
	   System.out.println("matches " + c);
	   

	}

}
