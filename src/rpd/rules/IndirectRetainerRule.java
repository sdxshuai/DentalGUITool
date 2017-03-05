package rpd.rules;

import java.util.ArrayList;
import java.util.List;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.Component;
import rpd.components.RotationAxis;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;

//规则4
public class IndirectRetainerRule {

	public static List<IndirectRetainerRule> indirect_retainer_rules = null;
	
	private static Mouth mouth = null;
	
	public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ToothPosException, ClaspAssemblyException, EdentulousTypeException {
		throw new RuleException("call from abstract class");
	}
	
	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	private static RotationAxis findRotationAxis(EdentulousSpace edentulous_space, RPDPlan rpd_plan) {
		
		EdentulousType edentulous_type = edentulous_space.getEdentulousType();
		if(edentulous_type.equals(EdentulousType.PosteriorExtension))
			return findPosteriorExtensionRotationAxis(edentulous_space, rpd_plan);
		else if(edentulous_type.equals(EdentulousType.AnteriorExtension))
			return findAnteriorExtensionRotationAxis(edentulous_space, rpd_plan);
		else
			return null;
	}
	
	private static RotationAxis findAnteriorExtensionRotationAxis(EdentulousSpace edentulous_space, RPDPlan rpd_plan) {
		
		Component left_indirect_retainer = null;
		Component right_indirect_retainer = null;
		
		int left_min_num = 9;
		int right_min_num = 9;
		
		for(Component component : rpd_plan.getComponents()) {
			
			if(!component.isIndirectRetainer())
				continue;
			int zone = component.getToothPos().get(0).getZone();
			int num = component.getToothPos().get(0).getNum();
			if(zone == 4 || zone == 1) {
				if(num < left_min_num) {
					left_min_num = num;
					left_indirect_retainer = component;
				}
			}
			else {
				if(num < right_min_num) {
					right_min_num = num;
					right_indirect_retainer = component;
				}
			}
		}
		
		if(left_indirect_retainer == null || right_indirect_retainer == null)
			return null;
		else
			return new RotationAxis(left_indirect_retainer, right_indirect_retainer, edentulous_space);
	}
	
	private static RotationAxis findPosteriorExtensionRotationAxis(EdentulousSpace edentulous_space, RPDPlan rpd_plan) {
		
		Component left_indirect_retainer = null;
		Component right_indirect_retainer = null;
		
		int left_max_num = -1;
		int right_max_num = -1;
		
		for(Component component : rpd_plan.getComponents()) {
			
			if(!component.isIndirectRetainer())
				continue;
			int zone = component.getToothPos().get(0).getZone();
			int num = component.getToothPos().get(0).getNum();
			if(zone == 4 || zone == 1) {
				if(num > left_max_num) {
					left_max_num = num;
					left_indirect_retainer = component;
				}
			}
			else {
				if(num > right_max_num) {
					right_max_num = num;
					right_indirect_retainer = component;
				}
			}
		}
		
		if(left_indirect_retainer == null || right_indirect_retainer == null)
			return null;
		else
			return new RotationAxis(left_indirect_retainer, right_indirect_retainer, edentulous_space);
	}
	
	public static void initRules(Mouth mouth_obj) {
		
		mouth = mouth_obj;
		indirect_retainer_rules = new ArrayList<IndirectRetainerRule>();
		
		indirect_retainer_rules.add(new IndirectRetainerRule() {
			
			public int getRuleNum() throws RuleException {
				return 1;
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public String getExplaination() {
				return "游离缺失，若转动轴前侧无间接固位体，则增设间接固位体";
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws EdentulousTypeException, ToothPosException {
				
				EdentulousType edentulous_type = edentulous_space.getEdentulousType();
				if(!edentulous_type.equals(EdentulousType.PosteriorExtension))
					return null;
				else {
					
					RotationAxis rotation_axis = findRotationAxis(edentulous_space, rpd_plan);
					if(rotation_axis == null)
						return null;
					
					if(rotation_axis.needIndirectRetainer(rpd_plan, edentulous_space)) {
						
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						Component indirect_retainer = rotation_axis.properIndirectRetainet(mouth, Position.Mandibular);
						new_plan.addComponent(indirect_retainer);
						return new_plan;
					}
					else
						return null;
				}
			}
		});
	}
}
