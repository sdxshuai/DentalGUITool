package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class IncisalRest extends Rest {

	public Position mesial_or_distal = null;

	public IncisalRest(Tooth tooth_pos, Position mesial_or_distal) {
		this.type = Position.Incisal;
		this.tooth_pos = tooth_pos;
		this.mesial_or_distal = mesial_or_distal;
	}

	public int hashCode() {
		return this.tooth_pos.hashCode() + this.type.hashCode() + this.mesial_or_distal.hashCode();
	}

	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;

		IncisalRest incisal_rest = (IncisalRest) obj;
		if (this.tooth_pos == incisal_rest.tooth_pos &&
				this.type == incisal_rest.type &&
				this.mesial_or_distal == incisal_rest.mesial_or_distal)

			return true;
		else
			return false;
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
		s.append(super.toString());
		if (mesial_or_distal.equals(Position.Mesial))
			s.append("近中");
		else if (mesial_or_distal.equals(Position.Distal))
			s.append("远中");
		else {
		}
		s.append("切支托");
		return s.toString();
	}

	public String toString() {
		return this.print();
	}
}
