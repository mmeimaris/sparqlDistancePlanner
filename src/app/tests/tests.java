package app.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import app.model.DataConnection;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.tdb.TDBFactory;

public class tests {

	public static void main(String[] args) {
		
		LUBMQueries queries = new LUBMQueries();
		
		
		Dataset dataset = TDBFactory.createDataset(args[0]);	   		
		//Model inmem = dataset.getDefaultModel();
		
		//LUBMQueries2 queries = new LUBMQueries2();
		String query = queries.getQueries(2).get(Integer.parseInt(args[1]));
		
			ArrayList<Long> times = new ArrayList<Long>();
			for(int i = 0 ; i < 10 ; i++){
				long start = System.nanoTime();
				
				Query q = QueryFactory.create(query);
				
				QueryExecution vqe = QueryExecutionFactory.create (q, dataset);
				HashSet<List<RDFNode>> resList = new HashSet<List<RDFNode>>();
				ResultSet res = vqe.execSelect();
				while(res.hasNext()){
					QuerySolution rs = res.next(); 
					List<RDFNode> list = new ArrayList<RDFNode>();
					for(Var var : q.getProjectVars()){
						list.add(rs.get(var.toString()));
					}
					resList.add(list);				
				}				
				long end = System.nanoTime();
				vqe.close();
				System.out.println("jena " +resList.size() + " : "  + (end-start));
				times.add((end-start));
			}
			Collections.sort(times);
			System.out.println("min: " + times.get(0));
		
		dataset.close();
		if(true) return;
		/*Dataset dataset2 = TDBFactory.createDataset("C:/temp/TDB_wordnet");
		//args[0] = "/data1/mmeimaris/TDB1000"
		Model model = dataset2.getDefaultModel();
		try {
			OutputStream fos = new FileOutputStream(new File("C:/temp/wordnet_exported.nt"));
			RDFDataMgr.write(fos, model, Lang.NT) ;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(model.size());
		if(true) return;
		//args[1] = "/data1/mmeimaris/LUBM"
		File dir = new File("C:/temp/wordnet/wordnet.nt");
		TDBLoader.loadModel(model, dir.toString());
		System.out.println(model.size());
		File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		      
		    	if(child.getName().contains("wordnet")){
			    	  //File newFile = new File(child.getName().replaceAll("classes\\\\", ""));
			    	  //child.renameTo(newFile);
			    	  TDBLoader.loadModel(model, child.getName());
		    		try {
						TDBLoader.load(TDBInternal.getBaseDatasetGraphTDB(dataset2.asDatasetGraph()),  new FileInputStream(child), false);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
			      }
		    }
		  } 
		
		dataset2.close();
		for (File child : directoryListing) {
		      if(child.getName().contains(".owl")){
		    	  child.delete();
		      }
		    }
		if(true) return;*/
		VirtGraph graph = DataConnection.getConnection(args[0]);
		//LUBMQueries2 queries = new LUBMQueries2();
		
		//for(String query : queries.getQueries(0)){
		String query2= queries.getQueries(2).get(Integer.parseInt(args[1]));
			ArrayList<Long> times2 = new ArrayList<Long>();
			for(int i = 0 ; i < 10 ; i++){
				long start = System.nanoTime();
				
				Query q = QueryFactory.create(query);
				
				VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (q, graph);
				HashSet<List<RDFNode>> resList = new HashSet<List<RDFNode>>();
				ResultSet res = vqe.execSelect();
				while(res.hasNext()){
					QuerySolution rs = res.next(); 
					List<RDFNode> list = new ArrayList<RDFNode>();
					for(Var var : q.getProjectVars()){
						list.add(rs.get(var.toString()));
					}
					resList.add(list);				
				}				
				long end = System.nanoTime();
				vqe.close();
				System.out.println("virtuoso " +resList.size() + " : "  + (end-start));
				times.add((end-start));
			}
			Collections.sort(times);
			System.out.println("min: " + times.get(0));
		//}
		
		
		/*Dataset dataset2 = TDBFactory.createDataset(args[0]);
		//args[0] = "/data1/mmeimaris/TDB1000"
		Model model = dataset2.getDefaultModel();
		//args[1] = "/data1/mmeimaris/LUBM"
		File dir = new File(args[1]);
		
		File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		      if(child.getName().contains(".owl")){
		    	  File newFile = new File(child.getName().replaceAll("classes\\\\", ""));
		    	  child.renameTo(newFile);
		    	  TDBLoader.loadModel(model, newFile.getName());
		      }
		    }
		  } 
		
		dataset2.close();
		for (File child : directoryListing) {
		      if(child.getName().contains(".owl")){
		    	  child.delete();
		      }
		    }*/
		if(true) return;
		/*File file = new File("C:/temp/testMap2");
		DB db = DBMaker.newFileDB(file)
 				.transactionDisable()
 				.fileChannelEnable()
 				//.fileMmapCleanerHackEnable()
 				.fileMmapEnable()
 				//.fileMmapEnableIfSupported() 				
 				//.mmapFileEnable()
 				.cacheSize(1000) 				
 				.closeOnJvmShutdown()
 				.make();
		for(int i = 0; i < 20; i++){
			
		Map<Integer, long[]> dbECSMap = db.hashMapCreate("ecsMap")
 				.keySerializer(Serializer.INTEGER)
 				.valueSerializer(Serializer.LONG_ARRAY)
 				.makeOrGet();
		int counter = 0;
		for(Integer in : dbECSMap.keySet()){
			long ts = System.nanoTime();
			long[] children = dbECSMap.get(in);
			for(long l : children){
				counter++;
			}
			long te = System.nanoTime();
			System.out.println(te-ts);
			ts = System.nanoTime();
			long[] children2 = dbECSMap.get(in);
			for(long l : children2){
				counter++;
			}
			te = System.nanoTime();
			System.out.println(te-ts);
			}
			break;
		}
		if(true) return;
		
		String query = LUBMQueries.q9;
		
		System.out.println(query);
		
		ArrayList<Long> times = new ArrayList<Long>();
		
		VirtGraph graph = DataConnection.getConnection("hello");
		//67614365920
		for(int i = 0 ; i < 10 ; i++){
			long start = System.nanoTime();
			
			Query q = QueryFactory.create(query);
			
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (q, graph);
			HashSet<List<RDFNode>> resList = new HashSet<List<RDFNode>>();
			ResultSet res = vqe.execSelect();
			while(res.hasNext()){
				QuerySolution rs = res.next(); 
				List<RDFNode> list = new ArrayList<RDFNode>();
				for(Var var : q.getProjectVars()){
					list.add(rs.get(var.toString()));
				}
				resList.add(list);				
			}
			
			long end = System.nanoTime();
			System.out.println("virtuoso " +resList.size() + " : "  + (end-start));
			times.add((end-start));
		}
		Collections.sort(times);
		System.out.println("min: " + times.get(0));
		Dataset dataset = TDBFactory.createDataset("C:/temp/TDB");	   		
		//Model inmem = dataset.getDefaultModel();		
		times = new ArrayList<Long>();
		for(int i = 0 ; i < 10 ; i++){
			long start = System.nanoTime();			
			HashSet<List<RDFNode>> resList = new HashSet<List<RDFNode>>();
			Query q = QueryFactory.create(query);
			QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
	 		ResultSet res = qexec.execSelect();
	 			 		
	 		while(res.hasNext()){
	 			QuerySolution rs = res.next(); 
				List<RDFNode> list = new ArrayList<RDFNode>();
				for(Var var : q.getProjectVars()){
					list.add(rs.get(var.toString()));
				}
				resList.add(list);	
	 			
	 		}
	 			 	
	 		long end = System.nanoTime();
			System.out.println("jena tdb " +resList.size() + " : "  + (end-start));
			times.add((end-start));
			qexec.close();
		}
		Collections.sort(times);
		System.out.println("min: " + times.get(0));*/
	}

}
