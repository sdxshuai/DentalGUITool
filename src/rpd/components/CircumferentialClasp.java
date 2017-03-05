package rpd.components;

import exceptions.rpd.ComponentException;
import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class CircumferentialClasp extends Clasp implements Component {

	//private Position tip_direction = null;
	
	private ClaspArm buccal_arm = null;
	private ClaspArm lingual_arm = null;
	
	/*public CircumferentialClasp(Tooth tooth_pos, Position tip_direction) {
		super(tooth_pos);
		//this.tip_direction = tip_direction;
	}*/
	
	public CircumferentialClasp(Tooth tooth_pos, Position tip_direction, ClaspMaterial buccal_material, ClaspMaterial lingual_material) {
		super(tooth_pos);
		//this.tip_direction = tip_direction;
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, buccal_material);
		this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, lingual_material);
	}
	
	/*public void setBuccalMaterial(ClaspMaterial material) {
		this.buccal_arm.setClaspMaterial(material);
	}
	
	public void setlLingualMaterial(ClaspMaterial material) {
		this.lingual_arm.setClaspMaterial(material);
	}*/
	
	/*public Position getTipdirection() {
		return this.tip_direction;
	}*/
	
	/*public ClaspMaterial getBuccalMaterial() {
		return this.lingual_arm.getClaspMaterial();
	}
	
	public ClaspMaterial getLingualMaterial() {
		return this.buccal_arm.getClaspMaterial();
	}*/

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this.buccal_arm);
		rpd_plan.addComponent(this.lingual_arm);
	}
	
	public String print() throws ComponentException {
		throw new ComponentException("this should not be called");
	}

	/*@Override
	public String print() {
		
		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		if(buccal_material != null && buccal_material.equals(ClaspMaterial.WW))
			s.append("颊侧弯制卡环，");
		else if(buccal_material != null && buccal_material.equals(ClaspMaterial.Cast))
			s.append("颊侧铸造卡环，");
		else {}
			
		if(lingual_material != null && lingual_material.equals(ClaspMaterial.WW))
			s.append("舌侧弯制卡环，");
		else if(lingual_material != null && lingual_material.equals(ClaspMaterial.Cast))
			s.append("舌侧铸造卡环，");
		else {}
		
		if(tip_direction.equals(Position.Mesial))
			s.append("卡环臂尖朝向近中");
		else if(tip_direction.equals(Position.Distal))
			s.append("卡环臂尖朝向远中");
		else {}
			
		return s.toString();
	}
	
	public String toString() {
		return this.print();
	}*/
}
