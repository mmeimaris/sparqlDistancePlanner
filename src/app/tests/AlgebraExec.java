package app.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import stocker.heuristic.HeuristicsRegistry;
import stocker.probability.Probability;
import stocker.probability.ProbabilityFactory;
import stocker.util.Config;
import stocker.util.Constants;
import app.transform.ReorderFixedTransformation;
import app.transform.ReorderWeightedTransformation;
import app.transform.StockerTransformONS;
import app.transform.StockerTransformPFJ;
import arqaco.ds.TransformParameters;
import arqaco.transform.OptimizationTransform;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class AlgebraExec {

	static public String statsFile = "C:/temp/TDB/stats.opt";
	//static public String statsFile = "/data1/mmeimaris/TDB/stats.opt";
	//static public String statsFile = "/data1/mmeimaris/TDB2/stats.opt";
	//static public String statsFile = "C:/temp/TDB_sp/stats.opt";
	static String pfjFile = "C:/Users/Marios/Documents/Projects/Query planner/ARQ-ACO-master/data/university_v2_index.owl"; 
   	//static String pfjFile = "/data1/mmeimaris/university_v2_index.owl";
	static HashMap<String, Double> results = new HashMap<>();
	static HashMap<String, Double> times = new HashMap<String, Double>();
	static int loops = 1;
	static int timeout = 3600;
	public static Model model;
	public static HashSet<String> methods = new HashSet<String>();
	static{				
		methods.add("weighted");		
		methods.add("fixed");
		methods.add("ons");
		methods.add("ant");
		methods.add("pfj");			
		methods.add("our2");
		methods.add("virtuoso");		
	}
	public static void main(String[] args) {
		
		
		//TDB.getContext().set(ARQ.symLogExec,true) ;
		String queriesDir = args[0]; //"C:/temp/lubm-queries"
		queriesDir =  "C:/temp/lubm-queries/lubm_original";
		//queriesDir =  "C:/temp/lubm-queries/my_queries";
		//queriesDir =  "C:/temp/lubm-queries/ant_query_set";
		//queriesDir =  "/data1/mmeimaris/lubm-queries/ant_query_set";
		//queriesDir =  "C:/temp/sp-queries";
        String directory = args[1]; //"C:/temp/TDB";
        //directory = "/data1/mmeimaris/TDB2";
        //directory = "C:/temp/TDB_sp";
        Dataset dataset = TDBFactory.createDataset(directory);
	    model = dataset.getDefaultModel();
	    
	    OutputStream resultsStream = null;
	    BufferedWriter writer = null;
	    try {
	    	
			resultsStream = new FileOutputStream(new File("_results_"));
			writer = new BufferedWriter(new OutputStreamWriter(resultsStream));
			writer.write("Query\t\tWeighted\t\tFixed\t\tONS\t\tPFJ\t\tANT\t\tOUR\tOUR2\t") ;
			writer.newLine();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ExecutorService executor = Executors.newSingleThreadExecutor();        
	    Future<Long> future ;
	    String queryString = "";
		/*Model pfjIndexModel = ProbabilityFactory.createIndex(model);
		try {
			pfjIndexModel.write(new FileOutputStream(new File(args[2])));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
	    File[] files = new File(queriesDir).listFiles();
	    
	    
	    
	    
		for(File file : files){
		    	
		   	   
			if(!file.getName().contains("query8") ) continue;
		   	   try {
		   		   byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		   		   queryString = new String(encoded, StandardCharsets.UTF_8);
		   	   } catch (Exception e) {	e.printStackTrace(); break; }

   	   try {
	    	System.out.println("Parsing query: " + file.getName());
	    	writer.write(file.getName()+"\t");
			Query q = QueryFactory.create(queryString);
			 
			
			
			/*MyPlanner plan1 = new MyPlanner(q, model);
		    List<Var> vars1 = new ArrayList<>() ;
		    vars1.add(Var.alloc("X")) ;
		    vars1.add(Var.alloc("Y")) ;
		    vars1.add(Var.alloc("Z")) ;
	       	
	       	plan1.map = plan1.buildQueryMap();
			Op ourop1 = plan1.getPlan();
			OpProject newopp1 = new OpProject(ourop1, vars1);
			 QueryIterator qIter2 = Algebra.exec(newopp1, model.getGraph()) ;
		       
		        List<String> varNames2 = new ArrayList<String>() ;
		        varNames2.add("X") ;
		        varNames2.add("Y") ;
		        varNames2.add("Z") ;
		        
		        ResultSet rs2 = new ResultSetStream(varNames2, model, qIter2);
		        //ResultSetFormatter.out(rs2) ;
		        while(rs2.hasNext()){
		        	QuerySolution qs = rs2.next();
		        	
		        }	*/
			    //   
			/*
			Query dq = QueryFactory.create("SELECT ?s WHERE {?s <http://ksjd.ksdjsd.fcvd> <http://ksjd.ksdjsd.fcv>}");
			Op dop = Algebra.compile(dq);
			OpProject ddopp = (OpProject) dop;		   	
			OpProject dopp = (OpProject) Transformer.transform(new ReorderWeightedTransformation(), ddopp);
			
			 
			//dummy
			future = executor.submit(new AlgebraExec().new RunQuery(dopp));			
			
			
			//System.out.println("Weighted avg: " + );
   	   		try{
   	   			future.get(10, TimeUnit.SECONDS);
	   	   	}catch(TimeoutException e){	   	   		
	   	   		future.cancel(true);
			}				
			*/
			
			
	   	   	Op op = Algebra.compile(q);
			OpProject opp = (OpProject) op;		   	
			OpProject newopp = (OpProject) Transformer.transform(new ReorderWeightedTransformation(), opp);
			//			
			 
			if(methods.contains("weighted")){
				newopp = (OpProject) Transformer.transform(new ReorderWeightedTransformation(), opp);
				System.out.println("Weighted : " + newopp.toString());
				future = executor.submit(new AlgebraExec().new RunQuery(newopp));			
				
				
				//System.out.println("Weighted avg: " + );
	   	   		try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}	
			}
				
			if(methods.contains("fixed")){
				newopp = (OpProject) Transformer.transform(new ReorderFixedTransformation(), opp);
				System.out.println("Fixed : " + newopp.toString());
			   	future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   	
			   	//System.out.println("Fixed avg: " + future.get(timeout, TimeUnit.SECONDS));
			   	try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}	  
			}
			
		   	
		   	//System.out.println("Fixed avg: " + testQuery(newopp));
			if(methods.contains("ons")){
				newopp = (OpProject) Transformer.transform(new StockerTransformONS(model.getGraph()), opp);
			   	System.out.println("ONS : " + newopp.toString());
			   	future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   	
			   	//System.out.println("ONS avg: " + future.get(timeout, TimeUnit.SECONDS));
			   	try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}	
			}
		     
		   	
		   	
			
			/*newopp = (OpProject) Transformer.transform(new ReorderWeightedTransformation(), opp);
			System.out.println("Weighted : " + newopp.toString());			
			newopp = (OpProject) Transformer.transform(new ReorderWeightedTransformation(), opp);
			future = executor.submit(new AlgebraExec().new RunQuery(newopp));			
			
			
			//System.out.println("Weighted avg: " + );
   	   		try{
   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
   	   			System.out.println("Done.");
	   	   	}catch(TimeoutException e){
	   	   		writer.write("timeout\t\t");
	   	   		future.cancel(true);
	   	   		System.out.println("Timeout.");
			}		*/
		   	//System.out.println("ONS avg: " + testQuery(newopp));
			if(methods.contains("pfj")){
				prepareForPFJ(pfjFile, model);
			   	//prepareForPFJ(args[2], model);				
			   	newopp = (OpProject) Transformer.transform(new StockerTransformPFJ(), opp);
			   	System.out.println("PFJ : " + newopp.toString());
			   	future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   	
			   	//System.out.println("PFJ avg: " + future.get(timeout, TimeUnit.SECONDS));
			   	try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}	 
			}
		   	 
		   	
		   	//System.out.println("PFJ avg: " + testQuery(newopp));
			
			if(methods.contains("ant")){
				TransformParameters.optimization = arqaco.ds.Constants.OPT_AS;
				TransformParameters.cost = arqaco.ds.Constants.COST_MOD_STOCKER;
				TransformParameters.nodechooser = arqaco.ds.Constants.SNC_RND;
				newopp = (OpProject) Transformer.transform(new OptimizationTransform(model), opp);
			   	System.out.println("ANT : " + newopp.toString());
			   	//System.out.println("ANT avg: " + testQuery(newopp));
				future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   	
			   	//System.out.println("ANT avg: " + future.get(timeout, TimeUnit.SECONDS));
				try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}	  
			}
		   
			if(methods.contains("our")){
				MyPlanner plan = new MyPlanner(q, model);
				//Planner2 plan = new Planner2(q, model);
			    List<Var> vars = new ArrayList<>() ;
			    vars.add(Var.alloc("X")) ;
			    //vars.add(Var.alloc("Y")) ;
			    //vars.add(Var.alloc("Z")) ;
		       	
		       	plan.map = plan.buildQueryMap();
				Op ourop = plan.getPlan();
				newopp = new OpProject(ourop, vars);
				System.out.println("OUR : " + newopp.toString());
			  	//System.out.println("OUR avg: " + testQuery(newopp));O
				future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   	
				//System.out.println("OUR avg: " + future.get(timeout, TimeUnit.SECONDS));
				try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}
			}
			
			if(methods.contains("our2")){
				//MyPlanner plan = new MyPlanner(q, model);
				Planner2 plan = new Planner2(q, model);
			    List<Var> vars = new ArrayList<>() ;
			    vars = q.getProjectVars();
			    //vars.add(Var.alloc("Y")) ;
			    //vars.add(Var.alloc("Z")) ;
		       	
		       	plan.map = plan.buildQueryMap();
				Op ourop = plan.getPlan();
				newopp = new OpProject(ourop, vars);
				System.out.println("OUR 2 : " + newopp.toString());
			  	//System.out.println("OUR avg: " + testQuery(newopp));O
				future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   	
				//System.out.println("OUR avg: " + future.get(timeout, TimeUnit.SECONDS));
				try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}
			}
			
			if(methods.contains("virtuoso")){
				//MyPlanner plan = new MyPlanner(q, model);
				CustomPlanner plan = new CustomPlanner(q);
			    List<Var> vars = new ArrayList<>() ;
			    //vars.add(Var.alloc("X")) ;
			    //vars.add(Var.alloc("Y")) ;
			    //vars.add(Var.alloc("Z")) ;
			    vars = q.getProjectVars();
		       	
		       	plan.createQuery();				
		       	//Op newOp = new OpBGP(plan.createBP(VirtuosoPlans.queryMap.get(file.getName()))) ;
		       	Op newOp = new OpBGP(plan.createBP(new ArrayList(Arrays.asList(new Integer[]{3,5,6,4,1,2})))) ;
		       
		       	newopp = new OpProject(newOp, vars);
				System.out.println("VIRTUOSO : " + newopp.toString());			  	
				future = executor.submit(new AlgebraExec().new RunQuery(newopp));		   					
				try{
	   	   			writer.write(future.get(timeout, TimeUnit.SECONDS)+"\t\t");
	   	   			System.out.println("Done.");
		   	   	}catch(TimeoutException e){
		   	   		writer.write("timeout\t\t");
		   	   		future.cancel(true);
		   	   		System.out.println("Timeout.");
				}
			}
			
			
			writer.newLine();
		} catch (Exception e2) {
			
			e2.printStackTrace();
		}	       	  	       	
				   		   	

      }

	       
			executor.shutdownNow();
	        
	        try {
	        	//writer.flush();
	        	System.out.println("Closing writer");
	        	writer.close();
	        	System.out.println("Writer closed");
	        	
	        	System.out.println("Executor shut down");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        System.out.println("Loop done.");
	       /* for(String filename : times.keySet()){
	        	System.out.println(filename+": " + times.get(filename)/loops );*/
	        model.close();
	}

	public static void prepareForPFJ(Model indexModel, Model model) {
		
		
		Config config = new Config();
		config.setBasicPatternHeuristic(HeuristicsRegistry.BGP_PROBABILISTIC_FRAMEWORK_JOIN);				
		Probability probability = ProbabilityFactory.loadDefaultModel(
				model, indexModel,
				config);	
		ARQ.getContext().set(Constants.PF, probability);
		
	}
	
	public static void prepareForPFJ(String indexFilename, Model model) {
		
		Model indexModel = loadIndex(indexFilename);
		Config config = new Config();
		config.setBasicPatternHeuristic(HeuristicsRegistry.BGP_PROBABILISTIC_FRAMEWORK_JOIN);
		
		/*try {
			m.read(new FileInputStream(new File("C:/temp/final_lubm_10_.owl")), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//System.out.println(model.getGraph().getStatisticsHandler());
		//Probability probability = ProbabilityFactory.createDefaultModel(model, config);
		Probability probability = ProbabilityFactory.loadDefaultModel(
				model, indexModel,
				config);
		//System.out.println(probability.getProbability());
		ARQ.getContext().set(Constants.PF, probability);
		
	}
	
	public static Model loadIndex(String filename) {
		OntModel subOntModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_MEM, null);

		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			throw new IllegalArgumentException("File: " + filename
					+ " not found");
		}

		// read the RDF/XML file
		subOntModel.read(in, "");
		return subOntModel;
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
			
	     for(int i = 0; i < loops ; i++){
	    	 qIter2 = Algebra.exec(op, model.getGraph()) ;
		       		        
		        varNames2.add("X") ;
		        varNames2.add("Y") ;
		        varNames2.add("Z") ;
		        
		        rs2 = new ResultSetStream(varNames2, model, qIter2);
		        //ResultSetFormatter.out(rs2) ;
		        while(rs2.hasNext()){
		        	QuerySolution qs = rs2.next();
		        	
		        }		 				
	     }
	     return (System.nanoTime()-start)/loops;
	     
	  }
	
	class RunQuery implements Callable<Long> {
	
		private Op op;
		
		public RunQuery(Op op) {
			this.op = op;
		}
		@Override
	    public Long call() throws Exception {
	        	    	
	        return testQuery(op);
	    }
	}
		 
	
}
