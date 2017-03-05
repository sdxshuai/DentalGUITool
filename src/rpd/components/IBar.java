package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class IBar extends Clasp {

	private Position originating = null;
	
	public IBar(Tooth tooth_pos, Position originating) {
		super(tooth_pos);
		this.originating = originating;
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	public Position getOriginating() {
		return this.originating;
	}
	
	public int hashCode() {
		return this.tooth_pos.hashCode() + this.originating.hashCode();
	}
	
	public boolean equals(Object obj) {
		
		if(obj == null)
			return false;
		if(!obj.getClass().equals(this.getClass()))
			return false;
		
		IBar ibar = (IBar)obj;
		if(this.tooth_pos == ibar.tooth_pos && 
			this.originating == ibar.originating)	
			return true;
		else
			return false;
	}
	
	@Override
	public String print() {
		
		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("I杆，");
		if(this.originating.equals(Position.Mesial))
			s.append("延伸自近中");
		if(this.originating.equals(Position.Distal))
			s.append("延伸自远中");
		return s.toString();
	}
	
	public String toString() {
		return this.print();
	}
}
