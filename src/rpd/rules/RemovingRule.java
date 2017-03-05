package rpd.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import exceptions.rpd.RuleException;
import rpd.RPDPlan;
import rpd.components.ClaspArm;
import rpd.components.Component;
import rpd.components.OcclusalRest;
import rpd.conceptions.Position;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则6
public class RemovingRule {

public static List<RemovingRule> removing_rules = null;
	
	private static Mouth mouth = null;
	
	public RPDPlan apply(RPDPlan rpd_plan) throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public static void initRules(Mouth mouth_obj) {
		
		mouth = mouth_obj;
		removing_rules = new ArrayList<RemovingRule>();
		
		removing_rules.add(new RemovingRule() {
			
			public int getRuleNum() throws RuleException {
				return 1;
			}
			
			public String getExplaination() throws RuleException {
				return "若一颗后牙远中近中皆有支托，则舌侧卡环臂可省略";
			}
			
			public RPDPlan apply(RPDPlan rpd_plan) throws RuleException {
				
				Set<ArrayList<Tooth>> abutment_teeth = rpd_plan.getAbutmentTeeth();
				List<Component> lingual_arms_to_remove = new ArrayList<Component>();
				for(ArrayList<Tooth> tooth : abutment_teeth) {
					
					Set<Component> tooth_components = rpd_plan.getComponents(tooth);
					List<Component> lingual_arms = new ArrayList<Component>();
					boolean has_mesial_rest = false;
					boolean has_distal_rest = false;
					for(Component component : tooth_components) {
						
						if(component.getClass().equals(OcclusalRest.class)) {
							OcclusalRest occlusal_rest = (OcclusalRest)component;
							if(occlusal_rest.getMesialOrDistal().equals(Position.Mesial))
								has_mesial_rest = true;
							else if(occlusal_rest.getMesialOrDistal().equals(Position.Distal))
								has_distal_rest = true;
						}
						
						if(component.getClass().equals(ClaspArm.class)) {
							ClaspArm clasp_arm = (ClaspArm)component;
							if(clasp_arm.getBuccalOrLingual().equals(Position.Lingual))
								lingual_arms.add(component);
						}
					}
					
					if(has_mesial_rest && has_distal_rest)
						lingual_arms_to_remove.addAll(lingual_arms);
				}
				
				if(lingual_arms_to_remove.size() > 0) {
					
					RPDPlan new_plan = new RPDPlan(rpd_plan);
					for(Component lingual_arm : lingual_arms_to_remove)
						new_plan.removeComponent(lingual_arm);
					return new_plan;
				}
				else
					return null;
			}
		});
	}
}
