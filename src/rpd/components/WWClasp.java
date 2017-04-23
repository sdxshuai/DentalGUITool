package rpd.components;

import exceptions.rpd.ComponentException;
import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

//弯制--WW
public class WWClasp extends Clasp implements Component {

	//private Position tip_direction = null;
	//private ClaspMaterial material = ClaspMaterial.WW;

	private ClaspArm buccal_arm = null;

	public WWClasp(Tooth tooth_pos, Position tip_direction) {
		super(tooth_pos);
		//this.tip_direction = tip_direction;
		this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, ClaspMaterial.WW);
	}
	
	/*public Position getTipdirection() {
		return this.tip_direction;
	}*/

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this.buccal_arm);
	}
	
	/*public ClaspMaterial getMaterial() {
		return this.material;
	}*/

	public String print() throws ComponentException {
		throw new ComponentException("this should not be called");
	}
	
	/*@Override
	public String print() {
		
		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("颊侧弯制卡环，");
		if(tip_direction.equals(Position.Mesial))
			s.append("卡环臂尖朝向近中");
		else if(tip_direction.equals(Position.Distal))
			s.append("卡环臂尖朝向远中");
		else {}
			//throw new ComponentException("illegal clasp tip direction: " + tip_direction.name());
		return s.toString();
	}
	
	public String toString() {
		return this.print();
	}*/
}
