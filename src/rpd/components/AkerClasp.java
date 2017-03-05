package rpd.components;

import exceptions.rpd.ComponentException;
import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class AkerClasp extends Clasp {

	//private Position tip_direction = null;
	//private ClaspMaterial material = null;
	
	private ClaspArm buccal_arm = null;
	
	public AkerClasp(Tooth tooth_pos, Position tip_direction) {
		super(tooth_pos);
		//this.tip_direction = tip_direction;
		//this.material = ClaspMaterial.Cast;
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, ClaspMaterial.Cast);
	}

	/*public Position getTipDirection() {
		return this.tip_direction;
	}
	
	public ClaspMaterial getMaterial() {
		return this.material;
	}*/

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this.buccal_arm);
	}
	
	public String print() throws ComponentException {
		throw new ComponentException("this should not be called");
	}
	
	/*public String print() {
		
		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("aker卡环，");
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
