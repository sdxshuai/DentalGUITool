package rpd.oral;

import exceptions.PropertyValueException;
import exceptions.ToothMapException;
import exceptions.ToothModifierException;
import misc.ToothPosition;
import ontologies.LabelModifier;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

//初始化实例
public class Instantialize {

	private static String ont_prefix = "http://www.semanticweb.org/msiip/ontologies/CDSSinRPD#";

	private static void initTeeth(OntModel dental_model) {

		OntClass tooth_class = dental_model.getOntClass(ont_prefix + "tooth");
		DatatypeProperty tooth_zone_dp = dental_model.getDatatypeProperty(ont_prefix + "tooth_zone");
		DatatypeProperty tooth_ordinal_dp = dental_model.getDatatypeProperty(ont_prefix + "tooth_ordinal");
		DatatypeProperty is_missing_dp = dental_model.getDatatypeProperty(ont_prefix + "is_missing");

		for (int i = 1; i < 5; i++) {
			for (int j = 1; j < 9; j++) {
				int zone = i;
				int number = j;
				Individual tooth = tooth_class.createIndividual(ont_prefix + "tooth" + zone + number);
				tooth.addProperty(tooth_zone_dp, dental_model.createTypedLiteral(i));
				tooth.addProperty(tooth_ordinal_dp, dental_model.createTypedLiteral(j));
				if (number == 8)
					tooth.addProperty(is_missing_dp, dental_model.createTypedLiteral(true));
			}
		}
	}

	public static void convertXmlToOnt(OntModel dental_ont, File label_xml_file) throws ParserConfigurationException, SAXException, IOException, ToothMapException, ToothModifierException, PropertyValueException {

		initTeeth(dental_ont);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document document = db.parse(label_xml_file);

		Node all_labels = document.getFirstChild();
		NodeList label_node_list = all_labels.getChildNodes();

		for (int i = 0; i < label_node_list.getLength(); i++) {

			String property_str = null;
			DatatypeProperty property = null;
			String value_type = null;
			String value_str = null;
			LabelModifier modifier = null;
			String tooth_map_str = null;

			Node label_node = label_node_list.item(i);
			NodeList label_node_fields = label_node.getChildNodes();
			if (label_node_fields.getLength() == 0)
				continue;

			for (int j = 0; j < label_node_fields.getLength(); j++) {

				Node label_node_field = label_node_fields.item(j);

				String field_name = label_node_field.getNodeName();
				String field_content = label_node_field.getTextContent();

				if (field_name.equals("property")) {

					property_str = field_content;
					property = dental_ont.getDatatypeProperty(ont_prefix + field_content);
				}
				if (field_name.equals("value_type"))
					value_type = field_content;
				if (field_name.equals("value"))
					value_str = field_content;
				if (field_name.equals("modifier"))
					modifier = LabelModifier.valueOf(field_content);
				if (field_name.equals("tooth_map_str"))
					tooth_map_str = field_content;
			}

			if (property == null) {

				System.out.println("unknown property: " + property_str + " in " + label_xml_file.getName());
				continue;
			}

			if (tooth_map_str != null) {

				List<String> tooth_list = null;
				if (modifier == null || modifier.equals(LabelModifier.NT9))
					tooth_list = ToothPosition.getToothList(tooth_map_str);
				else
					tooth_list = ToothPosition.getModifiedToothList(tooth_map_str, modifier);

				for (String tooth_str : tooth_list) {

					Individual tooth_ind = dental_ont.getIndividual(ont_prefix + "tooth" + tooth_str);

					if (value_type.equals("boolean")) {
						if (value_str.equals("0"))
							tooth_ind.addProperty(property, dental_ont.createTypedLiteral(false));
						else if (value_str.equals("1"))
							tooth_ind.addProperty(property, dental_ont.createTypedLiteral(true));
						else
							throw new PropertyValueException("can not parse boolean value: " + value_str);
					} else if (value_type.equals("list_int")) {
						int int_value = Integer.valueOf(value_str);
						tooth_ind.addProperty(property, dental_ont.createTypedLiteral(int_value));
					} else if (value_type.equals("int")) {
						int int_value = Integer.valueOf(value_str);
						tooth_ind.addProperty(property, dental_ont.createTypedLiteral(int_value));
					} else if (value_type.equals("double")) {
						double double_value = Double.valueOf(value_str);
						tooth_ind.addProperty(property, dental_ont.createTypedLiteral(double_value));
					} else if (value_type.equals("string"))
						tooth_ind.addProperty(property, dental_ont.createTypedLiteral(value_str));
					else
						throw new PropertyValueException("unknown value type: " + value_type);
				
					/*if((!property_str.equals("is_missing")) && tooth_str.endsWith("8")) {
						DatatypeProperty is_missing_dp = dental_ont.getDatatypeProperty(ont_prefix + "is_missing");
						if(dental_ont.contains(tooth_ind, is_missing_dp, dental_ont.createTypedLiteral("true")))
							dental_ont.remove(tooth_ind, is_missing_dp, dental_ont.createTypedLiteral("true"));
						tooth_ind.addProperty(is_missing_dp, dental_ont.createTypedLiteral(false));
					}*/
				}
			}
		}
	}

}
