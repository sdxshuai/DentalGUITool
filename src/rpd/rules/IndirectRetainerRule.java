package rpd.rules;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.Component;
import rpd.components.LingualRest;
import rpd.components.RotationAxis;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.util.ArrayList;
import java.util.List;

//规则4
public class IndirectRetainerRule {

	public static List<IndirectRetainerRule> indirect_retainer_rules = null;

	private static Mouth mouth = null;

	private static RotationAxis findRotationAxis(EdentulousSpace edentulous_space, RPDPlan rpd_plan) {

		EdentulousType edentulous_type = edentulous_space.getEdentulousType();
		if (edentulous_type.equals(EdentulousType.PosteriorExtension))
			return findPosteriorExtensionRotationAxis(edentulous_space, rpd_plan);
		else if (edentulous_type.equals(EdentulousType.AnteriorExtension))
			return findAnteriorExtensionRotationAxis(edentulous_space, rpd_plan);
		else
			return null;
	}

	private static RotationAxis findAnteriorExtensionRotationAxis(EdentulousSpace edentulous_space, RPDPlan rpd_plan) {

		Component left_indirect_retainer = null;
		Component right_indirect_retainer = null;

		int left_min_num = 9;
		int right_min_num = 9;

		for (Component component : rpd_plan.getComponents()) {

			if (!component.isIndirectRetainer())
				continue;
			int zone = component.getToothPos().get(0).getZone();
			int num = component.getToothPos().get(0).getNum();
			if (zone == 4 || zone == 1) {
				if (num < left_min_num) {
					left_min_num = num;
					left_indirect_retainer = component;
				}
			} else {
				if (num < right_min_num) {
					right_min_num = num;
					right_indirect_retainer = component;
				}
			}
		}

		if (left_indirect_retainer == null || right_indirect_retainer == null)
			return null;
		else
			return new RotationAxis(left_indirect_retainer, right_indirect_retainer, edentulous_space);
	}

	private static RotationAxis findPosteriorExtensionRotationAxis(EdentulousSpace edentulous_space, RPDPlan rpd_plan) {

		Component left_indirect_retainer = null;
		Component right_indirect_retainer = null;

		int left_max_num = -1;
		int right_max_num = -1;

		for (Component component : rpd_plan.getComponents()) {

			if (!component.isIndirectRetainer())
				continue;
			int zone = component.getToothPos().get(0).getZone();
			int num = component.getToothPos().get(0).getNum();
			if (zone == 4 || zone == 1) {
				if (num > left_max_num) {
					left_max_num = num;
					left_indirect_retainer = component;
				}
			} else {
				if (num > right_max_num) {
					right_max_num = num;
					right_indirect_retainer = component;
				}
			}
		}

		if (left_indirect_retainer == null || right_indirect_retainer == null)
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
				return "前牙缺失，缺隙邻牙为前牙，则缺隙邻牙添加舌支托";
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans, List<EdentulousSpace> edentulous_spaces)
					throws EdentulousTypeException, ToothPosException {
				if (rpd_plans == null || rpd_plans.size() == 0) {
					return null;
				}

				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					if (plan.getPosition() == Position.Mandibular) {
						for (int t_zone = 3; t_zone <= 4; t_zone++) {
							for (int t_num = 1; t_num <= 3; t_num++) {
								if (t_num == 1) {
									if (!mouth.getMandibular().getTooth(t_zone, t_num).isMissing()
											&& (mouth.getMandibular().getTooth(t_zone, 2).isMissing()
											|| mouth.getMandibular().getTooth(7 - t_zone, 1).isMissing())) {
										if (!plan.getAbutmentTeeth().contains(mouth.getTooth(t_zone, 1))) {
											plan.addComponent(new LingualRest(mouth.getTooth(t_zone, 1)));
											plan.addAbutmentTeeth(mouth.getTooth(t_zone, 1));
										}
									}
								} else {
									if (!mouth.getMandibular().getTooth(t_zone, t_num).isMissing()
											&& (mouth.getMandibular().getTooth(t_zone, t_num + 1).isMissing()
											|| mouth.getMandibular().getTooth(t_zone, t_num - 1).isMissing())) {
										if (!plan.getAbutmentTeeth().contains(mouth.getTooth(t_zone, t_num))) {
											plan.addComponent(new LingualRest(mouth.getTooth(t_zone, t_num)));
											plan.addAbutmentTeeth(mouth.getTooth(t_zone, t_num));
										}
									}
								}
							}
						}
					} else {
						for (int t_zone = 1; t_zone <= 2; t_zone++) {
							for (int t_num = 1; t_num <= 3; t_num++) {
								if (t_num == 1) {
									if (!mouth.getMaxillary().getTooth(t_zone, t_num).isMissing()
											&& (mouth.getMaxillary().getTooth(t_zone, 2).isMissing()
											|| mouth.getMaxillary().getTooth(3 - t_zone, 1).isMissing())) {
										if (!plan.getAbutmentTeeth().contains(mouth.getTooth(t_zone, 1))) {
											plan.addComponent(new LingualRest(mouth.getTooth(t_zone, 1)));
											plan.addAbutmentTeeth(mouth.getTooth(t_zone, 1));
										}
									}
								} else if (t_num == 2){
									if (!mouth.getMaxillary().getTooth(t_zone, t_num).isMissing()
											&& (mouth.getMaxillary().getTooth(t_zone, t_num + 1).isMissing()
											|| mouth.getMaxillary().getTooth(t_zone, t_num - 1).isMissing())) {
										if (!plan.getAbutmentTeeth().contains(mouth.getTooth(t_zone, t_num))) {
											plan.addComponent(new LingualRest(mouth.getTooth(t_zone, t_num)));
											plan.addAbutmentTeeth(mouth.getTooth(t_zone, t_num));
										}
									}
								} else {
									if (!mouth.getMaxillary().getTooth(t_zone, t_num).isMissing()
											&& mouth.getMaxillary().getTooth(t_zone, t_num - 1).isMissing()) {
										if (!plan.getAbutmentTeeth().contains(mouth.getTooth(t_zone, t_num))) {
											plan.addComponent(new LingualRest(mouth.getTooth(t_zone, t_num)));
											plan.addAbutmentTeeth(mouth.getTooth(t_zone, t_num));
										}
									}
								}
							}
						}
					}
				}
				res.addAll(rpd_plans);
				return res;
			}
		});
		indirect_retainer_rules.add(new IndirectRetainerRule() {

			public int getRuleNum() throws RuleException {
				return 2;
			}

			public String toString() {
				return this.getExplaination();
			}

			public String getExplaination() {
				return "游离缺失，若转动轴前侧无间接固位体，则增设间接固位体";
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans, List<EdentulousSpace> edentulous_spaces)
					throws EdentulousTypeException, ToothPosException {

				if (rpd_plans == null || rpd_plans.size() == 0) {
					return null;
				}

				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					for (EdentulousSpace edentulous_space : edentulous_spaces) {
						EdentulousType edentulous_type = edentulous_space.getEdentulousType();
						if (edentulous_type.equals(EdentulousType.PosteriorExtension)) {
							RotationAxis rotation_axis = findRotationAxis(edentulous_space, plan);
							if (rotation_axis == null)
								continue;

							if (rotation_axis.needIndirectRetainer(plan, edentulous_space, mouth)) {
								Component indirect_retainer = rotation_axis.properIndirectRetainet(mouth, plan);
								if (indirect_retainer != null) {
									plan.addComponent(indirect_retainer);
									plan.addAbutmentTeeth(indirect_retainer.getToothPos());
									StringBuilder explanation = new StringBuilder();
									for (Tooth tooth : indirect_retainer.getToothPos()) {
										explanation.append(tooth.toString());
									}
									explanation.append("：");
									explanation.append("游离缺失，缺失侧转动轴前侧无间接固位体，增设间接固位体\n");
									plan.appendPlanExplanation(explanation.toString());
								}
							}
						}
					}
				}
				res.addAll(rpd_plans);
				return res;
			}
		});
	}

	public List<RPDPlan> apply(List<RPDPlan> rpd_plans, List<EdentulousSpace> edentulous_spaces)
			throws RuleException, ToothPosException, ClaspAssemblyException, EdentulousTypeException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
}
