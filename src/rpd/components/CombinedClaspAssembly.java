package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class CombinedClaspAssembly implements ClaspAssembly {

	private CircumferentialClasp anterior_clasp = null;
	private CircumferentialClasp posterior_clasp = null;
	
	private OcclusalRest anterior_rest = null;
	private OcclusalRest posterior_rest = null;
	
	private Tooth anterior_tooth = null;
	private Tooth posterior_tooth = null;
	
	public CombinedClaspAssembly(Tooth anterior_tooth, Tooth posterior_tooth) {
		
		this.anterior_tooth = anterior_tooth;
		this.anterior_rest = new OcclusalRest(anterior_tooth, Position.Distal);
		this.posterior_rest = new OcclusalRest(posterior_tooth, Position.Mesial);
		this.anterior_clasp = new CircumferentialClasp(anterior_tooth, Position.Mesial, ClaspMaterial.WW, ClaspMaterial.Cast);
		this.posterior_clasp = new CircumferentialClasp(posterior_tooth, Position.Distal, ClaspMaterial.WW, ClaspMaterial.Cast);
	}

	public Tooth getAnteriorTooth() {
		return this.anterior_tooth;
	}
	
	public Tooth getPosteriorTooth() {
		return this.posterior_tooth;
	}
	
	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		anterior_clasp.addToPlan(rpd_plan);
		posterior_clasp.addToPlan(rpd_plan);
		anterior_rest.addToPlan(rpd_plan);
		posterior_rest.addToPlan(rpd_plan);
	}
}
