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
			arm_position = Position.Buccal;
		} else if (tooth_pos.getZone() == 3 || tooth_pos.getZone() == 4) {
			arm_position = Position.Lingual;
		} else {
			System.out.println("No zone for tooth");
		}

		this.clasp_arm = new ClaspArm(tooth_pos, Position.Mesial, arm_position, material);

		if (material.equals(ClaspMaterial.Cast)) {
			this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
			this.distal_rest = new OcclusalRest(tooth_pos, Position.Distal);
		} else if (material.equals(ClaspMaterial.WW)) {
			this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
			this.distal_rest = null;
		}
	}

	public RingClasp(Tooth tooth_pos) {

		super(tooth_pos);
		Position arm_position = null;
		if (tooth_pos.getZone() == 1 || tooth_pos.getZone() == 2) {
			arm_position = Position.Buccal;
		} else if (tooth_pos.getZone() == 3 || tooth_pos.getZone() == 4) {
			arm_position = Position.Lingual;
		} else {
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

	@Override
	public boolean isIndirectRetainer() {
		if (this.distal_rest != null) {
			return this.distal_rest.isIndirectRetainer();
		} else if (this.mesial_rest != null) {
			return this.mesial_rest.isIndirectRetainer();
		} else {
			System.out.println("No rest!");
			return false;
		}
	}

	public ClaspMaterial getMaterial() {
		if (this.clasp_arm != null) {
			return this.clasp_arm.getClaspMaterial();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}

	public Position getTipDirection() {

		if (this.clasp_arm != null) {
			return this.clasp_arm.getTipDirection();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}


	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("圈形（Ring）卡环，");

		if (this.getMaterial().equals(ClaspMaterial.WW))
			s.append("弯制材料");
		else if (this.getMaterial().equals(ClaspMaterial.Cast))
			s.append("铸造材料");
		else {
		}

		return s.toString();
	}

	public String[] addComponents() {

		String s[] = new String[2];
		//s.append(super.toString());
		s[0] = "圈形（Ring）卡环";

		if (this.getMaterial().equals(ClaspMaterial.WW))
			s[1] = "弯制材料";
		else if (this.getMaterial().equals(ClaspMaterial.Cast))
			s[1] = "铸造材料";
		else {
		}

		return s;
	}

	public String toString() {
		return this.print();
	}
}
