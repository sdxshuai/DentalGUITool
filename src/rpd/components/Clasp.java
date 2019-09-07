package rpd.components;

import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;

public abstract class Clasp implements Component {

	protected ArrayList<Tooth> tooth_pos = null;

	protected String rule_explanation = null;

	public Clasp(ArrayList<Tooth> tooth_pos) {
		this.tooth_pos = tooth_pos;
	}

	public Clasp(Tooth tooth_pos) {

		this.tooth_pos = new ArrayList<Tooth>();
		this.tooth_pos.add(tooth_pos);
	}

	public ArrayList<Tooth> getToothPos() {
		return this.tooth_pos;
	}

	public Position getTipDirection() {
		return null;
	}

	public void setTipDirection(Position tip_direction) {}

	public ClaspMaterial getMaterial() {
		return null;
	}

	@Override
	public boolean isRest() {
		return false;
	}

	@Override
	public boolean isClasp() {
		return true;
	}

	@Override
	public boolean isGuidingPlate() {
		return false;
	}

	@Override
	public boolean isPlating() {
		return false;
	}

	public boolean isIndirectRetainer() {
		return false;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Tooth tooth : this.tooth_pos) {
			s.append(tooth.toString());
		}
		s.append(": ");
		return s.toString();
	}
}
