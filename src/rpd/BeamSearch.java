package rpd;

import java.util.ArrayList;
import java.util.List;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mandibular;
import rpd.oral.Maxillary;
import rpd.oral.Mouth;
import rpd.rules.AddtionalAssemblyRule;
import rpd.rules.AssemblyRule;
import rpd.rules.EdentulousTypeRule;
import rpd.rules.IndirectRetainerRule;
import rpd.rules.PlatingRule;
import rpd.rules.RemovingRule;

//生成设计方案
public class BeamSearch {

	public static boolean noMissing(Mandibular mandibular) throws ToothPosException {

		boolean no_missing = true;
		for (int num = 1; num <= 7; num++) {
			no_missing = no_missing && (!mandibular.getTooth(3, num).isMissing());
			no_missing = no_missing && (!mandibular.getTooth(4, num).isMissing());
		}
		return no_missing;
	}

	public static boolean noMissing(Maxillary maxillary) throws ToothPosException {

		boolean no_missing = true;
		for (int num = 1; num <= 7; num++) {
			no_missing = no_missing && (!maxillary.getTooth(1, num).isMissing());
			no_missing = no_missing && (!maxillary.getTooth(2, num).isMissing());
		}
		return no_missing;
	}

	public static List<RPDPlan> searchMandibular(Mouth mouth) throws RuleException, ClaspAssemblyException, ToothPosException, EdentulousTypeException {

		Mandibular mandibular = mouth.getMandibular();

		if (noMissing(mandibular))
			return null;

		EdentulousTypeRule.initRules();
		AssemblyRule.initRules(mouth);
		AddtionalAssemblyRule.initRules(mouth);
		IndirectRetainerRule.initRules(mouth);
		PlatingRule.initRules(mouth);
		RemovingRule.initRules(mouth);

		List<RPDPlan> res = new ArrayList<RPDPlan>();
		RPDPlan empty_plan = new RPDPlan(mouth, Position.Mandibular);

		List<RPDPlan> assembly_plans = new ArrayList<RPDPlan>();
		List<RPDPlan> assembly_plans_buffer = new ArrayList<RPDPlan>();
		assembly_plans.add(empty_plan);
		boolean all_space_restored = true;
		for (EdentulousSpace edentulous_space : mandibular.getEdentulousSpaces()) {

			for (RPDPlan old_plan : assembly_plans) {
				boolean plan_changed = false;
				for (AssemblyRule rule : AssemblyRule.assembly_rules) {
					RPDPlan plan = rule.apply(edentulous_space, old_plan);
					if (plan != null) {
						assembly_plans_buffer.add(plan);
						plan_changed = true;
						//System.out.println(plan.toString());
					}
				}
				if (!plan_changed) {
					assembly_plans_buffer.add(old_plan);
					all_space_restored = false;
				}
			}
			assembly_plans.clear();
			assembly_plans.addAll(assembly_plans_buffer);
			assembly_plans_buffer.clear();
		}
		if (!all_space_restored) {
			assembly_plans.clear();
			return assembly_plans;
		}

		List<RPDPlan> addtional_assembly_plans = new ArrayList<RPDPlan>();
		for (RPDPlan old_plan : assembly_plans) {

			boolean plan_changed = false;
			for (AddtionalAssemblyRule rule : AddtionalAssemblyRule.addtional_assembly_rules) {
				RPDPlan plan = rule.apply(old_plan);
				if (plan != null) {
					addtional_assembly_plans.add(plan);
					plan_changed = true;
				}
			}
			if (!plan_changed)
				addtional_assembly_plans.add(old_plan);
		}

		List<RPDPlan> indirect_retainer_plans = new ArrayList<RPDPlan>();
		List<RPDPlan> indirect_retainer_plans_buffer = new ArrayList<RPDPlan>();
		indirect_retainer_plans.addAll(addtional_assembly_plans);
		for (EdentulousSpace edentulous_space : mandibular.getEdentulousSpaces()) {
			for (RPDPlan old_plan : indirect_retainer_plans) {
				boolean plan_changed = false;
				for (IndirectRetainerRule rule : IndirectRetainerRule.indirect_retainer_rules) {
					RPDPlan plan = rule.apply(edentulous_space, old_plan);
					if (plan != null) {
						indirect_retainer_plans_buffer.add(plan);
						plan_changed = true;
					}
				}
				if (!plan_changed)
					indirect_retainer_plans_buffer.add(old_plan);
			}
			indirect_retainer_plans.clear();
			indirect_retainer_plans.addAll(indirect_retainer_plans_buffer);
			indirect_retainer_plans_buffer.clear();
		}

		List<RPDPlan> plating_plans = new ArrayList<RPDPlan>();
		for (RPDPlan old_plan : indirect_retainer_plans) {

			boolean plan_changed = false;
			for (PlatingRule rule : PlatingRule.plating_rules) {
				RPDPlan plan = rule.apply(old_plan);
				if (plan != null) {
					plating_plans.add(plan);
					plan_changed = true;
				}
			}
			if (!plan_changed)
				plating_plans.add(old_plan);
		}

		List<RPDPlan> removing_plans = new ArrayList<RPDPlan>();
		for (RPDPlan old_plan : plating_plans) {

			boolean plan_changed = false;
			for (RemovingRule rule : RemovingRule.removing_rules) {
				RPDPlan plan = rule.apply(old_plan);
				if (plan != null) {
					removing_plans.add(plan);
					plan_changed = true;
				}
			}
			if (!plan_changed)
				removing_plans.add(old_plan);
		}

		res.addAll(removing_plans);

		if (res.size() == 1) {
			RPDPlan plan = res.get(0);
			if (plan.isEmptyPlan())
				res.remove(0);
		}

		return res;
	}

}
