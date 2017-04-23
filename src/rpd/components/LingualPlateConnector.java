package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */

import rpd.oral.Tooth;
import rpd.RPDPlan;
import rpd.conceptions.Position;
import java.util.ArrayList;

public class LingualPlateConnector extends MajorConnector{
	
	public LingualPlateConnector(ArrayList<Tooth> tooth_pos) {
		super(tooth_pos);
		this.mandibular_or_maxillary = Position.Mandibular;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("上颌舌板（Lingual Plate）");
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
