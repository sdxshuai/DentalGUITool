package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;

public class GuidingPlate implements Component {

	private ArrayList<Tooth> tooth_pos = null;
	private Position mesial_or_distal = null;
	
	private String rule_explanation = null;
	
	public GuidingPlate(ArrayList<Tooth> tooth_pos, Position mesial_or_distal) {
		this.tooth_pos = tooth_pos;
		this.mesial_or_distal = mesial_or_distal;
	}

	public GuidingPlate(Tooth tooth_pos, Position mesial_or_distal) {
		ArrayList<Tooth> tooth_list = new ArrayList<Tooth>();
		tooth_list.add(tooth_pos);
		this.tooth_pos = tooth_list;
		this.mesial_or_distal = mesial_or_distal;
	}
	
	public int hashCode() {
		return this.tooth_pos.hashCode() + this.mesial_or_distal.hashCode();
	}
	
	public boolean equals(Object obj) {
		
		if(obj == null)
			return false;
		if(!obj.getClass().equals(this.getClass()))
			return false;
		
		GuidingPlate guiding_plate = (GuidingPlate)obj;
		if(this.tooth_pos == guiding_plate.tooth_pos && 
			this.mesial_or_distal == guiding_plate.mesial_or_distal)	
			return true;
		else
			return false;
	}
	
	public ArrayList<Tooth> getToothPos() {
		return this.tooth_pos;
	}
	
	public Position getMesialOrDistal() {
		return this.mesial_or_distal;
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	@Override
	public String print() {
		
		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		if(mesial_or_distal.equals(Position.Mesial))
			s.append("近中导平面板");
		else if(mesial_or_distal.equals(Position.Distal))
			s.append("远中导平面板");
		else {}
			//throw new ComponentException("illegal guiding plate direction: " + mesial_or_distal.name());
		return s.toString();
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
		return true;
	}

	@Override
	public boolean isPlating() {
		return false;
	}
	
	public boolean isIndirectRetainer() {
		return false;
	}
}
