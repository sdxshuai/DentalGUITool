package rpd.components;

import rpd.conceptions.Position;
import rpd.oral.Tooth;

public abstract class Rest implements Component {

	protected Position type;
	protected Tooth tooth_pos;
	protected String rule_explanation = null;
	
	protected Rest() {}
	
	public Position getType() throws Exception {
		return type;
	}
	
	public Tooth getToothPos() {
		return tooth_pos;
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
