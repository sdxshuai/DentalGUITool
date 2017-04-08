package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class RingClasp extends Clasp {

	private ClaspArm clasp_arm = null;
	private OcclusalRest mesial_rest = null;
	private OcclusalRest distal_rest = null;


	public RingClasp(Tooth tooth_pos, ClaspMaterial material) {

		super(tooth_pos);
		Position arm_position = null;
		if (tooth_pos.getZone() == 1 || tooth_pos.getZone() == 2) {
			arm_position =  Position.Buccal;
		}
		else if (tooth_pos.getZone() == 3 || tooth_pos.getZone() == 4) {
			arm_position = Position.Lingual;
		}
		else {
			System.out.println("No zone for tooth");
		}

		this.clasp_arm = new ClaspArm(tooth_pos, Position.Mesial, arm_position, material);

		if (material.equals(ClaspMaterial.Cast)) {
			this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
			this.distal_rest = new OcclusalRest(tooth_pos, Position.Distal);
		}
		else if (material.equals(ClaspMaterial.WW)) {
			this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
			this.distal_rest = null;
		}
	}

	public RingClasp(Tooth tooth_pos) {

		super(tooth_pos);
		Position arm_position = null;
		if (tooth_pos.getZone() == 1 || tooth_pos.getZone() == 2) {
			arm_position = Position.Buccal;
		}
		else if (tooth_pos.getZone() == 3 || tooth_pos.getZone() == 4) {
			arm_position = Position.Lingual;
		}
		else {
			System.out.println("No zone for tooth");
		}

		this.clasp_arm = new ClaspArm(tooth_pos, Position.Mesial, arm_position, ClaspMaterial.Cast);
		this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
		this.distal_rest = new OcclusalRest(tooth_pos, Position.Distal);
	}


	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("圈形（Ring）卡环，");
		return s.toString();
	}

	public String toString()  {
		return this.print();
	}
}
