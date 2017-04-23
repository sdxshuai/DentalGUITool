package ontologies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import ontologies.LabelModifier;
import exceptions.PropertyValueException;

//取值含义
public class Descriptions {

	public Map<OntProperty, PropertyDescription> property_descriptions = null;

	public Map<LabelModifier, String> modifier_descriptions = null;

	private OntModel dental_ont = null;

	//public static Map<DentalProperty, String> class_description = null;

	public Descriptions(OntModel dental_ont, File modifier_file) throws IOException, PropertyValueException {

		this.dental_ont = dental_ont;
		this.readModifiers(modifier_file);
		this.readPropertyDescriptions(this.dental_ont);
	}

	private void readModifiers(File modifier_file) throws IOException {

		modifier_descriptions = new HashMap<LabelModifier, String>();
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(modifier_file), "utf8"));
		String line = null;

		while ((line = r.readLine()) != null) {

			String[] fs = line.split("###");
			LabelModifier modifier = LabelModifier.valueOf(fs[0].trim());
			modifier_descriptions.put(modifier, fs[1].trim());
		}
		r.close();
	}

	private void readPropertyDescriptions(OntModel ont) throws IOException, PropertyValueException {

		property_descriptions = new HashMap<OntProperty, PropertyDescription>();

		List<DatatypeProperty> dps = dental_ont.listDatatypeProperties().toList();
		for (DatatypeProperty dp : dps) {

			List<RDFNode> comments = dp.listComments(null).toList();
			List<? extends OntResource> value_ranges = dp.listRange().toList();
			PropertyDescription d = new PropertyDescription();
			d.property_eng_name = dp.getLocalName();

			OntResource domain = dp.getDomain();
			if (domain != null) {

				String name = domain.getLocalName();
				if (name.equals("existed_restorations_exam1") ||
						name.equals("edentulous_space_and_oral_mucosa_soft_tissue_exam") ||
						name.equals("tooth"))
					d.tooth_related = true;
				else
					d.tooth_related = false;
			} else
				d.tooth_related = false;

			if (value_ranges.size() == 0)
				property_descriptions.put(dp, d);

			else {

				property_descriptions.put(dp, d);

				for (OntResource value_range : value_ranges) {

					if (OntFunc.isRDFList(value_range)) {

						d.allow_list_value = true;
						//d.value_type = RDF.List;
						RDFList enumeration = value_range.getPropertyResourceValue(OWL.oneOf).as(RDFList.class);
						List<RDFNode> value_list = enumeration.asJavaList();
						List<Integer> list_int_values = new ArrayList<Integer>();
						for (RDFNode value : value_list) {

							int list_int_value = value.asLiteral().getInt();
							list_int_values.add(list_int_value);
						}
						d.list_values = list_int_values;
					} else if (value_range.equals(XSD.xboolean))
						d.allow_boolean_value = true;
					else if (value_range.equals(XSD.xint))
						d.allow_int_value = true;
					else if (value_range.equals(XSD.xdouble))
						d.allow_double_value = true;
					else if (value_range.equals(XSD.xstring))
						d.allow_string_value = true;
					else
						System.out.println("unknown value type: " + value_range.getLocalName() + " at property " + dp.getLocalName());
				}

				for (RDFNode comment_node : comments) {

					String comment = comment_node.asLiteral().getString();

					if (comment.startsWith("boolean:") || comment.startsWith("list:")) {

						String comment_tmp = comment.replace("boolean:", "");
						comment_tmp = comment_tmp.replace("list:", "");
						Map<Integer, String> value_meanings = parseComment(comment);
						if (value_meanings == null) {
							//System.out.println("can not parse comment: " + comment + " at property " + dp.getLocalName());
						}
						if (comment.startsWith("boolean:")) {

							Map<Boolean, String> boolean_value_meanings = new HashMap<Boolean, String>();
							String true_meaning = value_meanings.get(1);
							String false_meaning = value_meanings.get(0);
							boolean_value_meanings.put(true, true_meaning);
							boolean_value_meanings.put(false, false_meaning);
							d.boolean_value_descriptions = boolean_value_meanings;
						} else
							d.list_value_descriptions = value_meanings;
					} else if (comment.startsWith("double:") || comment.startsWith("int:") || comment.startsWith("string:")) {

						String comment_tmp = comment.replace("double:", "");
						comment_tmp = comment_tmp.replace("int:", "");
						comment_tmp = comment_tmp.replace("string:", "");
						if (comment.startsWith("double:"))
							d.double_value_description = comment_tmp;
						else if (comment.startsWith("int:"))
							d.int_value_description = comment_tmp;
						else
							d.string_value_description = comment_tmp;
					} else if (comment.startsWith("chn:")) {

						String comment_tmp = comment.replace("double:", "");
						d.property_chn_name = comment_tmp;
					} else {
						//System.out.println("unknown value type: " + comment + " at property " + dp.getLocalName());
					}
				}
			}
		}
	}

	public static Map<Integer, String> parseComment(String comment) {

		if (comment == null)
			return null;

		comment = comment.replace("（", "(");
		comment = comment.replace("）", ")");

		String[] lines = comment.split("\n");
		comment = lines[0].trim();

		Map<Integer, String> res = new HashMap<Integer, String>();

		int i = 0;
		int value_start = -1;
		int value_end = -1;
		int meaning_start = -1;
		int meaning_end = -1;
		char ch;
		while (i < comment.length()) {

			ch = comment.charAt(i);
			if (ch >= '0' && ch <= '9') {

				value_start = i;
				while (ch >= '0' && ch <= '9') {

					i++;
					if (i >= comment.length())
						break;
					ch = comment.charAt(i);
				}
				value_end = i - 1;
				int value = Integer.valueOf(comment.substring(value_start, value_end + 1));

				while (ch != '(') {

					i++;
					if (i >= comment.length())
						break;
					ch = comment.charAt(i);
				}
				meaning_start = i + 1;

				while (ch != ')') {

					i++;
					if (i >= comment.length())
						break;
					ch = comment.charAt(i);
				}
				meaning_end = i - 1;
				if (meaning_start < comment.length() && meaning_end < comment.length()) {

					String meaning = comment.substring(meaning_start, meaning_end + 1);
					res.put(value, meaning);
				}
				i++;
			} else
				i++;
		}

		if (res.size() == 0)
			return null;
		else
			return res;
	}

	public static class PropertyDescription {

		private String property_chn_name = null;
		private String property_eng_name = null;
		//private Resource value_type = null;

		private List<Integer> list_values = null;
		private Map<Integer, String> list_value_descriptions = null;
		private Map<Boolean, String> boolean_value_descriptions = null;
		private String double_value_description = null;
		private String int_value_description = null;
		private String string_value_description = null;

		private boolean tooth_related = false;

		private boolean allow_list_value = false;
		private boolean allow_boolean_value = false;
		private boolean allow_double_value = false;
		private boolean allow_int_value = false;
		private boolean allow_string_value = false;

		public List<Integer> getListValues() throws PropertyValueException {

			if (!allow_list_value)
				throw new PropertyValueException("incorrect value type");
			return this.list_values;
		}

		public Map<Integer, String> getListValueDescriptions() throws PropertyValueException {

			if (!allow_list_value)
				throw new PropertyValueException("incorrect value type");
			return this.list_value_descriptions;
		}

		public Map<Boolean, String> getBooleanValueDescriptions() throws PropertyValueException {

			if (!allow_boolean_value)
				throw new PropertyValueException("incorrect value type");
			return this.boolean_value_descriptions;
		}

		public String getIntValueDescription() throws PropertyValueException {

			if (!allow_int_value)
				throw new PropertyValueException("incorrect value type");
			return this.int_value_description;
		}

		public String getDoubleValueDescription() throws PropertyValueException {

			if (!allow_double_value)
				throw new PropertyValueException("incorrect value type");
			return this.double_value_description;
		}

		public String getStringValueDescription() throws PropertyValueException {

			if (!allow_string_value)
				throw new PropertyValueException("incorrect value type");
			return this.string_value_description;
		}

		public String getChnName() {
			return this.property_chn_name;
		}

		public String getEngName() {
			return this.property_eng_name;
		}

		public boolean isToothRelated() {
			return this.tooth_related;
		}
		
		/*public Resource getValueType() {
			return this.value_type;
		}*/
	}
}
