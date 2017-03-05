package rpd.components;

import rpd.oral.Tooth;

public abstract class Clasp implements Component {

	protected Tooth tooth_pos = null;
	
	protected String rule_explanation = null;
	
	public Clasp(Tooth tooth_pos) {
		this.tooth_pos = tooth_pos;
	}
	
	public Tooth getToothPos() {
		return this.tooth_pos;
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
}
