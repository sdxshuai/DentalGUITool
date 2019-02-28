package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;

public class ClaspArm implements Component {

	private Tooth tooth_pos = null;
	private Position tip_direction = null;
	private Position buccal_or_lingual = null;
	private ClaspMaterial material = null;
	public ClaspArm(Tooth tooth_pos, Position tip_direction,
	                Position buccal_or_lingual, ClaspMaterial material) {

		this.tooth_pos = tooth_pos;
		this.tip_direction = tip_direction;
		this.buccal_or_lingual = buccal_or_lingual;
		this.material = material;
	}

	public static void main(String[] args) {

		Tooth tooth1 = new Tooth(3, 4);
		Tooth tooth2 = new Tooth(3, 4);
		Component clasp_arm1 = new ClaspArm(tooth1, Position.Mesial, Position.Buccal, ClaspMaterial.WW);
		Component clasp_arm2 = new ClaspArm(tooth2, Position.Mesial, Position.Buccal, ClaspMaterial.WW);
		System.out.println(clasp_arm1.equals(clasp_arm2));
	}

	public int hashCode() {
		return this.tooth_pos.hashCode() + this.tip_direction.hashCode() +
				this.buccal_or_lingual.hashCode() + this.material.hashCode();
	}

	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;

		ClaspArm clasp_arm = (ClaspArm) obj;
		if (this.tooth_pos == clasp_arm.tooth_pos &&
				this.tip_direction == clasp_arm.tip_direction &&
				this.buccal_or_lingual == clasp_arm.buccal_or_lingual &&
				this.material == clasp_arm.material)

			return true;
		else
			return false;
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public Position getTipDirection() {
		return this.tip_direction;
	}

	public Position getBuccalOrLingual() {
		return this.buccal_or_lingual;
	}

	public ClaspMaterial getClaspMaterial() {
		return this.material;
	}

	public void setClaspMaterial(ClaspMaterial material) {
		this.material = material;
	}

	@Override
	public ArrayList<Tooth> getToothPos() {

		ArrayList<Tooth> tooth_list = new ArrayList<Tooth>();
		tooth_list.add(tooth_pos);
		return tooth_list;
	}

	@Override
	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		if (buccal_or_lingual.equals(Position.Buccal))
			s.append("颊侧");
		else if (buccal_or_lingual.equals(Position.Lingual))
			s.append("舌侧");
		else {
		}

		if (material.equals(ClaspMaterial.WW))
			s.append("弯制卡环，");
		else if (material.equals(ClaspMaterial.Cast))
			s.append("铸造卡环，");
		else {
		}

		if (tip_direction.equals(Position.Mesial))
			s.append("卡环臂尖朝向近中");
		else if (tip_direction.equals(Position.Distal))
			s.append("卡环臂尖朝向远中");
		else {
		}

		return s.toString();
	}

	public String[] addComponents() {

		String s[] = new String[4];
		//s.append(super.toString());
		s[0] = "卡环臂（Clasp Arm）";

		//s.append(this.tooth_pos.toString() + ":");
		if (buccal_or_lingual.equals(Position.Buccal))
			s[1] = "颊侧";
		else if (buccal_or_lingual.equals(Position.Lingual))
			s[1] = "舌侧";
		else {
		}

		if (material.equals(ClaspMaterial.WW))
			s[2] = "弯制卡环";
		else if (material.equals(ClaspMaterial.Cast))
			s[2] = "铸造卡环";
		else {
		}

		if (tip_direction.equals(Position.Mesial))
			s[3] = "卡环臂尖朝向近中";
		else if (tip_direction.equals(Position.Distal))
			s[3] = "卡环臂尖朝向远中";
		else {
		}

		return s;
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
}
