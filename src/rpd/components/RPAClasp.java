package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class RPAClasp extends Clasp {

	private OcclusalRest occlusal_rest = null;
	private ClaspArm buccal_arm = null;

	public RPAClasp(Tooth tooth_pos) {

		super(tooth_pos);
		this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
		this.buccal_arm = new ClaspArm(tooth_pos, Position.Distal, Position.Buccal, ClaspMaterial.Cast);
	}

	public RPAClasp(Tooth tooth_pos, ClaspMaterial material) {

		super(tooth_pos);
		this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
		this.buccal_arm = new ClaspArm(tooth_pos, Position.Distal, Position.Buccal, material);
	}


	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public boolean isIndirectRetainer() {
		return this.occlusal_rest.isIndirectRetainer();
	}

	public Position getTipDirection() {

		return Position.Distal;
	}

	public ClaspMaterial getMaterial() {
		if (this.buccal_arm != null) {
			return this.buccal_arm.getClaspMaterial();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("RPA卡环，");

		if (this.getMaterial().equals(ClaspMaterial.WW))
			s.append("弯制材料，");
		else if (this.getMaterial().equals(ClaspMaterial.Cast))
			s.append("铸造材料，");
		else {
		}

		s.append("卡环臂尖朝向远中");
		return s.toString();
	}

	public String toString() {
		return this.print();
	}
}
