package rpd.components;

import rpd.RPDPlan;
import rpd.oral.Tooth;
import java.util.ArrayList;

/**
 * Created by sdxshuai on 2017/5/8.
 */
public class DentureBase implements Component{

	protected ArrayList<Tooth> tooth_pos = null;

	protected String rule_explanation = null;

	public DentureBase(ArrayList<Tooth> tooth_pos) {
		this.tooth_pos = new ArrayList<Tooth>();
		this.tooth_pos.addAll(tooth_pos);
	}

	public DentureBase(Tooth tooth_pos) {
		this.tooth_pos = new ArrayList<Tooth>();
		this.tooth_pos.add(tooth_pos);
	}

	public ArrayList<Tooth> getToothPos() {
		return this.tooth_pos;
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
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
		return false;
	}

	@Override
	public boolean isPlating() {
		return false;
	}

	@Override
	public boolean isIndirectRetainer() {
		return false;
	}

	public String print() {
		StringBuilder s = new StringBuilder();
		for (Tooth tooth : this.tooth_pos) {
			s.append(tooth.toString());
		}
		s.append(": 基托（Denture Base）");
		return s.toString();
	}

	public String toString() {
		return this.print();
	}
}
