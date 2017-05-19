package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class LingualPlateConnector extends MajorConnector {

	public LingualPlateConnector(ArrayList<Tooth> tooth_pos) {
		super(tooth_pos);
		this.mandibular_or_maxillary = Position.Mandibular;
	}

	public LingualPlateConnector(Set<Tooth> abutment_teeth) {
		this.tooth_pos = new ArrayList<>();
		ArrayList<Tooth> sorted_zone3 = new ArrayList<>();
		ArrayList<Tooth> sorted_zone4 = new ArrayList<>();
		for (Tooth tooth : abutment_teeth) {
			if (tooth.getZone() == 3) {
				sorted_zone3.add(tooth);
			} else {
				sorted_zone4.add(tooth);
			}
		}
		Collections.sort(sorted_zone3);
		Collections.sort(sorted_zone4);
		this.tooth_pos.add(sorted_zone3.get(sorted_zone3.size() - 1));
		this.tooth_pos.add(sorted_zone4.get(sorted_zone4.size() - 1));
		this.mandibular_or_maxillary = Position.Mandibular;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("下颌舌板（Lingual Plate）");
		if (this.lingual_confrontation != null) {
			s.append("，舌侧对抗（");
			for (Tooth tooth : this.lingual_confrontation) {
				s.append(" ");
				s.append(tooth.toString());
			}
			s.append("）");
		}

		return s.toString();
	}

	public String toString() {
		return this.print();
	}
}
