package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class RPIAssembly implements ClaspAssembly {

	private IBar i_bar = null;
	
	private GuidingPlate guiding_plate = null;
	
	private OcclusalRest occlusal_rest = null;
	
	private Tooth tooth_pos = null;
	
	public RPIAssembly(Tooth tooth_pos, EdentulousType edentulous_type) {
		
		this.tooth_pos = tooth_pos;
		if(edentulous_type.equals(EdentulousType.PosteriorExtension)) {
			this.i_bar = new IBar(this.tooth_pos, Position.Distal);
			this.occlusal_rest = new OcclusalRest(this.tooth_pos, Position.Mesial);
			this.guiding_plate = new GuidingPlate(this.tooth_pos, Position.Distal);
		}
		else if(edentulous_type.equals(EdentulousType.AnteriorExtension)) {
			this.i_bar = new IBar(this.tooth_pos, Position.Mesial);
			this.occlusal_rest = new OcclusalRest(this.tooth_pos, Position.Distal);
			this.guiding_plate = new GuidingPlate(this.tooth_pos, Position.Mesial);	
		}
		else {}
	}
	
	public void addToPlan(RPDPlan rpd_plan) {
		this.i_bar.addToPlan(rpd_plan);
		this.guiding_plate.addToPlan(rpd_plan);
		this.occlusal_rest.addToPlan(rpd_plan);
	}
}
