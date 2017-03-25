package rpd.oral;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntModel;

import exceptions.rpd.ToothPosException;

public class Maxillary {

	private List<Tooth> zone1 = new ArrayList<Tooth>();
	private List<Tooth> zone2 = new ArrayList<Tooth>();
	
	private List<EdentulousSpace> edentulous_spaces = null;
	
	public Maxillary(OntModel dental_ont) {	
		init(dental_ont);
	}
	
	public Tooth getTooth(int zone, int num) {
		if(zone == 1)
			return this.zone1.get(num);
		else if(zone == 2)
			return this.zone2.get(num);
		else
			return null;
	}
	
	public List<EdentulousSpace> getEdentulousSpaces() {
		return this.edentulous_spaces;
	}
	
	private void init(OntModel dental_ont) {
		initZone1(dental_ont);
		initZone2(dental_ont);
		initEdentulousSpaces();
	}
	
	private void initZone1(OntModel dental_ont) {
		zone1.add(null);
		for(int i = 1; i <= 8; i++)
			zone1.add(new Tooth(1, i));
	}
	
	private void initZone2(OntModel dental_ont) {
		zone2.add(null);
		for(int i = 1; i <= 8; i++)
			zone2.add(new Tooth(2, i));
	}
	
	private void initEdentulousSpaces() {
		this.edentulous_spaces = new ArrayList<EdentulousSpace>();
	}

	public List<Tooth> getExistingTeeth() {
		List<Tooth> res = new ArrayList<Tooth>();
		for (Tooth tooth:zone1) {
			if (!tooth.isMissing()) res.add(tooth);
		}
		for (Tooth tooth:zone2) {
			if (!tooth.isMissing()) res.add(tooth);
		}
		return res;
	}

	public boolean isZone1AllMissing() {
		boolean flag = true;
		for (Tooth tooth:zone1) {
			if (tooth!=null) {
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
		for (Tooth tooth:zone2) {
			if (tooth!=null) {
				if (!tooth.isMissing()) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

}


