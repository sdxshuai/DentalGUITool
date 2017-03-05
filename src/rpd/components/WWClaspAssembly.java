package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class WWClaspAssembly implements ClaspAssembly {

	private WWClasp clasp = null;
	private LingualRest rest = null;
	private Plating plating = null;
	
	private boolean plated = false;
	
	public WWClaspAssembly(Tooth tooth_pos, Position tip_direction, boolean plated) {
		this.clasp = new WWClasp(tooth_pos, tip_direction);
		if(!plated)
			rest = new LingualRest(tooth_pos);
		else
			plating = new Plating(tooth_pos);
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		this.clasp.addToPlan(rpd_plan);
		if(!plated)
			this.rest.addToPlan(rpd_plan);
		else
			this.plating.addToPlan(rpd_plan);
	}
}
