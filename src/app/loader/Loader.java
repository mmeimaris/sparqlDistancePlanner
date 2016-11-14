package app.loader;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class Loader {

			
	public Loader() {}
	
	public void persistLUBM(String directory, String modelPath ) throws Exception {
		
		 
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();	
		FileManager.get().readModel( model, modelPath);
        model.close();
		
		
	}
	
	
}
