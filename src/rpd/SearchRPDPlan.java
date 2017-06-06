package rpd;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.components.DentureBase;
import rpd.conceptions.Position;
import rpd.oral.*;
import rpd.rules.*;

import java.util.ArrayList;
import java.util.List;

//生成设计方案
public class SearchRPDPlan {

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

//        EdentulousTypeRule.initRules();
//        AssemblyRule.initRules(mouth);
//        AddtionalAssemblyRule.initRules(mouth);
//        IndirectRetainerRule.initRules(mouth);
//        PlatingRule.initRules(mouth);
//        RemovingRule.initRules(mouth);

		EdentulousTypeRule.initRules();
		ChooseAbutmentRule.initRules(mouth);
		ClaspRule.initRules(mouth);
		IndirectRetainerRule.initRules(mouth);
		MajorConnectorRule.initRules(mouth);

		List<RPDPlan> res = new ArrayList<RPDPlan>();
		RPDPlan empty_plan = new RPDPlan(mouth, Position.Mandibular);

		List<RPDPlan> abutment_teeth_plans = new ArrayList<RPDPlan>();
		abutment_teeth_plans.add(empty_plan);
		for (ChooseAbutmentRule rule : ChooseAbutmentRule.choose_abutment_rules) {
			List<RPDPlan> plans = rule.apply(abutment_teeth_plans);
			abutment_teeth_plans.clear();
			abutment_teeth_plans.addAll(plans);
		}

		List<RPDPlan> clasp_plans = new ArrayList<RPDPlan>();
		clasp_plans.addAll(abutment_teeth_plans);
		for (ClaspRule rule : ClaspRule.clasp_rules) {
			List<RPDPlan> plans = rule.apply(clasp_plans);
			clasp_plans.clear();
			clasp_plans.addAll(plans);
		}

		List<RPDPlan> indirect_retainer_plans = new ArrayList<RPDPlan>();
		List<RPDPlan> indirect_retainer_plans_buffer = new ArrayList<RPDPlan>();
		indirect_retainer_plans.addAll(clasp_plans);
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

		List<RPDPlan> major_connector_plans = new ArrayList<RPDPlan>();
		major_connector_plans.addAll(indirect_retainer_plans);
		for (MajorConnectorRule rule : MajorConnectorRule.major_connector_rules) {
			List<RPDPlan> plans = rule.apply(major_connector_plans);
			major_connector_plans.clear();
			major_connector_plans.addAll(plans);
		}

		res.addAll(major_connector_plans);
		if (res.size() == 1) {
			RPDPlan plan = res.get(0);
			if (plan.isEmptyPlan())
				res.remove(0);
		}

		for (EdentulousSpace edentulous_space : mandibular.getEdentulousSpaces()) {
			ArrayList<Tooth> denture_base_tooth_pos = new ArrayList<>();
			denture_base_tooth_pos.add(edentulous_space.getLeftMost());
			if (!edentulous_space.getRightMost().equals(edentulous_space.getLeftMost())) {
				denture_base_tooth_pos.add(edentulous_space.getRightMost());
			}
//			denture_base_tooth_pos.add(edentulous_space.getRightMost());
			DentureBase dentureBase = new DentureBase(denture_base_tooth_pos);

			for (RPDPlan plan : res) {
				plan.addComponent(dentureBase);
			}
		}

		if (res.size()==0) {
			res = null;
		}
		return res;
	}

	public static List<RPDPlan> searchMaxillary(Mouth mouth) throws RuleException, ClaspAssemblyException, ToothPosException, EdentulousTypeException {

		Maxillary maxillary = mouth.getMaxillary();

		if (noMissing(maxillary))
			return null;

//        EdentulousTypeRule.initRules();
//        AssemblyRule.initRules(mouth);
//        AddtionalAssemblyRule.initRules(mouth);
//        IndirectRetainerRule.initRules(mouth);
//        PlatingRule.initRules(mouth);
//        RemovingRule.initRules(mouth);

		EdentulousTypeRule.initRules();
		ChooseAbutmentRule.initRules(mouth);
		ClaspRule.initRules(mouth);
		IndirectRetainerRule.initRules(mouth);
		MajorConnectorRule.initRules(mouth);

		List<RPDPlan> res = new ArrayList<RPDPlan>();
		RPDPlan empty_plan = new RPDPlan(mouth, Position.Maxillary);

		List<RPDPlan> abutment_teeth_plans = new ArrayList<RPDPlan>();
		abutment_teeth_plans.add(empty_plan);
		for (ChooseAbutmentRule rule : ChooseAbutmentRule.choose_abutment_rules) {
			List<RPDPlan> plans = rule.apply(abutment_teeth_plans);
			abutment_teeth_plans.clear();
			abutment_teeth_plans.addAll(plans);
		}

		List<RPDPlan> clasp_plans = new ArrayList<RPDPlan>();
		clasp_plans.addAll(abutment_teeth_plans);
		for (ClaspRule rule : ClaspRule.clasp_rules) {
			List<RPDPlan> plans = rule.apply(clasp_plans);
			clasp_plans.clear();
			clasp_plans.addAll(plans);
		}

		List<RPDPlan> indirect_retainer_plans = new ArrayList<RPDPlan>();
		List<RPDPlan> indirect_retainer_plans_buffer = new ArrayList<RPDPlan>();
		indirect_retainer_plans.addAll(clasp_plans);
		for (EdentulousSpace edentulous_space : maxillary.getEdentulousSpaces()) {
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

		List<RPDPlan> major_connector_plans = new ArrayList<RPDPlan>();
		major_connector_plans.addAll(indirect_retainer_plans);
		for (MajorConnectorRule rule : MajorConnectorRule.major_connector_rules) {
			List<RPDPlan> plans = rule.apply(major_connector_plans);
			major_connector_plans.clear();
			major_connector_plans.addAll(plans);
		}

		res.addAll(major_connector_plans);
		if (res.size() == 1) {
			RPDPlan plan = res.get(0);
			if (plan.isEmptyPlan())
				res.remove(0);
		}

		for (EdentulousSpace edentulous_space : maxillary.getEdentulousSpaces()) {
			ArrayList<Tooth> denture_base_tooth_pos = new ArrayList<>();
			denture_base_tooth_pos.add(edentulous_space.getLeftMost());
			if (!edentulous_space.getLeftMost().equals(edentulous_space.getRightMost())) {
				denture_base_tooth_pos.add(edentulous_space.getRightMost());
			}
			DentureBase dentureBase = new DentureBase(denture_base_tooth_pos);

			for (RPDPlan plan : res) {
				plan.addComponent(dentureBase);
			}
		}

		if (res.size()==0) {
			res = null;
		}
		return res;
	}
}
