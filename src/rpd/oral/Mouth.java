package rpd.oral;

import java.io.File;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;

public class Mouth {

	private Mandibular mandibular = null;
	private Maxillary maxillary = null;
	
	private OntModel dental_ont = null;
	
	/*public Mouth(File owl_file) throws IOException, RuleException, ToothPosException {
		this.dental_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		this.dental_ont.read("file:" + owl_file.getCanonicalPath());
		this.mandibular = new Mandibular(dental_ont);
	}*/
	
	public Mouth(OntModel dental_ont) throws IOException, RuleException, ToothPosException {
		this.dental_ont = dental_ont;
		this.mandibular = new Mandibular(dental_ont);
	}
	
	public Tooth getTooth(int zone, int num) {
		
		if(zone == 1 || zone == 2)
			return maxillary.getTooth(zone, num);
		else if(zone == 3 || zone == 4)
			return mandibular.getTooth(zone, num);
		else
			return null;
	}
	
	public Mandibular getMandibular() {
		return this.mandibular;
	}
	
	public Maxillary getMaxillary() {
		return this.maxillary;
	}
}
