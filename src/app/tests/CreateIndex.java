package app.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import stocker.probability.impl.ProbabilityIndex;
import stocker.util.Config;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class CreateIndex {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		String directory = "C:/temp/TDB_sp";
		 Dataset dataset = TDBFactory.createDataset(directory);
		Model model = dataset.getDefaultModel();
		ProbabilityIndex pi = new ProbabilityIndex();
		Config c = new Config();
		c.setIndexLevel(1);
		pi.create(model, c);
		Model index = pi.getModel();
		FileOutputStream fos = new FileOutputStream(new File("C:/Users/Marios/Desktop/index.rdf"));
		index.write(fos, "N3");
		
	}

}
