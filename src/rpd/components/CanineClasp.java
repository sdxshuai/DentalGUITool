package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class CanineClasp extends Clasp {


	private ClaspArm arm = null;

	private IncisalRest rest = null;

	public CanineClasp(Tooth tooth_pos, ClaspMaterial material) {

		super(tooth_pos);
		this.arm = new ClaspArm(tooth_pos, Position.Distal, Position.Buccal, material);
		this.rest = new IncisalRest(tooth_pos, Position.Mesial);
	}

	public CanineClasp(Tooth tooth_pos) {

		super(tooth_pos);
		this.arm = new ClaspArm(tooth_pos, Position.Distal, Position.Buccal, ClaspMaterial.WW);
		this.rest = new IncisalRest(tooth_pos, Position.Mesial);
	}

	public Position getTipDirection() {

		return Position.Distal;
	}

	public ClaspMaterial getMaterial() {
		if (this.arm != null) {
			return this.arm.getClaspMaterial();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}


	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public boolean isIndirectRetainer() {
		return this.rest.isIndirectRetainer();
	}


	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("尖牙(Canine)卡环");

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
		s[0] = "尖牙(Canine)卡环";

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
