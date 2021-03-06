package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class RPIClasp extends Clasp {

	private OcclusalRest occlusal_rest = null;

	public RPIClasp(Tooth tooth_pos) {

		super(tooth_pos);
		this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);

	}


	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public Position getTipDirection() {

		return Position.Distal;
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("RPI卡环，");
		s.append("卡环臂尖朝向远中");
		return s.toString();
	}

	public String[] addComponents() {

		String s[] = new String[2];
		s[0] = ("RPI卡环");
		s[1] = ("卡环臂尖朝向远中");
		return s;
	}

	public String toString() {
		return this.print();
	}
}
