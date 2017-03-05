package ontologies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;

//扩展jena功能
public class OntFunc {

	public static String prefix = "http://www.semanticweb.org/msiip/ontologies/CDSSinRPD#";

	//get top level data
	public static Set<DatatypeProperty> getTopDataProperties(OntModel dental_ont) {
		
		Set<DatatypeProperty> top_dps = new HashSet<DatatypeProperty>();
		ExtendedIterator<DatatypeProperty> data_ps = dental_ont.listDatatypeProperties();
		while(data_ps.hasNext()) {
		
			DatatypeProperty dp = data_ps.next();
			if(dp.listSuperProperties().toList().size() == 0)
				top_dps.add(dp);
		}
		
		return top_dps;
	}

	//取出子属性
	public static List<? extends OntProperty> getSubProperties(OntProperty dp) {
		
		ExtendedIterator<? extends OntProperty> sub_dps = dp.listSubProperties();
		List<? extends OntProperty> sub_dps_list = sub_dps.toList();
		if(sub_dps_list.size() == 0)
			return null;
		else
			return sub_dps_list;
	}
	
	public static String getComment(OntResource r) {
		
		/*Resource c = r.getPropertyResourceValue(RDFS.comment);
		if(c == null)
			return null;
		else
			return c.asLiteral().getString();*/
		String c = r.getComment(null);
		return c;
	}
	
	public static boolean isRDFList(Resource datatype) {
		
		Resource r = datatype.getPropertyResourceValue(OWL.oneOf);
		return (r != null);
	}
	
}
