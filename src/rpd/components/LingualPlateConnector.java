package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mandibular;
import rpd.oral.Tooth;

import java.util.*;

public class LingualPlateConnector extends MajorConnector {

	public LingualPlateConnector(ArrayList<Tooth> tooth_pos) {
		super(tooth_pos);
		this.mandibular_or_maxillary = Position.Mandibular;
	}

	public LingualPlateConnector(Set<Tooth> abutment_teeth, Mandibular mandibular) {
		ArrayList<Tooth> candidate_teeth = new ArrayList<>(abutment_teeth);
		List<EdentulousSpace> edentulousSpaces = mandibular.getEdentulousSpaces();

		for (EdentulousSpace edentulousSpace:edentulousSpaces) {
			if (edentulousSpace.getLeftNeighbor() == null && edentulousSpace.getRightNeighbor() != null) {
				candidate_teeth.add(edentulousSpace.getRightNeighbor());
			}
			if (edentulousSpace.getRightNeighbor() == null && edentulousSpace.getLeftNeighbor() != null) {
				candidate_teeth.add(edentulousSpace.getLeftNeighbor());
			}
		}

		this.tooth_pos = new ArrayList<>();
		ArrayList<Tooth> sorted_zone3 = new ArrayList<>();
		ArrayList<Tooth> sorted_zone4 = new ArrayList<>();
		for (Tooth tooth : candidate_teeth) {
			if (tooth.getZone() == 3) {
				sorted_zone3.add(tooth);
			} else {
				sorted_zone4.add(tooth);
			}
		}
		Collections.sort(sorted_zone3);
		Collections.sort(sorted_zone4);
		if (sorted_zone3.size() != 0) {
			this.tooth_pos.add(sorted_zone3.get(sorted_zone3.size() - 1));
			if (sorted_zone4.size() != 0) {
				this.tooth_pos.add(sorted_zone4.get(sorted_zone4.size() - 1));
			}
			else {
				this.tooth_pos.add(sorted_zone3.get(0));
			}
		}
		else {
			if (sorted_zone4.size() != 0){
				this.tooth_pos.add(sorted_zone4.get(sorted_zone4.size() - 1));
				this.tooth_pos.add(sorted_zone4.get(0));
			}
			else {
				System.out.println("There are no teeth left!");
			}
		}


		this.lingual_confrontation = new HashSet<>();
		for (Tooth tooth:abutment_teeth) {
			if (tooth.getNum() == 3) {
				super.addLingualConfrontation(tooth);
			}
		}
		this.mandibular_or_maxillary = Position.Mandibular;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("下颌舌板（Lingual Plate）");
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
		s[0] = "下颌舌板（Lingual Plate）";
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
