package rpd.rules;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.*;
import rpd.conceptions.*;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.util.*;
import static java.lang.Math.abs;

//规则2
public class ClaspRule {

	public static List<ClaspRule> clasp_rules = null;

	private static Mouth mouth = null;

	public static void initRules(Mouth mouth_obj) {

		mouth = mouth_obj;
		clasp_rules = new ArrayList<ClaspRule>();

		clasp_rules.add(new ClaspRule() {

			public String getExplaination() {
				return "初始化搜索树，遍历选择卡环";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 1;
			}

			public boolean isDislocate(Tooth tooth) {
				boolean distal_all_missing_flag = true;
				if (tooth.getNum() == 7 || tooth.getNum() == 8) {
					return false;
				}

				int cur_zone = tooth.getZone();
				int cur_num = tooth.getNum();
				for (int num = cur_num + 1; num <= 8; num++) {
					if (!mouth.getTooth(cur_zone, num).isMissing()) {
						distal_all_missing_flag = false;
						break;
					}
				}

				if (tooth.getNum() == 6 && distal_all_missing_flag) {
					int occlusal_zone = tooth.getOcclusalZone();
					return !mouth.getTooth(occlusal_zone, 7).isMissing()
							|| !mouth.getTooth(occlusal_zone, 8).isMissing();
				} else {
					return distal_all_missing_flag;
				}
			}

			public boolean isIsolate(Tooth tooth) {
				int cur_num = tooth.getNum();
				int cur_zone = tooth.getZone();
				return mouth.getTooth(cur_zone, cur_num + 1).isMissing()
						&& mouth.getTooth(cur_zone, cur_num - 1).isMissing();
			}

			public boolean isDistalIsolate(Tooth tooth) {
				// 远中孤立判定方式：
				// 1）对于7,8：其后面的牙齿都缺失，且其前连续缺失大于等于5颗
				// 2）对于6：78位置都缺失，且对合78都缺失，且其前连续缺失大于等于5
				int cur_num = tooth.getNum();
				int cur_zone = tooth.getZone();
				// 限制于6,7,8
				if (cur_num <= 5) {
					return false;
				}
				// 其后牙齿都缺失
				for (int num = cur_num + 1; num <= 8; num++) {
					if (!mouth.getTooth(cur_zone, num).isMissing()) {
						return false;
					}
				}

				boolean before_succ_missing_flag = true;
				for (int num = cur_num - 1; num >= cur_num - 5; num--) {
					if (!mouth.getTooth(cur_zone, num).isMissing()) {
						before_succ_missing_flag = false;
						break;
					}
				}

				if (cur_num == 6 && before_succ_missing_flag) {
					int occlusal_zone = tooth.getOcclusalZone();
					return !mouth.getTooth(occlusal_zone, 7).isMissing()
							|| !mouth.getTooth(occlusal_zone, 8).isMissing();
				} else {
					return before_succ_missing_flag;
				}
			}

			public boolean isBothSideMissing(ArrayList<Tooth> teeth) {
				// 基牙双侧缺失
				Collections.sort(teeth);
				Tooth tooth_mesial = teeth.get(0);
				Tooth tooth_distal = teeth.get(1);

				if (tooth_distal.getNum() == 7 || tooth_mesial.getNum() == 1) {
					return true;
				}
				if (tooth_distal.getZone() != tooth_mesial.getZone()) {
					System.out.println("Error: wrong successive position for clasp");
					return false;
				}

				boolean flag_distal_missing = false;
				boolean flag_mesial_missing = false;
				int cur_zone = tooth_distal.getZone();
				int distal_num = tooth_distal.getNum();
				int mesial_num = tooth_mesial.getNum();
				for (int num = distal_num; num <= 7; num++) {
					if (mouth.getTooth(cur_zone, num).isMissing()) {
						flag_distal_missing = true;
						break;
					}
				}
				for (int num = mesial_num; num >= 1; num--) {
					if (mouth.getTooth(cur_zone, num).isMissing()) {
						flag_mesial_missing = true;
						break;
					}
				}
				return flag_distal_missing && flag_mesial_missing;
			}

			public Clasp chooseClaspOnMultiTeeth(
					ArrayList<Tooth> teeth, RPDPlan plan, StringBuilder explanation) throws RuleException {

				if (teeth.size() == 1) {
					return chooseClaspOnTooth(teeth.get(0), plan, explanation);
				} else if (teeth.size() == 2) {
					for (Tooth tooth : teeth) {
						if (tooth.getToothType() == ToothType.Canine) {
							return null;
						}
					}
					ClaspMaterial material = ClaspMaterial.Cast;
					for (Tooth tooth : teeth) {
						if (tooth.isBadPeriphery()) {
							material = ClaspMaterial.WW;
							break;
						}
					}
					if (material == ClaspMaterial.WW) {
						explanation.append("基牙牙周状况不佳，选择弯制材料，");
					}
					else {
						explanation.append("基牙牙周状况良好，选择铸造材料，");
					}

					if (!isBothSideMissing(teeth)) {
						explanation.append("单侧缺失，且距离缺失区距离大于2个牙位，");
						Map<String, Object> info_0 = plan.getNearestEdentulous(teeth.get(0));
						Map<String, Object> info_1 = plan.getNearestEdentulous(teeth.get(1));
						int distance0 = (Integer) info_0.get("distance");
						int distance1 = (Integer) info_1.get("distance");

						if (distance0 >= 2 && distance1 >= 2) {
							if (material == ClaspMaterial.WW) {
								explanation.append("选择间隙（Embrasure）卡环\n");
								return new EmbrasureClasp(teeth);
							} else {
								explanation.append("选择联合（Combined）卡环\n");
								return new CombinedClasp(teeth);
							}
						}
					} else {
						if (material == ClaspMaterial.WW) {
							explanation.append("双侧缺失，选择连续（Continuous）卡环\n");
							return new ContinuousClasp(teeth, material);
						} else {
							return null;
						}
					}
				} else {
					System.out.println("Error: Wrong number of successive multi teeth while chooseClaspOnMultiTeeth");
					return null;
				}
				return null;
			}

			public Clasp chooseClaspOnTooth(Tooth tooth, RPDPlan plan, StringBuilder explanation) throws RuleException {
				Clasp res = null;
				if (tooth.getToothType() == ToothType.Canine) {
					res = chooseClaspOnCanine(tooth, plan, explanation);
				} else if (tooth.getToothType() == ToothType.Premolar) {
					res = chooseClaspOnPremolar(tooth, plan, explanation);
				} else if (tooth.getToothType() == ToothType.Molar) {
					res = chooseClaspOnDistomolar(tooth, plan, explanation);
				} else {
					System.out.println("Error: Wrong tooth type while chooseClaspOnTooth!");
				}
				return res;
			}

			public Clasp chooseClaspOnCanine(Tooth tooth, RPDPlan plan, StringBuilder explanation) throws RuleException {
				ClaspMaterial material = ClaspMaterial.Cast;
				if (tooth.isBadPeriphery()) {
					material = ClaspMaterial.WW;
					explanation.append("基牙牙周状况不佳，选择弯制材料，");
				}
				else {
					explanation.append("基牙牙周状况良好，选择铸造材料，");
				}

				if (isDislocate(tooth) && tooth.getCrownRootRatio() == CrownRootRatio.SHORT) {
					explanation.append("基牙为尖牙，后牙游离缺失，基牙冠短，选择回力（Back Action）卡环\n");
					return new BackActionClasp(tooth, material);
				} else if (tooth.isCingulum()) {
					explanation.append("基牙为尖牙，基牙舌隆突不明显，选择尖牙（Canine）卡环\n");
					return new CanineClasp(tooth, material);
				} else {
//					Map<String, Object> info = plan.getNearestEdentulous(tooth);
//					Position tip_direction = (Position) info.get("direction");
					explanation.append("基牙为尖牙，基牙舌隆突明显，选择尖牙上Aker（Canine Aker）卡环\n");
					return new CanineAkerClasp(tooth, Position.Mesial, material);
				}
			}

			public Clasp chooseClaspOnPremolar(Tooth tooth, RPDPlan plan, StringBuilder explanation) throws RuleException {
				ClaspMaterial material = ClaspMaterial.Cast;
				if (tooth.isBadPeriphery()) {
					material = ClaspMaterial.WW;
					explanation.append("基牙牙周状况不佳，选择弯制材料，");
				}
				else {
					explanation.append("基牙牙周状况良好，选择铸造材料，");
				}

				if (isDislocate(tooth)) {
					explanation.append("基牙为前磨牙，紧邻游离缺失，");
					if (tooth.getCrownRootRatio() == CrownRootRatio.SHORT
							|| tooth.getBuccalSurfaceSlope()
							|| tooth.getLingualSurfaceSlope()) {
						if (tooth.getCrownRootRatio() == CrownRootRatio.SHORT) {
							explanation.append("基牙冠短，");
						}
						else if (tooth.getBuccalSurfaceSlope()) {
							explanation.append("基牙颊面坡度小于20度，");
						}
						else if (tooth.getLingualSurfaceSlope()) {
							explanation.append("基牙舌面坡度小于20度，");
						}
						explanation.append("选择回力（Back Action）卡环\n");
						return new BackActionClasp(tooth, material);
					} else if (tooth.isMesialInclination()) {
					    explanation.delete(0, explanation.length());
						explanation.append("基牙近中倾斜，选择弯制（Wrought Wire）卡环\n");
						Position tip_direction = Position.Mesial;
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						explanation.append("缺失多颗，选择RPA卡环\n");
						return new RPAClasp(tooth, material);
					}
				} else if (isIsolate(tooth)) {
					if (material == ClaspMaterial.WW) {
						explanation.append("基牙为孤立前磨牙，牙周状况不佳，选择弯制（Wrought Wire）卡环\n");
						Position tip_direction = Position.Mesial;
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						explanation.append("基牙为孤立前磨牙，牙周状况良好，选择对半（Half and Half）卡环\n");
						return new HalfHalfClasp(tooth, material);
					}
				} else {
					Map<String, Object> info = plan.getNearestEdentulous(tooth);
					Position tip_direction = (Position) info.get("direction");
					explanation.append("基牙无其他特殊状况，选择Aker卡环\n");
					if (material == ClaspMaterial.WW) {
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						return new AkerClasp(tooth, tip_direction);
					}
				}
			}

			public Clasp chooseClaspOnDistomolar(Tooth tooth, RPDPlan plan, StringBuilder explanation) throws RuleException {
				ClaspMaterial material = ClaspMaterial.Cast;
				if (tooth.isBadPeriphery()) {
					material = ClaspMaterial.WW;
					explanation.append("基牙牙周状况不佳，选择弯制材料，");
				}
				else {
					explanation.append("基牙牙周状况良好，选择铸造材料，");
				}

				if (isDistalIsolate(tooth)) {
					if (material == ClaspMaterial.WW) {
						explanation.append("基牙为远中孤立后磨牙，牙周状况不佳，选择弯制（Wrought Wire）卡环\n");
						Position tip_direction = Position.Mesial;
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						explanation.append("基牙为远中孤立后磨牙，牙周状况良好，选择圈形（Ring）卡环\n");
						return new RingClasp(tooth, material);
					}
				} else if (isDislocate(tooth)) {
					explanation.append("基牙为后磨牙，紧邻游离缺失，");
					if (tooth.isMesialInclination()) {
						explanation.delete(0, explanation.length());
						explanation.append("基牙近中倾斜，选择弯制（Wrought Wire）卡环\n");
						Position tip_direction = Position.Mesial;
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						explanation.append("缺失单颗，选择Aker卡环\n");
						Position tip_direction = Position.Mesial;
						if (material == ClaspMaterial.WW) {
							return new WroughtWireClasp(tooth, tip_direction);
						} else {
							return new AkerClasp(tooth, tip_direction);
						}
					}
				} else {
					Map<String, Object> info = plan.getNearestEdentulous(tooth);
					Position tip_direction = (Position) info.get("direction");
					explanation.append("基牙无其他特殊状况，选择Aker卡环\n");
					if (material == ClaspMaterial.WW) {
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						return new AkerClasp(tooth, tip_direction);
					}
				}
			}

			public void getPlans(ArrayList<ArrayList<Tooth>> with_multi_list, RPDPlan abutment_plan, List<RPDPlan> plans)
					throws RuleException {
				RPDPlan new_plan = new RPDPlan(abutment_plan);
				for (ArrayList<Tooth> teeth : with_multi_list) {
					StringBuilder explanation = new StringBuilder();
					for (Tooth tooth:teeth) {
						explanation.append(tooth.toString());
					}
					explanation.append("：");
					Clasp clasp = chooseClaspOnMultiTeeth(teeth, abutment_plan, explanation);
					if (clasp == null) {
						return;
					}
					new_plan.addComponent(clasp);
					new_plan.appendPlanExplanation(explanation.toString());
				}
				plans.add(new_plan);
			}

			public void getPlans(List<Tooth> no_multi_list, RPDPlan abutment_plan, List<RPDPlan> plans)
					throws RuleException {
				RPDPlan new_plan = new RPDPlan(abutment_plan);
				for (Tooth tooth : no_multi_list) {
					StringBuilder explanation = new StringBuilder();
					explanation.append(tooth.toString());
					explanation.append("：");
					Clasp clasp = chooseClaspOnTooth(tooth, abutment_plan, explanation);
					if (clasp == null) {
						return;
					}
					new_plan.addComponent(clasp);
					new_plan.appendPlanExplanation(explanation.toString());
				}
				plans.add(new_plan);
			}


			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					ArrayList<Tooth> abutment_teeth = new ArrayList<>();
					abutment_teeth.addAll(plan.getAbutmentTeeth());
					Collections.sort(abutment_teeth);
					List<Tooth> no_multi_list = new ArrayList<>(abutment_teeth);
					getPlans(no_multi_list, plan, res);

					ArrayList<ArrayList<Tooth>> succ_tooth_list = new ArrayList<ArrayList<Tooth>>(); //连续牙位列表
					Set<Tooth> succ_tooth_set = new HashSet<>(); //连续牙位位置集合

					//找到可能的放置多牙卡环的位置,最长连续2个牙位
					int last_num = 0;
					Tooth last_tooth = null;
					for (Tooth tooth : abutment_teeth) {
						int tooth_num = Integer.parseInt(tooth.toString().substring(5));
						if (tooth_num - last_num == 1 || last_num - tooth_num == 1) {
							ArrayList<Tooth> current_teeth = new ArrayList<Tooth>();
							current_teeth.add(last_tooth);
							current_teeth.add(tooth);
							succ_tooth_list.add(current_teeth);
							succ_tooth_set.addAll(current_teeth);
						}
						last_num = tooth_num;
						last_tooth = tooth;
					}

					//有两个多牙卡环的位置
					if (succ_tooth_set.size() != 0) {
						//只有一个位置放多牙卡环
						for (ArrayList<Tooth> succ_teeth : succ_tooth_list) {
							ArrayList<ArrayList<Tooth>> with_multi_list = new ArrayList<ArrayList<Tooth>>();
							with_multi_list.add(succ_teeth);
							Set<Tooth> differ_set = new HashSet<>();
							differ_set.addAll(abutment_teeth);
							differ_set.removeAll(succ_teeth);
							for (Tooth differ_tooth : differ_set) {
								ArrayList<Tooth> current_list = new ArrayList<Tooth>();
								current_list.add(differ_tooth);
								with_multi_list.add(current_list);
							}
							getPlans(with_multi_list, plan, res);
						}
					}

					//两个位置都放多牙卡环
					if (succ_tooth_set.size() == 4) {
						ArrayList<ArrayList<Tooth>> with_multi_list = new ArrayList<ArrayList<Tooth>>();
						with_multi_list.add(succ_tooth_list.get(0));
						with_multi_list.add(succ_tooth_list.get(succ_tooth_list.size() - 1));
						getPlans(with_multi_list, plan, res);
					}
				}
				return res;
			}
		});
	}

	public List<RPDPlan> apply(List<RPDPlan> rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
}
