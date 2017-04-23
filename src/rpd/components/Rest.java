package rpd.components;

import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;

public abstract class Rest implements Component {

	protected Position type;
	protected Tooth tooth_pos;
	protected String rule_explanation = null;

	protected Rest() {
	}

	public Position getType() throws Exception {
		return type;
	}

	public ArrayList<Tooth> getToothPos() {

		ArrayList<Tooth> tooth_pos_list = new ArrayList<Tooth>();
		tooth_pos_list.add(this.tooth_pos);
		return tooth_pos_list;
	}

	@Override
	public boolean isRest() {
		return true;
	}

	@Override
	public boolean isClasp() {
		return false;
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
		return true;
	}
}
