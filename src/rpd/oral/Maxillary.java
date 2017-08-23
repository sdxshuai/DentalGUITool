package rpd.oral;

import exceptions.rpd.RuleException;
import ontologies.OntFunc;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import rpd.conceptions.*;

import java.util.ArrayList;
import java.util.List;

public class Maxillary {

	private List<Tooth> zone1 = new ArrayList<Tooth>();
	private List<Tooth> zone2 = new ArrayList<Tooth>();

	private List<EdentulousSpace> edentulous_spaces = null;

	public Maxillary(OntModel dental_ont) throws RuleException {
		init(dental_ont);
	}

	public static boolean[] getTeethMissingFlags(List<Tooth> zone1, List<Tooth> zone2) {

		boolean[] teeth_missing_flags = new boolean[17];
		for (int i = 1; i <= 8; i++) {
			Tooth tooth = zone1.get(i);
			teeth_missing_flags[9 - i] = tooth.isMissing();
		}
		for (int i = 1; i <= 8; i++) {
			Tooth tooth = zone2.get(i);
			teeth_missing_flags[8 + i] = tooth.isMissing();
		}
		return teeth_missing_flags;
	}

	public static List<EdentulousSpace> getEdentulousSpaces(List<Tooth> zone1, List<Tooth> zone2) throws RuleException {

		List<EdentulousSpace> edentulous_spaces = new ArrayList<EdentulousSpace>();
		boolean[] teeth_missing_flags = getTeethMissingFlags(zone1, zone2);
		int i = 1;
		int edentulous_space_start = -1;
		int edentulous_space_end = -1;
		while (i <= 16) {

			if (!teeth_missing_flags[i]) {
				i++;
				continue;
			} else {
				edentulous_space_start = i;
				int j = i + 1;
				while (j <= 16 && teeth_missing_flags[j])
					j++;
				edentulous_space_end = j - 1;

				int start_zone = -1;
				int start_number = -1;
				int end_zone = -1;
				int end_number = -1;

				if (edentulous_space_start <= 8) {
					start_zone = 1;
					start_number = 9 - edentulous_space_start;
				} else {
					start_zone = 2;
					start_number = edentulous_space_start - 8;
				}

				if (edentulous_space_end <= 8) {
					end_zone = 1;
					end_number = 9 - edentulous_space_end;
				} else {
					end_zone = 2;
					end_number = edentulous_space_end - 8;
				}

				if (start_zone == 1 && start_number == 8 && end_zone == 1 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}

				if (start_zone == 2 && start_number == 8 && end_zone == 2 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}

				Tooth left_neighbor = null;
				Tooth right_neighbor = null;
				if (start_number != 8) {
					if (start_zone == 1)
						left_neighbor = zone1.get(start_number + 1);
					else if (start_zone == 2 && start_number == 1)
						left_neighbor = zone1.get(1);
					else if (start_zone == 2 && start_number != 1)
						left_neighbor = zone2.get(start_number - 1);
					else {
					}
				}
				if (end_number != 8) {
					if (end_zone == 1 && end_number == 1)
						right_neighbor = zone2.get(1);
					else if (end_zone == 1 && end_number != 1)
						right_neighbor = zone1.get(end_number - 1);
					else if (end_zone == 2)
						right_neighbor = zone2.get(end_number + 1);
				}

				EdentulousSpace edentulous_space = new EdentulousSpace(left_neighbor, right_neighbor, Position.Maxillary);
				edentulous_spaces.add(edentulous_space);
				i = edentulous_space_end + 1;
			}
		}
		return edentulous_spaces;
	}

	public Tooth getTooth(int zone, int num) {
		if (zone == 1)
			return this.zone1.get(num);
		else if (zone == 2)
			return this.zone2.get(num);
		else
			return null;
	}

	public List<EdentulousSpace> getEdentulousSpaces() {
		return this.edentulous_spaces;
	}

	public void setEdentulousSpaces(List<EdentulousSpace> edentulousSpaceList) {
		this.edentulous_spaces = edentulousSpaceList;
	}

	public List<Tooth> getExistingTeeth() {
		List<Tooth> res = new ArrayList<Tooth>();
		for (Tooth tooth : zone1) {
			if (tooth != null) {
				if (!tooth.isMissing()) res.add(tooth);
			}
		}
		for (Tooth tooth : zone2) {
			if (tooth != null) {
				if (!tooth.isMissing()) res.add(tooth);
			}
		}
		return res;
	}

	private void init(OntModel dental_ont) throws RuleException {
		initZone1(dental_ont);
		initZone2(dental_ont);
		initEdentulousSpaces();
	}

	private void initZone1(OntModel dental_ont) {
		zone1.add(null);
		String tooth_class_uri = OntFunc.prefix + "tooth";
		for (int i = 1; i <= 8; i++) {
			Individual tooth_ind = dental_ont.getIndividual(tooth_class_uri + "1" + i);
			Tooth tooth_obj = new Tooth(1, i);
			this.readToothInfo(tooth_obj, dental_ont, tooth_ind);
			zone1.add(tooth_obj);
		}
	}

	public List<Tooth> getZone1() {
		return zone1;
	}

	public void setZone1(List<Tooth> zone1) {
		this.zone1 = zone1;
	}

	private void initZone2(OntModel dental_ont) {
		zone2.add(null);
		String tooth_class_uri = OntFunc.prefix + "tooth";
		for (int i = 1; i <= 8; i++) {
			Individual tooth_ind = dental_ont.getIndividual(tooth_class_uri + "2" + i);
			Tooth tooth_obj = new Tooth(2, i);
			this.readToothInfo(tooth_obj, dental_ont, tooth_ind);
			zone2.add(tooth_obj);
		}
	}

	public List<Tooth> getZone2() {
		return zone2;
	}

	public void setZone2(List<Tooth> zone2) {
		this.zone2 = zone2;
	}

	public boolean isZone1AllMissing() {
		boolean flag = true;
		for (Tooth tooth : zone1) {
			if (tooth != null) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	public boolean isZone2AllMissing() {
		boolean flag = true;
		for (Tooth tooth : zone2) {
			if (tooth != null) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	public boolean isZone1AllMissingExceptIncisor() {
		boolean flag = true;
		for (Tooth tooth : zone1) {
			if (tooth != null && tooth.getToothType() != ToothType.Incisor) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	public boolean isZone2AllMissingExceptIncisor() {
		boolean flag = true;
		for (Tooth tooth : zone2) {
			if (tooth != null && tooth.getToothType() != ToothType.Incisor) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	public boolean isZone1NoMissing() {
		boolean flag = true;
		for (Tooth tooth : zone1) {
			if (tooth != null && tooth.getNum() != 8) {
				if (tooth.isMissing()) {
					flag = false;
				}
			}
		}
		return flag;
	}

	public boolean isZone2NoMissing() {
		boolean flag = true;
		for (Tooth tooth : zone2) {
			if (tooth != null && tooth.getNum() != 8) {
				if (tooth.isMissing()) {
					flag = false;
				}
			}
		}
		return flag;
	}

	public boolean isZoneNoMissing(int zone) {
		if (zone == 1) {
			return isZone1NoMissing();
		} else if (zone == 2) {
			return isZone2NoMissing();
		} else {
			System.out.println("Error: wrong zone!");
			return false;
		}
	}

	public List<Tooth> getMissingTeeth() {
		List<Tooth> res = new ArrayList<Tooth>();
		for (Tooth tooth : zone1) {
			if (tooth != null && tooth.getNum() != 8) {
				if (tooth.isMissing()) res.add(tooth);
			}
		}
		for (Tooth tooth : zone2) {
			if (tooth != null && tooth.getNum() != 8) {
				if (tooth.isMissing()) res.add(tooth);
			}
		}
		return res;
	}

	public boolean isMissingFrontTeeth() {
		boolean flag = false;
		List<Tooth> missingTeeth = this.getMissingTeeth();
		for (Tooth tooth:missingTeeth) {
			if (tooth.getNum() <= 4) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public void initEdentulousSpaces() throws RuleException {

		this.edentulous_spaces = new ArrayList<EdentulousSpace>();
		boolean[] teeth_missing_flags = getTeethMissingFlags(this.zone1, this.zone2);

		int i = 1;
		int edentulous_space_start = -1;
		int edentulous_space_end = -1;
		while (i <= 16) {

			if (!teeth_missing_flags[i]) {
				i++;
				continue;
			} else {
				edentulous_space_start = i;
				int j = i + 1;
				while (j <= 16 && teeth_missing_flags[j])
					j++;
				edentulous_space_end = j - 1;

				int start_zone = -1;
				int start_number = -1;
				int end_zone = -1;
				int end_number = -1;

				if (edentulous_space_start <= 8) {
					start_zone = 1;
					start_number = 9 - edentulous_space_start;
				} else {
					start_zone = 2;
					start_number = edentulous_space_start - 8;
				}

				if (edentulous_space_end <= 8) {
					end_zone = 1;
					end_number = 9 - edentulous_space_end;
				} else {
					end_zone = 2;
					end_number = edentulous_space_end - 8;
				}

				if (start_zone == 1 && start_number == 8 && end_zone == 1 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}

				if (start_zone == 2 && start_number == 8 && end_zone == 2 && end_number == 8) {
					i = edentulous_space_end + 1;
					continue;
				}

				Tooth left_neighbor = null;
				Tooth right_neighbor = null;
				if (start_number != 8) {
					if (start_zone == 1)
						left_neighbor = zone1.get(start_number + 1);
					else if (start_zone == 2 && start_number == 1)
						left_neighbor = zone1.get(1);
					else if (start_zone == 2 && start_number != 1)
						left_neighbor = zone2.get(start_number - 1);
					else {
					}
				}
				if (end_number != 8) {
					if (end_zone == 1 && end_number == 1)
						right_neighbor = zone2.get(1);
					else if (end_zone == 1 && end_number != 1)
						right_neighbor = zone1.get(end_number - 1);
					else if (end_zone == 2)
						right_neighbor = zone2.get(end_number + 1);
				}

				EdentulousSpace edentulous_space = new EdentulousSpace(left_neighbor, right_neighbor, Position.Maxillary);
				this.edentulous_spaces.add(edentulous_space);
				i = edentulous_space_end + 1;
			}
		}
	}

	private void readToothInfo(Tooth tooth_obj, OntModel dental_ont, Individual tooth_ind) {

		DatatypeProperty missing_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "is_missing");
		RDFNode missing_value = tooth_ind.getPropertyValue(missing_dp);
		if (missing_value != null) {
			boolean missing_value_boolean = missing_value.asLiteral().getBoolean();
			tooth_obj.setMissing(missing_value_boolean);
		}

		DatatypeProperty bone_undercut_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "bone_undercut");
		RDFNode bone_undercut_value = tooth_ind.getPropertyValue(bone_undercut_dp);
		if (bone_undercut_value != null) {
			boolean bone_undercut_value_boolean = bone_undercut_value.asLiteral().getBoolean();
			tooth_obj.setBoneUndercut(bone_undercut_value_boolean);
		}

		DatatypeProperty buccal_surface_slope_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "buccal_surface_slope");
		RDFNode buccal_surface_slope_value = tooth_ind.getPropertyValue(buccal_surface_slope_dp);
		if (buccal_surface_slope_value != null) {
			boolean buccal_surface_slope_value_boolean = buccal_surface_slope_value.asLiteral().getBoolean();
			tooth_obj.setBuccalSurfaceSlope(buccal_surface_slope_value_boolean);
		}

		DatatypeProperty lingual_surface_slope_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "lingual_surface_slope");
		RDFNode lingual_surface_slope_value = tooth_ind.getPropertyValue(lingual_surface_slope_dp);
		if (lingual_surface_slope_value != null) {
			boolean lingual_surface_slope_value_boolean = lingual_surface_slope_value.asLiteral().getBoolean();
			tooth_obj.setLingualSurfaceSlope(lingual_surface_slope_value_boolean);
		}

		DatatypeProperty cingulum_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "cingulum");
		RDFNode cingulum_value = tooth_ind.getPropertyValue(cingulum_dp);
		if (cingulum_value != null) {
			boolean cingulum_value_boolean = cingulum_value.asLiteral().getBoolean();
			tooth_obj.setCingulum(cingulum_value_boolean);
		}

		DatatypeProperty torus_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "torus");
		RDFNode torus_value = tooth_ind.getPropertyValue(torus_dp);
		if (torus_value != null) {
			boolean torus_value_boolean = torus_value.asLiteral().getBoolean();
			tooth_obj.setTorus(torus_value_boolean);
		}

		DatatypeProperty mobility_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "mobility");
		RDFNode mobility_value = tooth_ind.getPropertyValue(mobility_dp);
		if (mobility_value != null) {
			int mobility_value_int = mobility_value.asLiteral().getInt();
			ToothMobility[] mobility_values = ToothMobility.values();
			tooth_obj.setMobility(mobility_values[mobility_value_int]);
		}

		DatatypeProperty crown_root_ratio_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "crown_root_ratio");
		RDFNode crown_root_ratio_value = tooth_ind.getPropertyValue(crown_root_ratio_dp);
		if (crown_root_ratio_value != null) {
			String datatype_uri = crown_root_ratio_value.asLiteral().getDatatypeURI();
			if (datatype_uri.equals("http://www.w3.org/2001/XMLSchema#int")) {
				int crown_root_ratio_int = crown_root_ratio_value.asLiteral().getInt();
				CrownRootRatio[] crown_root_ratio_values = CrownRootRatio.values();
				tooth_obj.setCrownRootRatio(crown_root_ratio_values[crown_root_ratio_int]);
			} else if (datatype_uri.equals("http://www.w3.org/2001/XMLSchema#double")) {
				double crown_root_ratio_double = crown_root_ratio_value.asLiteral().getDouble();
				if (crown_root_ratio_double > 0) {
					tooth_obj.setCrownRootRatio(CrownRootRatio.LONG);
				} else if (crown_root_ratio_double < 0) {
					tooth_obj.setCrownRootRatio(CrownRootRatio.SHORT);
				}
			}
		}

		DatatypeProperty furcation_involvement_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "furcation_involvement");
		RDFNode furcation_involvement_value = tooth_ind.getPropertyValue(furcation_involvement_dp);
		if (furcation_involvement_value != null) {
			String datatype_uri = furcation_involvement_value.asLiteral().getDatatypeURI();
			if (datatype_uri.equals("http://www.w3.org/2001/XMLSchema#int")) {
				int furcation_involvement_int = furcation_involvement_value.asLiteral().getInt();
				FurcationInvolvement[] furcation_involvement_values = FurcationInvolvement.values();
				tooth_obj.setFurcationInvolvement(furcation_involvement_values[furcation_involvement_int]);
			}
		}

		DatatypeProperty teeth_related_imaging_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "teeth_related_imaging");
		RDFNode teeth_related_imaging_value = tooth_ind.getPropertyValue(teeth_related_imaging_dp);
		if (teeth_related_imaging_value != null) {
			int teeth_related_imaging_int = teeth_related_imaging_value.asLiteral().getInt();
			AlveolarAbsorption[] alveolar_absorption_values = AlveolarAbsorption.values();
			tooth_obj.setAlveolarAbsorption(alveolar_absorption_values[teeth_related_imaging_int]);
		}

		DatatypeProperty classification_of_survey_line_on_buccal_surface_dp
				= dental_ont.getDatatypeProperty(OntFunc.prefix + "classification_of_survey_line_on_buccal_surface");
		RDFNode classification_of_survey_line_on_buccal_surface_value
				= tooth_ind.getPropertyValue(classification_of_survey_line_on_buccal_surface_dp);
		if (classification_of_survey_line_on_buccal_surface_value != null) {
			int classification_of_survey_line_on_buccal_surface_int
					= classification_of_survey_line_on_buccal_surface_value.asLiteral().getInt();
			ClassificationOfSurveyLineOnBuccalSurface[] classification_of_survey_line_on_buccal_surface_values
					= ClassificationOfSurveyLineOnBuccalSurface.values();
			tooth_obj.setClassificationOfSurveyLineOnBuccalSurface(
					classification_of_survey_line_on_buccal_surface_values[classification_of_survey_line_on_buccal_surface_int]);
		}

		DatatypeProperty space_below_gingival_margins_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "space_below_gingival_margins");
		RDFNode space_below_gingival_margins_value = tooth_ind.getPropertyValue(space_below_gingival_margins_dp);
		if (space_below_gingival_margins_value != null) {
			String datatype_uri = space_below_gingival_margins_value.asLiteral().getDatatypeURI();
			if (datatype_uri.equals("http://www.w3.org/2001/XMLSchema#boolean")) {
				boolean space_below_gingival_margins_boolean = space_below_gingival_margins_value.asLiteral().getBoolean();
				tooth_obj.setSpaceBelowGingivalMargins(space_below_gingival_margins_boolean);
			} else if (datatype_uri.equals("http://www.w3.org/2001/XMLSchema#double")) {
				double space_below_gingival_margins_double = space_below_gingival_margins_value.asLiteral().getDouble();
				if (space_below_gingival_margins_double > 7) {
					tooth_obj.setSpaceBelowGingivalMargins(false);
				} else if (space_below_gingival_margins_double < 7) {
					tooth_obj.setSpaceBelowGingivalMargins(true);
				}
			}
		}
		DatatypeProperty tooth_position_dp = dental_ont.getDatatypeProperty(OntFunc.prefix + "tooth_position");
		RDFNode tooth_position_value = tooth_ind.getPropertyValue(tooth_position_dp);
		if (tooth_position_value != null) {
			int tooth_position_value_int = tooth_position_value.asLiteral().getInt();
			ToothPosition[] tooth_position_values = ToothPosition.values();
			tooth_obj.setToothPosition(tooth_position_values[tooth_position_value_int]);
		}
	}
}


