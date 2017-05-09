package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */
import rpd.oral.Maxillary;
import rpd.oral.Tooth;
import rpd.RPDPlan;
import rpd.conceptions.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SinglePalatalStrapConnector extends MajorConnector{
	
	public SinglePalatalStrapConnector(ArrayList<Tooth> tooth_pos) {
		super(tooth_pos);
		this.mandibular_or_maxillary = Position.Maxillary;
	}

	public SinglePalatalStrapConnector(Set<Tooth> abutment_teeth, Maxillary maxillary) {

		ArrayList<Tooth> abutment_missing_teeth = new ArrayList<>(abutment_teeth);
		List<Tooth> missing_teeth = maxillary.getMissingTeeth();
		abutment_missing_teeth.addAll(missing_teeth);

		this.tooth_pos = new ArrayList<>();
		ArrayList<Tooth> sorted_zone1 = new ArrayList<>();
		ArrayList<Tooth> sorted_zone2 = new ArrayList<>();
		for (Tooth tooth:abutment_missing_teeth) {
			if (tooth.getZone() == 1) {
				sorted_zone1.add(tooth);
			}
			else {
				sorted_zone2.add(tooth);
			}
		}
		Collections.sort(sorted_zone1);
		Collections.sort(sorted_zone2);
		if (sorted_zone1.size() > 1) {
			this.tooth_pos.add(sorted_zone1.get(sorted_zone1.size() - 1));
			this.tooth_pos.add(sorted_zone1.get(0));
		}
		else if (sorted_zone1.size() == 1) {
			this.tooth_pos.addAll(sorted_zone1);
			int single_pos = sorted_zone1.get(0).getNum();
			if (single_pos != 7) {
				Tooth neighbor_tooth = new Tooth(1, sorted_zone1.get(0).getNum() + 1);
				this.tooth_pos.add(neighbor_tooth);
			}
			else {
				Tooth neighbor_tooth = new Tooth(1, sorted_zone1.get(0).getNum() - 1);
				this.tooth_pos.add(neighbor_tooth);
			}
		}

		if (sorted_zone2.size() > 1) {
			this.tooth_pos.add(sorted_zone2.get(0));
			this.tooth_pos.add(sorted_zone2.get(sorted_zone2.size() - 1));
		}
		else if (sorted_zone2.size() == 1) {
			tooth_pos.addAll(sorted_zone2);
			int single_pos = sorted_zone2.get(0).getNum();
			if (single_pos != 7) {
				Tooth neighbor_tooth = new Tooth(2, sorted_zone2.get(0).getNum() + 1);
				this.tooth_pos.add(neighbor_tooth);
			}
			else {
				Tooth neighbor_tooth = new Tooth(2, sorted_zone2.get(0).getNum() - 1);
				this.tooth_pos.add(neighbor_tooth);
			}
		}
		this.mandibular_or_maxillary = Position.Maxillary;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("上颌腭带（Single Palatal Strap）");
		if (this.lingual_confrontation != null) {
			s.append("，舌侧对抗（");
			for (Tooth tooth:this.lingual_confrontation) {
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
