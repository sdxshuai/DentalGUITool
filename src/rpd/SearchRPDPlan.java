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
import java.util.Iterator;
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
		List<EdentulousSpace> edentulousSpaceList = mandibular.getEdentulousSpaces();

		//标记是否对合7位置缺失视为不缺失
		boolean tooth37Changed = false;
		boolean tooth47Changed = false;

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
		ScoreRule.initRules(mouth);
		MajorConnectorRule.initRules(mouth);

		List<EdentulousSpace> curEdentulousSpaces = new ArrayList<>();
		curEdentulousSpaces.addAll(edentulousSpaceList);
		Iterator<EdentulousSpace> iterator = curEdentulousSpaces.iterator();
		while (iterator.hasNext()) {
			EdentulousSpace edentulousSpace = iterator.next();
			if (edentulousSpace.getLeftMost().getNum() == 7 && edentulousSpace.getRightMost().getNum() == 7) {
				int edentulousZone = edentulousSpace.getLeftMost().getZone();
				if (mouth.getMaxillary().getTooth(5-edentulousZone, 7).isMissing()
						&& !mouth.getMaxillary().getTooth(5-edentulousZone, 6).isMissing()
						&& mouth.getMandibular().getTooth(edentulousZone, 8).isMissing()) {
					iterator.remove();
					if (edentulousZone == 3) {
						tooth37Changed = true;
					}
					else if (edentulousZone == 4) {
						tooth47Changed = true;
					}
				}
			}
		}
		mandibular.setEdentulousSpaces(curEdentulousSpaces);

		List<RPDPlan> res = new ArrayList<RPDPlan>();
		RPDPlan empty_plan = new RPDPlan(mouth, Position.Mandibular);

		List<RPDPlan> abutment_teeth_plans = new ArrayList<RPDPlan>();
		abutment_teeth_plans.add(empty_plan);
		for (ChooseAbutmentRule rule : ChooseAbutmentRule.choose_abutment_rules) {
			List<RPDPlan> plans = rule.apply(abutment_teeth_plans);
			abutment_teeth_plans.clear();
			abutment_teeth_plans.addAll(plans);
		}
		if (abutment_teeth_plans.size() == 0) {
			return null;
		}

		//挑选完基牙将对合7位置设为不缺失
		if (tooth37Changed) {
			mandibular.getZone3().get(7).setMissing(false);
		}
		if (tooth47Changed) {
			mandibular.getZone4().get(7).setMissing(false);
		}


		List<RPDPlan> clasp_plans = new ArrayList<RPDPlan>();
		clasp_plans.addAll(abutment_teeth_plans);
		for (ClaspRule rule : ClaspRule.clasp_rules) {
			List<RPDPlan> plans = rule.apply(clasp_plans);
			clasp_plans.clear();
			clasp_plans.addAll(plans);
		}
		if (clasp_plans.size() == 0) {
			return null;
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

		List<RPDPlan> score_plans = new ArrayList<RPDPlan>();
		score_plans.addAll(indirect_retainer_plans);
		for (ScoreRule rule : ScoreRule.score_rules) {
			List<RPDPlan> plans = rule.apply(score_plans);
			score_plans.clear();
			score_plans.addAll(plans);
		}
		if (score_plans.size() == 0) {
			return null;
		}

		List<RPDPlan> major_connector_plans = new ArrayList<RPDPlan>();
		major_connector_plans.addAll(score_plans);
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

		//复原对合牙位7缺失属性
		mandibular.setEdentulousSpaces(edentulousSpaceList);
		if (tooth37Changed) {
			mandibular.getZone3().get(7).setMissing(true);
		}
		if (tooth47Changed) {
			mandibular.getZone4().get(7).setMissing(true);
		}

		if (res.size()==0) {
			res = null;
		}
		return res;
	}

	public static List<RPDPlan> searchMaxillary(Mouth mouth) throws RuleException, ClaspAssemblyException, ToothPosException, EdentulousTypeException {

		Maxillary maxillary = mouth.getMaxillary();
		List<EdentulousSpace> edentulousSpaceList = maxillary.getEdentulousSpaces();
		boolean tooth17Changed = false;
		boolean tooth27Changed = false;

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
		ScoreRule.initRules(mouth);
		MajorConnectorRule.initRules(mouth);

		List<EdentulousSpace> curEdentulousSpaces = new ArrayList<>();
		curEdentulousSpaces.addAll(edentulousSpaceList);
		Iterator<EdentulousSpace> iterator = curEdentulousSpaces.iterator();
		while (iterator.hasNext()) {
			EdentulousSpace edentulousSpace = iterator.next();
			if (edentulousSpace.getLeftMost().getNum() == 7 && edentulousSpace.getRightMost().getNum() == 7) {
				int edentulousZone = edentulousSpace.getLeftMost().getZone();
				if (mouth.getMandibular().getTooth(5-edentulousZone, 7).isMissing()
						&& !mouth.getMandibular().getTooth(5-edentulousZone, 6).isMissing()
						&& mouth.getMaxillary().getTooth(edentulousZone, 8).isMissing()) {
					iterator.remove();
					if (edentulousZone == 1) {
						tooth17Changed = true;
					}
					else if (edentulousZone == 2) {
						tooth27Changed = true;
					}
				}
			}
		}
		maxillary.setEdentulousSpaces(curEdentulousSpaces);

		List<RPDPlan> res = new ArrayList<RPDPlan>();
		RPDPlan empty_plan = new RPDPlan(mouth, Position.Maxillary);

		List<RPDPlan> abutment_teeth_plans = new ArrayList<RPDPlan>();
		abutment_teeth_plans.add(empty_plan);
		for (ChooseAbutmentRule rule : ChooseAbutmentRule.choose_abutment_rules) {
			List<RPDPlan> plans = rule.apply(abutment_teeth_plans);
			abutment_teeth_plans.clear();
			abutment_teeth_plans.addAll(plans);
		}
		if (abutment_teeth_plans.size() == 0) {
			return null;
		}

		if (tooth17Changed) {
			maxillary.getZone1().get(7).setMissing(false);
		}
		if (tooth27Changed) {
			maxillary.getZone2().get(7).setMissing(false);
		}


		List<RPDPlan> clasp_plans = new ArrayList<RPDPlan>();
		clasp_plans.addAll(abutment_teeth_plans);
		for (ClaspRule rule : ClaspRule.clasp_rules) {
			List<RPDPlan> plans = rule.apply(clasp_plans);
			clasp_plans.clear();
			clasp_plans.addAll(plans);
		}
		if (clasp_plans.size() == 0) {
			return null;
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

		List<RPDPlan> score_plans = new ArrayList<RPDPlan>();
		score_plans.addAll(indirect_retainer_plans);
		for (ScoreRule rule : ScoreRule.score_rules) {
			List<RPDPlan> plans = rule.apply(score_plans);
			score_plans.clear();
			score_plans.addAll(plans);
		}
		if (score_plans.size() == 0) {
			return null;
		}

		List<RPDPlan> major_connector_plans = new ArrayList<RPDPlan>();
		major_connector_plans.addAll(score_plans);
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

		maxillary.setEdentulousSpaces(edentulousSpaceList);
		if (tooth17Changed) {
			maxillary.getZone1().get(7).setMissing(true);
		}
		if (tooth27Changed) {
			maxillary.getZone2().get(7).setMissing(true);
		}

		if (res.size()==0) {
			res = null;
		}
		return res;
	}
}
