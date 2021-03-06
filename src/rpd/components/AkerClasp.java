package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class AkerClasp extends Clasp {

	private ClaspArm buccal_arm = null;
	private ClaspArm lingual_arm = null;
	private OcclusalRest occlusal_rest = null;
	private boolean enable_buccal = true;
	private boolean enable_lingual = true;

	public AkerClasp(Tooth tooth_pos, Position tip_direction, ClaspMaterial buccal_material,
	                 ClaspMaterial lingual_material) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, buccal_material);
		this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, lingual_material);
		if (tip_direction.equals(Position.Distal)) {
			this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
		} else if (tip_direction.equals(Position.Mesial)) {
			this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
		}
	}

	public AkerClasp(Tooth tooth_pos, Position tip_direction, ClaspMaterial material) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, material);
		this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, material);
		if (tip_direction.equals(Position.Distal)) {
			this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
		} else if (tip_direction.equals(Position.Mesial)) {
			this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
		}
	}

	public AkerClasp(Tooth tooth_pos, Position tip_direction) {

		super(tooth_pos);
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, ClaspMaterial.Cast);
		this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, ClaspMaterial.Cast);
		if (tip_direction.equals(Position.Distal)) {
			this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
		} else if (tip_direction.equals(Position.Mesial)) {
			this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
		}
	}

	public boolean isEnableBuccal() {
		return enable_buccal;
	}

	public void setEnableBuccal(boolean enable_buccal) {
		this.enable_buccal = enable_buccal;
	}

	public boolean isEnableLingual() {
		return enable_lingual;
	}

	public void setEnableLingual(boolean enable_lingual) {
		this.enable_lingual = enable_lingual;
	}

	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public boolean isIndirectRetainer() {
		return this.occlusal_rest.isIndirectRetainer();
	}

	public Position getTipDirection() {

		if (this.buccal_arm != null) {
			return this.buccal_arm.getTipDirection();
		} else if (this.lingual_arm != null) {
			return this.lingual_arm.getTipDirection();
		} else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}

	public void setTipDirection(Position tip_direction) {
		this.buccal_arm = new ClaspArm(this.getToothPos().get(0), tip_direction, Position.Buccal, ClaspMaterial.Cast);
		this.lingual_arm = new ClaspArm(this.getToothPos().get(0), tip_direction, Position.Lingual, ClaspMaterial.Cast);
		if (tip_direction.equals(Position.Distal)) {
			this.occlusal_rest = new OcclusalRest(this.getToothPos().get(0), Position.Mesial);
		} else if (tip_direction.equals(Position.Mesial)) {
			this.occlusal_rest = new OcclusalRest(this.getToothPos().get(0), Position.Distal);
		}
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
		s.append("Aker卡环，");

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
		s[0] = "Aker卡环";

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
