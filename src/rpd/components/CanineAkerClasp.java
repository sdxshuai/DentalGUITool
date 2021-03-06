package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class CanineAkerClasp extends Clasp {

	private ClaspArm buccal_arm = null;
	private LingualRest lingual_rest = null;

	public CanineAkerClasp(Tooth tooth_pos, Position tip_direction, ClaspMaterial material) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, material);
		this.lingual_rest = new LingualRest(tooth_pos);
	}

	public CanineAkerClasp(Tooth tooth_pos, Position tip_direction) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, ClaspMaterial.WW);
		this.lingual_rest = new LingualRest(tooth_pos);
	}


	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public boolean isIndirectRetainer() {
		return this.lingual_rest.isIndirectRetainer();
	}

	public Position getTipDirection() {

		if (this.buccal_arm != null) {
			return this.buccal_arm.getTipDirection();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
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
		s.append("尖牙Aker（Canine Aker）卡环，");

		if (this.getMaterial().equals(ClaspMaterial.WW))
			s.append("弯制材料，");
		else if (this.getMaterial().equals(ClaspMaterial.Cast))
			s.append("铸造材料，");
		else {
		}

		if (this.getTipDirection().equals(Position.Mesial))
			s.append("卡环臂尖朝向近中");
		else if (this.getTipDirection().equals(Position.Distal))
			s.append("卡环臂尖朝向远中");
		else {
		}

		return s.toString();
	}

	public String[] addComponents() {

		String s[] = new String[3];
		//s.append(super.toString());
		s[0] = "尖牙Aker（Canine Aker）卡环";

		if (this.getMaterial().equals(ClaspMaterial.WW))
			s[1] = "弯制材料";
		else if (this.getMaterial().equals(ClaspMaterial.Cast))
			s[1] = "铸造材料";
		else {
		}

		if (this.getTipDirection().equals(Position.Mesial))
			s[2] = "卡环臂尖朝向近中";
		else if (this.getTipDirection().equals(Position.Distal))
			s[2] = "卡环臂尖朝向远中";
		else {
		}

		return s;
	}
	public String toString() {
		return this.print();
	}
}


