package rpd.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.CircumferencialClaspAssembly;
import rpd.components.ClaspAssembly;
import rpd.components.CombinedClaspAssembly;
import rpd.components.Component;
import rpd.conceptions.Position;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则3
public class AddtionalAssemblyRule {

	public static List<AddtionalAssemblyRule> addtional_assembly_rules = null;

	private static Mouth mouth = null;

	public RPDPlan apply(RPDPlan rpd_plan) throws RuleException, ToothPosException, ClaspAssemblyException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	/*public static boolean[] needAddtionalAssembly(RPDPlan rpd_plan) {
		
		boolean[] res = new boolean[2];
		res[0] = false;
		res[1] = false;
		
		boolean left_posterior_has_clasp = false;
		boolean right_posterior_has_clasp = false;
		
		List<Component> components = rpd_plan.getComponents();
		for(Component component : components) {
			
			if(!component.isClasp())
				continue;
			Tooth tooth = component.getToothPos();
			int zone = tooth.getZone();
			int num = tooth.getNum();
			if((zone == 4 || zone == 1) && num >= 4)
				left_posterior_has_clasp = true;
			else if((zone == 2 || zone == 3) && num >= 4)
				right_posterior_has_clasp = true;
			else {}
		}
		
		if(!left_posterior_has_clasp)
			res[0] = true;
		if(!right_posterior_has_clasp)
			res[1] = true;
		
		return res;
	}*/

	public static boolean[] needAddtionalAssembly(RPDPlan rpd_plan) throws ToothPosException {

		boolean[] res = new boolean[2];
		res[0] = false;
		res[1] = false;

		boolean left_posterior_has_clasp = false;
		boolean right_posterior_has_clasp = false;

		boolean left_posterior_no_missing = true;
		boolean right_posterior_no_missing = true;

		Set<Component> components = rpd_plan.getComponents();
		for (Component component : components) {

			if (!component.isClasp())
				continue;
			ArrayList<Tooth> tooth = component.getToothPos();
			int zone = tooth.get(0).getZone();
			int num = tooth.get(0).getNum();
			if ((zone == 4 || zone == 1) && num >= 4)
				left_posterior_has_clasp = true;
			else if ((zone == 2 || zone == 3) && num >= 4)
				right_posterior_has_clasp = true;
			else {
			}
		}

		for (int i = 4; i <= 7; i++) {
			if (mouth.getTooth(3, i).isMissing())
				right_posterior_no_missing = false;
			if (mouth.getTooth(4, i).isMissing())
				left_posterior_no_missing = false;
		}

		if ((!left_posterior_has_clasp) && left_posterior_no_missing)
			res[0] = true;
		if (!right_posterior_has_clasp && right_posterior_no_missing)
			res[1] = true;

		return res;
	}

	public static void initRules(Mouth mouth_obj) {

		mouth = mouth_obj;
		addtional_assembly_rules = new ArrayList<AddtionalAssemblyRule>();

		addtional_assembly_rules.add(new AddtionalAssemblyRule() {

			public String getExplaination() {
				return "后侧无缺牙，放置圆形或联合卡环，以符合对成分布";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 1;
			}

			public RPDPlan apply(RPDPlan rpd_plan) throws RuleException, ToothPosException, ClaspAssemblyException {
				boolean[] need_addtional_assembly = needAddtionalAssembly(rpd_plan);
				if (need_addtional_assembly[0]) {
					RPDPlan new_plan = new RPDPlan(rpd_plan);
					ClaspAssembly clasp_assbemly_added = null;
					if (mouth_obj.getTooth(3, 8).isMissing() && mouth_obj.getTooth(3, 7).isMissing())
						clasp_assbemly_added = new CircumferencialClaspAssembly(mouth_obj.getTooth(4, 6), Position.Distal);
					else
						clasp_assbemly_added = new CombinedClaspAssembly(mouth_obj.getTooth(4, 5), mouth_obj.getTooth(4, 6));
					clasp_assbemly_added.addToPlan(new_plan);
					return new_plan;
				} else if (need_addtional_assembly[1]) {
					RPDPlan new_plan = new RPDPlan(rpd_plan);
					ClaspAssembly clasp_assbemly_added = null;
					if (mouth_obj.getTooth(4, 8).isMissing() && mouth_obj.getTooth(4, 7).isMissing())
						clasp_assbemly_added = new CircumferencialClaspAssembly(mouth_obj.getTooth(3, 6), Position.Distal);
					else
						clasp_assbemly_added = new CombinedClaspAssembly(mouth_obj.getTooth(3, 5), mouth_obj.getTooth(3, 6));
					clasp_assbemly_added.addToPlan(new_plan);
					return new_plan;
				} else
					return null;
			}
		});
	}
}
