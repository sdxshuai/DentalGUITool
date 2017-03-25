package rpd.oral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import ontologies.OntFunc;
import rpd.conceptions.AlveolarAbsorption;
import rpd.conceptions.GingivalRecession;
import rpd.conceptions.Position;
import rpd.conceptions.ToothMobility;

public class Mandibular {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, RuleException {
		
		/*File owl_file = new File("C:\\workspace\\MyDental\\data\\emr_data_ont\\3001551142.owl");
		OntModel dental_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		dental_ont.read("file:" + owl_file.getCanonicalPath());
		DatatypeProperty missing_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "is_missing");
		Individual tooth_ind = dental_ont.getIndividual(OntFunc.prefix + "tooth26");
		RDFNode missing_value = tooth_ind.getPropertyValue(missing_dp);
		Literal l = missing_value.asLiteral();
		//int i = l.getInt();
		//ToothMobility[] values = ToothMobility.values();
		System.out.println(l.getBoolean());*/
		
		List<Tooth> zone3 = new ArrayList<Tooth>();
		List<Tooth> zone4 = new ArrayList<Tooth>();
		zone3.add(null);
		zone4.add(null);
		for(int i = 1; i <= 8; i++) {
			Tooth tooth_obj = new Tooth(3, i);
			zone3.add(tooth_obj);
		}
		for(int i = 1; i <= 8; i++) {
			Tooth tooth_obj = new Tooth(4, i);
			zone4.add(tooth_obj);
		}
		
		zone3.get(8).setMissing(true);
		zone3.get(7).setMissing(true);
		zone3.get(1).setMissing(true);
		zone3.get(3).setMissing(true);
		zone4.get(1).setMissing(true);
		zone4.get(8).setMissing(true);
		zone4.get(7).setMissing(true);
		zone4.get(2).setMissing(true);
		zone4.get(3).setMissing(true);
		
		List<EdentulousSpace> edentulous_spaces = getEdentulousSpaces(zone3, zone4);
		
		System.out.println();
	}
	
	private List<Tooth> zone3 = new ArrayList<Tooth>();
	private List<Tooth> zone4 = new ArrayList<Tooth>();
	
	private List<EdentulousSpace> edentulous_spaces = null;
	
	public Mandibular(OntModel dental_ont) throws RuleException {	
		init(dental_ont);
	}
	
	public Tooth getTooth(int zone, int num) {
		if(zone == 3)
			return this.zone3.get(num);
		else if(zone == 4)
			return this.zone4.get(num);
		else
			return null;
	}
	
	public List<EdentulousSpace> getEdentulousSpaces() {
		return this.edentulous_spaces;
	}

	public List<Tooth> getExistingTeeth() {
		List<Tooth> res = new ArrayList<Tooth>();
		for (Tooth tooth:zone3) {
			if (tooth!=null) {
				if (!tooth.isMissing()) res.add(tooth);
			}
		}
		for (Tooth tooth:zone4) {
			if (tooth!=null) {
				if (!tooth.isMissing()) res.add(tooth);
			}
		}
		return res;
	}
	
	private void init(OntModel dental_ont) throws RuleException {
		initZone3(dental_ont);
		initZone4(dental_ont);
		initEdentulousSpaces();
	}
	
	private void initZone3(OntModel dental_ont) {
		
		zone3.add(null);
		String tooth_class_uri = OntFunc.prefix + "tooth";
		for(int i = 1; i <= 8; i++) {
			Individual tooth_ind = dental_ont.getIndividual(tooth_class_uri + "3" + i);
			Tooth tooth_obj = new Tooth(3, i);
			this.readToothInfo(tooth_obj, dental_ont, tooth_ind);
			zone3.add(tooth_obj);
		}
	}
	
	private void initZone4(OntModel dental_ont) {
		
		zone4.add(null);
		String tooth_class_uri = OntFunc.prefix + "tooth";
		for(int i = 1; i <= 8; i++) {
			Individual tooth_ind = dental_ont.getIndividual(tooth_class_uri + "4" + i);
			Tooth tooth_obj = new Tooth(4, i);
			this.readToothInfo(tooth_obj, dental_ont, tooth_ind);
			zone4.add(tooth_obj);
		}
	}

	public boolean isZone3AllMissing() {
		boolean flag = true;
		for (Tooth tooth:zone3) {
			if (tooth!=null) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	public boolean isZone4AllMissing() {
		boolean flag = true;
		for (Tooth tooth:zone4) {
			if (tooth!=null) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}
	
	public static boolean[] getTeethMissingFlags(List<Tooth> zone3, List<Tooth> zone4) {
		
		boolean[] teeth_missing_flags = new boolean[17];
		for(int i = 1; i <= 8; i++) {
			Tooth tooth = zone4.get(i);
			teeth_missing_flags[9 - i] = tooth.isMissing();
		}
		for(int i = 1; i <= 8; i++) {
			Tooth tooth = zone3.get(i);
			teeth_missing_flags[8 + i] = tooth.isMissing();
		}
		return teeth_missing_flags;
	}
	
	public static List<EdentulousSpace> getEdentulousSpaces(List<Tooth> zone3, List<Tooth> zone4) throws RuleException {
		
		List<EdentulousSpace> edentulous_spaces = new ArrayList<EdentulousSpace>();
		boolean[] teeth_missing_flags = getTeethMissingFlags(zone3, zone4);
		int i = 1;
		int edentulous_space_start = -1;
		int edentulous_space_end = -1;
		while( i <= 16 ) {
			
			if(!teeth_missing_flags[i]) {
				i++;
				continue;
			}
			else {
				edentulous_space_start = i;
				int j = i + 1;
				while(j <= 16 && teeth_missing_flags[j])
					j++;
				edentulous_space_end = j - 1;
				
				int start_zone = -1;
				int start_number = -1;
				int end_zone = -1;
				int end_number = -1;
				
				if(edentulous_space_start <= 8) {
					start_zone = 4;
					start_number = 9 - edentulous_space_start;
				}
				else {
					start_zone = 3;
					start_number = edentulous_space_start - 8;
				}
				
				if(edentulous_space_end <= 8) {
					end_zone = 4;
					end_number = 9 - edentulous_space_end;
				}
				else {
					end_zone = 3;
					end_number = edentulous_space_end - 8;
				}
				
				if(start_zone == 4 && start_number == 8 && end_zone == 4 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}
				
				if(start_zone == 3 && start_number == 8 && end_zone == 3 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}
				
				Tooth left_neighbor = null;
				Tooth right_neighbor = null;
				if(start_number != 8) {
					if(start_zone == 4)
						left_neighbor = zone4.get(start_number + 1);
					else if(start_zone == 3 && start_number == 1)
						left_neighbor = zone4.get(1);
					else if(start_zone == 3 && start_number != 1)
						left_neighbor = zone3.get(start_number - 1);
					else {}
				}
				if(end_number != 8) {
					if(end_zone == 4 && end_number == 1)
						right_neighbor = zone3.get(1);
					else if(end_zone == 4 && end_number != 1)
						right_neighbor = zone4.get(end_number - 1);
					else if(end_zone == 3)
						right_neighbor = zone3.get(end_number + 1);
				}
				
				EdentulousSpace edentulous_space = new EdentulousSpace(left_neighbor, right_neighbor, Position.Mandibular);
				edentulous_spaces.add(edentulous_space);
				i = edentulous_space_end + 1;
			}
		}
		return edentulous_spaces;
	}
	
	public void initEdentulousSpaces() throws RuleException {
		
		this.edentulous_spaces = new ArrayList<EdentulousSpace>();
		boolean[] teeth_missing_flags = getTeethMissingFlags(this.zone3, this.zone4);
		
		int i = 1;
		int edentulous_space_start = -1;
		int edentulous_space_end = -1;
		while( i <= 16 ) {
			
			if(!teeth_missing_flags[i]) {
				i++;
				continue;
			}
			else {
				edentulous_space_start = i;
				int j = i + 1;
				while(j <= 16 && teeth_missing_flags[j])
					j++;
				edentulous_space_end = j - 1;
				
				int start_zone = -1;
				int start_number = -1;
				int end_zone = -1;
				int end_number = -1;
				
				if(edentulous_space_start <= 8) {
					start_zone = 4;
					start_number = 9 - edentulous_space_start;
				}
				else {
					start_zone = 3;
					start_number = edentulous_space_start - 8;
				}
				
				if(edentulous_space_end <= 8) {
					end_zone = 4;
					end_number = 9 - edentulous_space_end;
				}
				else {
					end_zone = 3;
					end_number = edentulous_space_end - 8;
				}
				
				if(start_zone == 4 && start_number == 8 && end_zone == 4 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}
				
				if(start_zone == 3 && start_number == 8 && end_zone == 3 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}
				
				Tooth left_neighbor = null;
				Tooth right_neighbor = null;
				if(start_number != 8) {
					if(start_zone == 4)
						left_neighbor = zone4.get(start_number + 1);
					else if(start_zone == 3 && start_number == 1)
						left_neighbor = zone4.get(1);
					else if(start_zone == 3 && start_number != 1)
						left_neighbor = zone3.get(start_number - 1);
					else {}
				}
				if(end_number != 8) {
					if(end_zone == 4 && end_number == 1)
						right_neighbor = zone3.get(1);
					else if(end_zone == 4 && end_number != 1)
						right_neighbor = zone4.get(end_number - 1);
					else if(end_zone == 3)
						right_neighbor = zone3.get(end_number + 1);
				}
				
				EdentulousSpace edentulous_space = new EdentulousSpace(left_neighbor, right_neighbor, Position.Mandibular);
				this.edentulous_spaces.add(edentulous_space);
				i = edentulous_space_end + 1;
			}
		}
	}
	
	private void readToothInfo(Tooth tooth_obj, OntModel dental_ont, Individual tooth_ind) {
		
		DatatypeProperty missing_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "is_missing");
		DatatypeProperty mobility_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "mobility");
		DatatypeProperty mesial_tight_bite_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "mesial_tight_bite");
		DatatypeProperty distal_tight_bite_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "distal_tight_bite");
		DatatypeProperty gingival_recession_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "gingival_recession");
		DatatypeProperty teeth_related_imaging_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "teeth_related_imaging");
	
		RDFNode missing_value = tooth_ind.getPropertyValue(missing_dp);
		if(missing_value != null) {
			boolean missing_value_boolean = missing_value.asLiteral().getBoolean();
			tooth_obj.setMissing(missing_value_boolean);
		}
		
		RDFNode mobility_value = tooth_ind.getPropertyValue(mobility_dp);
		if(mobility_value != null) {
			int mobility_value_int = mobility_value.asLiteral().getInt();
			ToothMobility[] mobility_values = ToothMobility.values();
			tooth_obj.setMobility(mobility_values[mobility_value_int]);
		}
		
		RDFNode mesial_tight_bite_value = tooth_ind.getPropertyValue(mesial_tight_bite_dp);
		if(mesial_tight_bite_value != null) {
			boolean mesial_tight_bite_boolean = mesial_tight_bite_value.asLiteral().getBoolean();
			tooth_obj.setMesialTightBite(mesial_tight_bite_boolean);
		}
		
		RDFNode distal_tight_bite_value = tooth_ind.getPropertyValue(distal_tight_bite_dp);
		if(distal_tight_bite_value != null) {
			boolean distal_tight_bite_boolean = distal_tight_bite_value.asLiteral().getBoolean();
			tooth_obj.setDistalTightBite(distal_tight_bite_boolean);
		}
		
		RDFNode gingival_recession_value = tooth_ind.getPropertyValue(gingival_recession_dp);
		if(gingival_recession_value != null) {
			
			String datatype_uri = gingival_recession_value.asLiteral().getDatatypeURI();
			if(datatype_uri.equals("http://www.w3.org/2001/XMLSchema#int")) {
				int gingival_recession_int = gingival_recession_value.asLiteral().getInt();
				GingivalRecession[] gingival_recession_values = GingivalRecession.values();
				tooth_obj.setGingivalRecession(gingival_recession_values[gingival_recession_int]);
			}
		}
		
		RDFNode teeth_related_imaging_value = tooth_ind.getPropertyValue(teeth_related_imaging_dp);
		if(teeth_related_imaging_value != null) {
			int teeth_related_imaging_int = teeth_related_imaging_value.asLiteral().getInt();
			AlveolarAbsorption[] alveolar_absorption_values = AlveolarAbsorption.values();
			tooth_obj.setAlveolarAbsorption(alveolar_absorption_values[teeth_related_imaging_int]);
		}
		
	}
}
