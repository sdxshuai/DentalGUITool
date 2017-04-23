package rpd.components;

import exceptions.rpd.ComponentException;
import rpd.RPDPlan;
import rpd.oral.Tooth;

import java.util.ArrayList;

public interface Component {

	public void addToPlan(RPDPlan rpd_plan);

	public ArrayList<Tooth> getToothPos();

	public String print() throws ComponentException;

	public boolean isRest();

	public boolean isClasp();

	public boolean isGuidingPlate();

	public boolean isPlating();

	public boolean isIndirectRetainer();
}
