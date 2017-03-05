package rpd.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.xml.sax.SAXException;

import exceptions.PropertyValueException;
import exceptions.ToothMapException;
import exceptions.ToothModifierException;
import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.BeamSearch;
import rpd.RPDPlan;
import rpd.oral.Instantialize;
import rpd.oral.Mandibular;
import rpd.oral.Mouth;

public class testBeamSearch {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ToothMapException, ToothModifierException, PropertyValueException, RuleException, ToothPosException, ClaspAssemblyException, EdentulousTypeException {
		
		File emr_group_dir = new File("C:\\workspace\\datasets\\dental\\emr_groups_2_牙列缺损");
		File owl_file = new File("C:\\workspace\\DentalLabelTool2\\res\\CDSSinRPD_ontology_161109.owl");
		File bug_xml_file = new File("C:\\workspace\\datasets\\dental\\emr_groups_2_牙列缺损\\2_2000015409\\0.xml");
		
		test(emr_group_dir, owl_file);
		//testOneFile(bug_xml_file, owl_file);
	}
	
	@SuppressWarnings("unused")
	public static void testOneFile(File label_xml_file, File owl_file) throws ParserConfigurationException, SAXException, IOException, ToothMapException, ToothModifierException, PropertyValueException, RuleException, ToothPosException, ClaspAssemblyException, EdentulousTypeException {
		
		OntModel mouth_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		mouth_ont.read("file:" + owl_file.getCanonicalPath());
		Instantialize.convertXmlToOnt(mouth_ont, label_xml_file);
		Mouth mouth = new Mouth(mouth_ont);
		List<RPDPlan> rpd_plans = BeamSearch.searchMandibular(mouth);
		System.out.println();
	}
	
	public static void test(File emr_group_dir, File owl_file) throws IOException {
		
		int no_missing_cnt = 0;
		int bug_cnt = 0;
		Map<Integer, List<File>> plannum_xmls = new HashMap<Integer, List<File>>();
		List<File> bug_xmls = new ArrayList<File>();
		List<File> first_visit_xmls = new ArrayList<File>();
		
		for(File patient_dir : emr_group_dir.listFiles()) {
			File first_visit_xml = findFirstVisitXml(patient_dir);
			first_visit_xmls.add(first_visit_xml);
		}
		
		int cnt = 0;
		
		for(File first_visit_xml : first_visit_xmls) {
			
			cnt++;
			if(cnt % 10 == 0)
				System.out.println(cnt);
			
			try {
				OntModel mouth_ont = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
				mouth_ont.read("file:" + owl_file.getCanonicalPath());
				Instantialize.convertXmlToOnt(mouth_ont, first_visit_xml);
				Mouth mouth = new Mouth(mouth_ont);
				
				Mandibular mandibular = mouth.getMandibular();
				if(BeamSearch.noMissing(mandibular)) {
					no_missing_cnt++;
					continue;
				}
				List<RPDPlan> rpd_plans = BeamSearch.searchMandibular(mouth);
				int plan_num = rpd_plans.size();
				if(!plannum_xmls.containsKey(plan_num))
					plannum_xmls.put(plan_num, new ArrayList<File>());
				plannum_xmls.get(plan_num).add(first_visit_xml);
				
				if(rpd_plans.size() == 0)
					continue;
				
				if(rpd_plans.size() == 1)
					System.out.println();
				
				File output_file = new File(first_visit_xml.getCanonicalPath().replace(".xml", ".plan"));
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file), "utf8"));
				for(int i = 0; i < rpd_plans.size(); i++) {
					RPDPlan plan = rpd_plans.get(i);
					w.write(i + ":\n" + plan.toString() + "\n\n");
				}
				w.close();
				
			} catch(Exception e) {
				bug_xmls.add(first_visit_xml);
				bug_cnt++;
				continue;
			}
		}
		
		System.out.println("no_missing: " + no_missing_cnt);
		System.out.println("bug_cnt: " + bug_cnt);
		
		for(Map.Entry<Integer, List<File>> ent : plannum_xmls.entrySet())
			System.out.println(ent.getKey() + ": " + ent.getValue().size());
		
		for(File bug_xml : bug_xmls)
			System.out.println(bug_xml.getCanonicalPath());
	}
	
	public static File findFirstVisitXml(File patient_dir) {
		
		int min_num = Integer.MAX_VALUE;
		File min_num_file = null;
		for(File file : patient_dir.listFiles()) {
			
			if(!file.getName().endsWith(".xml"))
				continue;
			String xml_num_str = file.getName().replace(".xml", "");
			int xml_num = Integer.valueOf(xml_num_str);
			if(xml_num < min_num) {
				min_num = xml_num;
				min_num_file = file;
			}
		}
		return min_num_file;
	}
	
}
