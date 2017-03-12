package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class RingClaspAssembly implements ClaspAssembly {

	private RingClasp clasp = null;
	private OcclusalRest mesial_rest = null;
	//private OcclusalRest distal_rest = null;
	
	public RingClaspAssembly(Tooth tooth_pos, Position tip_position) {
		this.clasp = new RingClasp(tooth_pos);
		this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
	}
	
	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		this.clasp.addToPlan(rpd_plan);
		this.mesial_rest.addToPlan(rpd_plan);
	}
	
}
