package rpd.components;

import rpd.RPDPlan;
import rpd.oral.Tooth;

import java.util.ArrayList;

public class Plating implements Component {

	private ArrayList<Tooth> tooth_pos = null;

	private String rule_explanation = null;

	public Plating(ArrayList<Tooth> tooth_pos) {
		this.tooth_pos = tooth_pos;
	}

	public Plating(Tooth tooth_pos) {

		ArrayList<Tooth> tooth_list = new ArrayList<Tooth>();
		tooth_list.add(tooth_pos);
		this.tooth_pos = tooth_list;
	}

	public ArrayList<Tooth> getToothPos() {
		return this.tooth_pos;
	}

	public int hashCode() {
		return this.tooth_pos.hashCode();
	}

	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;

		Plating plating = (Plating) obj;
		if (this.tooth_pos == plating.tooth_pos)
			return true;
		else
			return false;
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	@Override
	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("舌板覆盖");
		return s.toString();
	}

	public String toString() {
		return this.print();
	}

	@Override
	public boolean isRest() {
		return false;
	}

	@Override
	public boolean isClasp() {
		return false;
	}

	@Override
	public boolean isGuidingPlate() {
		return true;
	}

	@Override
	public boolean isPlating() {
		return false;
	}

	public boolean isIndirectRetainer() {
		return true;
	}
}
