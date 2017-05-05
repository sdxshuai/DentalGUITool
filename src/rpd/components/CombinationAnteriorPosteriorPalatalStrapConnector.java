package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */
import rpd.oral.Tooth;
import rpd.RPDPlan;
import rpd.conceptions.Position;
import java.util.ArrayList;
import java.util.Set;

public class CombinationAnteriorPosteriorPalatalStrapConnector extends MajorConnector{
	public CombinationAnteriorPosteriorPalatalStrapConnector(ArrayList<Tooth> tooth_pos) {
		super(tooth_pos);
		this.mandibular_or_maxillary = Position.Maxillary;
	}

	public CombinationAnteriorPosteriorPalatalStrapConnector(Set<Tooth> abutment_teeth) {

		this.tooth_pos = new ArrayList<>();
		this.tooth_pos.addAll(abutment_teeth);
		this.mandibular_or_maxillary = Position.Maxillary;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("下颌前后腭带（Combination Anterior and Posterior Palatal Strap）");
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
