package rpd;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.components.AkerClasp;
import rpd.components.DentureBase;
import rpd.components.LingualPlateConnector;
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

	public static boolean isSpecialCase(Mandibular mandibular) throws ToothPosException {
		boolean flag = false;
		List<Tooth> missingTeeth = mandibular.getMissingTeeth();
		if (missingTeeth.size() == 1) {
			Tooth missingTooth = missingTeeth.get(0);
			if (missingTooth.getNum() == 7 && mandibular.getTooth(missingTooth.getZone(), 8).isMissing()) {
				flag = true;
			}
		}
		return flag;
	}

	public static boolean isSpecialCase(Maxillary maxillary) throws ToothPosException {
		boolean flag = false;
		List<Tooth> missingTeeth = maxillary.getMissingTeeth();
		if (missingTeeth.size() == 1) {
			Tooth missingTooth = missingTeeth.get(0);
			if (missingTooth.getNum() == 7 && maxillary.getTooth(missingTooth.getZone(), 8).isMissing()) {
				flag = true;
			}
		}
		return flag;
	}

	public static RPDPlan getSpecialCasePlan(Mouth mouth, Position mandibularOrMaxillary) {
		RPDPlan plan = new RPDPlan(mouth, mandibularOrMaxillary);
		List<Tooth> missingTeeth;
		Tooth missingTooth;
		Tooth abutmentToothAt6;
		Tooth abutmentToothAt5;
		int missingZone;
		if (mandibularOrMaxillary == Position.Mandibular) {
			missingTeeth = mouth.getMandibular().getMissingTeeth();
			missingTooth = missingTeeth.get(0);
			missingZone = missingTooth.getZone();
			abutmentToothAt6 = mouth.getMandibular().getTooth(missingZone, 6);
			abutmentToothAt5 = mouth.getMandibular().getTooth(missingZone, 5);
		}
		else {
			missingTeeth = mouth.getMaxillary().getMissingTeeth();
			missingTooth = missingTeeth.get(0);
			missingZone = missingTooth.getZone();
			abutmentToothAt6 = mouth.getMaxillary().getTooth(missingZone, 6);
			abutmentToothAt5 = mouth.getMaxillary().getTooth(missingZone, 5);
		}

		plan.addAbutmentTeeth(abutmentToothAt6);
		plan.addAbutmentTeeth(abutmentToothAt5);
		plan.addComponent(new AkerClasp(abutmentToothAt6, Position.Mesial));
		plan.addComponent(new AkerClasp(abutmentToothAt5, Position.Distal));
		plan.addComponent(new DentureBase(missingTooth));
		return plan;
	}

	public static List<RPDPlan> searchMandibular(Mouth mouth) throws RuleException, ClaspAssemblyException, ToothPosException, EdentulousTypeException {

		Mandibular mandibular = mouth.getMandibular();
		List<EdentulousSpace> edentulousSpaceList = mandibular.getEdentulousSpaces();

		//标记是否对合7位置缺失视为不缺失
		boolean tooth37Changed = false;
		boolean tooth47Changed = false;

		if (noMissing(mandibular)) {
			return null;
		}
		if (isSpecialCase(mandibular)) {
			List<RPDPlan> res = new ArrayList<RPDPlan>();
			RPDPlan plan = getSpecialCasePlan(mouth, Position.Mandibular);
			res.add(plan);
			return res;
		}

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
			if (edentulousSpace.getLeftMost().getNum() == 7 || edentulousSpace.getRightMost().getNum() == 7) {
				int edentulousZone = edentulousSpace.getLeftMost().getZone();
				if (mouth.getMaxillary().getTooth(5-edentulousZone, 7).isMissing()
						&& mouth.getMaxillary().getTooth(5-edentulousZone, 8).isMissing()
//						&& (!mouth.getMaxillary().getTooth(5-edentulousZone, 6).isMissing()
//						|| !mouth.getMandibular().getTooth(edentulousZone, 6).isMissing())
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
			mandibular.setRightMostToothNum(6);
		}
		if (tooth47Changed) {
			mandibular.getZone4().get(7).setMissing(false);
			mandibular.setleftMostToothNum(6);
		}
		edentulousSpaceList = mandibular.getEdentulousSpaces(mandibular.getZone3(), mandibular.getZone4());
		mandibular.setEdentulousSpaces(edentulousSpaceList);

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
		edentulousSpaceList = mandibular.getEdentulousSpaces(mandibular.getZone3(), mandibular.getZone4());
		mandibular.setEdentulousSpaces(edentulousSpaceList);

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

		if (noMissing(maxillary)) {
			return null;
		}
		if (isSpecialCase(maxillary)) {
			List<RPDPlan> res = new ArrayList<RPDPlan>();
			RPDPlan plan = getSpecialCasePlan(mouth, Position.Maxillary);
			res.add(plan);
			return res;
		}

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
			if (edentulousSpace.getLeftMost().getNum() == 7 || edentulousSpace.getRightMost().getNum() == 7) {
				int edentulousZone = edentulousSpace.getLeftMost().getZone();
				if (mouth.getMandibular().getTooth(5-edentulousZone, 7).isMissing()
						&& mouth.getMandibular().getTooth(5-edentulousZone, 8).isMissing()
//						&& (!mouth.getMandibular().getTooth(5-edentulousZone, 6).isMissing()
//						|| !mouth.getMaxillary().getTooth(edentulousZone, 6).isMissing())
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
			maxillary.setleftMostToothNum(6);
		}
		if (tooth27Changed) {
			maxillary.getZone2().get(7).setMissing(false);
			maxillary.setRightMostToothNum(6);
		}
		edentulousSpaceList = maxillary.getEdentulousSpaces(maxillary.getZone1(), maxillary.getZone2());
		maxillary.setEdentulousSpaces(edentulousSpaceList);

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
		edentulousSpaceList = maxillary.getEdentulousSpaces(maxillary.getZone1(), maxillary.getZone2());
		maxillary.setEdentulousSpaces(edentulousSpaceList);

		if (res.size()==0) {
			res = null;
		}
		return res;
	}
}
