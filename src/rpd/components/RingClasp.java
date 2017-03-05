package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class RingClasp extends Clasp {

	private Position tip_position = null;
	
	public RingClasp(Tooth tooth_pos, Position tip_position) {
		super(tooth_pos);
		this.tip_position = tip_position;
	}

	public int hashCode() {
		return this.tooth_pos.hashCode() + this.tip_position.hashCode();
	}
	
	public boolean equals(Object obj) {
		
		if(obj == null)
			return false;
		if(!obj.getClass().equals(this.getClass()))
			return false;
		
		RingClasp ring_clasp = (RingClasp)obj;
		if(this.tooth_pos == ring_clasp.tooth_pos &&
			this.tip_position == ring_clasp.tip_position)
			return true;
		else
			return false;
	}
	
	public Position getTipPosition() {
		return this.tip_position;
	}
	
	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	@Override
	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("圈形卡环，");
		if(tip_position.equals(Position.Buccal))
			s.append("卡环臂尖位于颊侧");
		else if(tip_position.equals(Position.Lingual))
			s.append("卡环臂尖位于舌侧");
		else {}
			//throw new ComponentException("illegal clasp tip position: " + tip_position.name());
		return s.toString();
	}
	
	public String toString() {
		return this.print();
	}
}
