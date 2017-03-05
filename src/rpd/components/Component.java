package rpd.components;

import exceptions.rpd.ComponentException;
import rpd.RPDPlan;
import rpd.oral.Tooth;

public interface Component {

	public void addToPlan(RPDPlan rpd_plan);
	
	public Tooth getToothPos();
	
	public String print() throws ComponentException;
	
	public boolean isRest();
	
	public boolean isClasp();
	
	public boolean isGuidingPlate();
	
	public boolean isPlating();

	public boolean isIndirectRetainer();
}
