package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class ReverseBackActionClasp extends Clasp {

	private ClaspArm buccal_arm = null;
	private ClaspArm lingual_arm = null;
	private OcclusalRest occlusal_rest = null;

	public ReverseBackActionClasp(Tooth tooth_pos, ClaspMaterial buccal_material, ClaspMaterial lingual_material) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, buccal_material);
		this.lingual_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Lingual, lingual_material);
		this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
	}

	public ReverseBackActionClasp(Tooth tooth_pos, ClaspMaterial material) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, material);
		this.lingual_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Lingual, material);
		this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
	}

	public ReverseBackActionClasp(Tooth tooth_pos) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, ClaspMaterial.Cast);
		this.lingual_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Lingual, ClaspMaterial.Cast);
		this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
	}


	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public boolean isIndirectRetainer() {
		return this.occlusal_rest.isIndirectRetainer();
	}

	public Position getTipDirection() {

		return Position.Mesial;
	}

	public ClaspMaterial getMaterial() {
		if (this.buccal_arm != null) {
			return this.buccal_arm.getClaspMaterial();
		} else if (this.lingual_arm != null) {
			return this.lingual_arm.getClaspMaterial();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}

	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(super.toString());
		s.append("反回力（Reverse Back Action）卡环");

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
		s[0] = "反回力（Reverse Back Action）卡环";

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
