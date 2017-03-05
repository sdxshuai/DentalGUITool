package rpd.components;

import exceptions.rpd.ClaspAssemblyException;
import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class CircumferencialClaspAssembly implements ClaspAssembly {

	private CircumferentialClasp clasp = null;
	private OcclusalRest rest = null;
	
	public CircumferencialClaspAssembly(Tooth tooth_pos, Position tip_direction) throws ClaspAssemblyException {
		this.clasp = new CircumferentialClasp(tooth_pos, tip_direction, ClaspMaterial.WW, ClaspMaterial.Cast);
		if(tip_direction.equals(Position.Mesial))
			rest = new OcclusalRest(tooth_pos, Position.Distal);
		else if(tip_direction.equals(Position.Distal))
			rest = new OcclusalRest(tooth_pos, Position.Mesial);
		else
			throw new ClaspAssemblyException("tip_direction must be mesial or distal");
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		clasp.addToPlan(rpd_plan);
		rest.addToPlan(rpd_plan);
	}
}
