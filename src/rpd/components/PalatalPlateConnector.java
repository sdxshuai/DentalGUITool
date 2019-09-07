package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Maxillary;
import rpd.oral.Tooth;

import java.util.*;

public class PalatalPlateConnector extends MajorConnector {

	public PalatalPlateConnector(ArrayList<Tooth> tooth_pos) {
		super(tooth_pos);
		this.mandibular_or_maxillary = Position.Maxillary;
	}

	public PalatalPlateConnector(Set<Tooth> abutment_teeth, Maxillary maxillary) {

		ArrayList<Tooth> abutment_missing_teeth = new ArrayList<>(abutment_teeth);
		List<Tooth> missing_teeth = maxillary.getMissingTeeth();
		abutment_missing_teeth.addAll(missing_teeth);

		this.tooth_pos = new ArrayList<>();
		this.lingual_confrontation = new HashSet<>();
		ArrayList<Tooth> sorted_zone1 = new ArrayList<>();
		ArrayList<Tooth> sorted_zone2 = new ArrayList<>();
		for (Tooth tooth : abutment_missing_teeth) {
			if (tooth.getZone() == 1) {
				sorted_zone1.add(tooth);
			} else {
				sorted_zone2.add(tooth);
			}
		}
		Collections.sort(sorted_zone1);
		Collections.sort(sorted_zone2);
		Tooth curTooth = null;
		curTooth = sorted_zone1.get(sorted_zone1.size() - 1);
		if (curTooth.getNum() == 3 && abutment_teeth.contains(curTooth)) {
			super.addLingualConfrontation(curTooth);
		}
		this.tooth_pos.add(curTooth);
		curTooth = sorted_zone1.get(0);
		if (curTooth.getNum() == 3 && abutment_teeth.contains(curTooth)) {
			super.addLingualConfrontation(curTooth);
		}
		this.tooth_pos.add(curTooth);
		curTooth = sorted_zone2.get(0);
		if (curTooth.getNum() == 3 && abutment_teeth.contains(curTooth)) {
			super.addLingualConfrontation(curTooth);
		}
		this.tooth_pos.add(curTooth);
		curTooth = sorted_zone2.get(sorted_zone2.size() - 1);
		if (curTooth.getNum() == 3 && abutment_teeth.contains(curTooth)) {
			super.addLingualConfrontation(curTooth);
		}
		this.tooth_pos.add(curTooth);
		this.mandibular_or_maxillary = Position.Maxillary;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("上颌腭板（Palatal Plate）");
		if (this.lingual_confrontation != null && this.lingual_confrontation.size() != 0) {
			s.append("，舌侧对抗（");
			for (Tooth tooth : this.lingual_confrontation) {
				s.append(" ");
				s.append(tooth.toString());
			}
			s.append("）");
		}

		return s.toString();
	}


	public String[] addComponents() {
		String s[] = new String[10];
		s[0] = "上颌腭板（Palatal Plate）";
		int i = 3;
		if (this.lingual_confrontation != null) {
			for (Tooth tooth : this.lingual_confrontation) {
				StringBuilder s1 = new StringBuilder();
				s1.append("舌侧对抗（");
				s1.append(tooth.toString());
				s1.append("）");
				s[i++] = s1.toString();
			}
		}
		return s;
	}

	public String toString() {
		return this.print();
	}
}
