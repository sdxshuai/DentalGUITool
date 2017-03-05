package rpd.rules;

import java.util.ArrayList;
import java.util.List;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.Component;
import rpd.components.Plating;
import rpd.conceptions.Position;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则5
public class PlatingRule {

public static List<PlatingRule> plating_rules = null;
	
	private static Mouth mouth = null;
	
	public RPDPlan apply(RPDPlan rpd_plan) throws RuleException, ToothPosException {
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
		plating_rules = new ArrayList<PlatingRule>();
		
		plating_rules.add(new PlatingRule() {
			
			public int getRuleNum() {
				return 1;
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public String getExplaination() {
				return "若前牙区有缺失，切牙、侧切牙用舌板覆盖";
			}
			
			public RPDPlan apply(RPDPlan rpd_plan) throws RuleException, ToothPosException {
				
				boolean need_plating = true;
				Position position = rpd_plan.getPosition();
				if(position.equals(Position.Mandibular))
					need_plating = (mouth.getMandibular().getTooth(3, 1).isMissing() ||
									mouth.getMandibular().getTooth(3, 2).isMissing() ||
									mouth.getMandibular().getTooth(3, 3).isMissing() ||
									mouth.getMandibular().getTooth(4, 1).isMissing() ||
									mouth.getMandibular().getTooth(4, 2).isMissing() ||
									mouth.getMandibular().getTooth(4, 3).isMissing());
				else
					need_plating = (mouth.getMandibular().getTooth(1, 1).isMissing() ||
									mouth.getMandibular().getTooth(1, 2).isMissing() ||
									mouth.getMandibular().getTooth(1, 3).isMissing() ||
									mouth.getMandibular().getTooth(2, 1).isMissing() ||
									mouth.getMandibular().getTooth(2, 2).isMissing() ||
									mouth.getMandibular().getTooth(2, 3).isMissing());
				if(!need_plating)
					return null;
				else {
					
					if(position.equals(Position.Mandibular)) {
						
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						List<Tooth> plating_teeth = new ArrayList<Tooth>();
						for(Component component : rpd_plan.getComponents()) {
							Tooth tooth = component.getToothPos();
							if(component.isPlating())
								plating_teeth.add(tooth);
						}
						
						Tooth tooth_31 = mouth.getTooth(3, 1);
						if(!plating_teeth.contains(tooth_31) && !tooth_31.isMissing()) {
							Plating plating = new Plating(tooth_31);
							new_plan.addComponent(plating);
						}
						Tooth tooth_32 = mouth.getTooth(3, 2);
						if(!plating_teeth.contains(tooth_32) && !tooth_32.isMissing()) {
							Plating plating = new Plating(tooth_32);
							new_plan.addComponent(plating);
						}
						Tooth tooth_41 = mouth.getTooth(4, 1);
						if(!plating_teeth.contains(tooth_41) && !tooth_41.isMissing()) {
							Plating plating = new Plating(tooth_41);
							new_plan.addComponent(plating);
						}
						Tooth tooth_42 = mouth.getTooth(4, 2);
						if(!plating_teeth.contains(tooth_42) && !tooth_42.isMissing()) {
							Plating plating = new Plating(tooth_42);
							new_plan.addComponent(plating);
						}
						return new_plan;
					}
					else
						return null;
				}
			}
		});
	}
			
}
