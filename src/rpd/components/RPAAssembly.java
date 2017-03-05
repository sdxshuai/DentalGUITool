package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class RPAAssembly implements ClaspAssembly {

	private AkerClasp aker_clasp = null;
	
	private GuidingPlate guiding_plate = null;
	
	private OcclusalRest occlusal_rest = null;
	
	private Tooth tooth_pos = null;
	
	public RPAAssembly(Tooth tooth_pos, EdentulousType edentulous_type) {
		
		this.tooth_pos = tooth_pos;
		if(edentulous_type.equals(EdentulousType.PosteriorExtension)) {
			this.aker_clasp = new AkerClasp(this.tooth_pos, Position.Mesial);
			this.occlusal_rest = new OcclusalRest(this.tooth_pos, Position.Mesial);
			this.guiding_plate = new GuidingPlate(this.tooth_pos, Position.Distal);
		}
		else if(edentulous_type.equals(EdentulousType.AnteriorExtension)) {
			this.aker_clasp = new AkerClasp(this.tooth_pos, Position.Distal);
			this.occlusal_rest = new OcclusalRest(this.tooth_pos, Position.Distal);
			this.guiding_plate = new GuidingPlate(this.tooth_pos, Position.Mesial);	
		}
		else {}
	}
	
	public void addToPlan(RPDPlan rpd_plan) {
		this.aker_clasp.addToPlan(rpd_plan);
		this.guiding_plate.addToPlan(rpd_plan);
		this.occlusal_rest.addToPlan(rpd_plan);
	}
}
