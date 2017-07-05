package rpd.rules;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import javafx.geometry.Pos;
import rpd.RPDPlan;
import rpd.components.*;
import rpd.conceptions.*;
import rpd.oral.EdentulousSpace;
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
				boolean flag = true;
				if (tooth.getNum() == 7) {
					return false;
				}
				int cur_zone = tooth.getZone();
				int cur_num = tooth.getNum();
				for (int num = cur_num + 1; num <= 8; num++) {
					if (mouth.getTooth(cur_zone, num).isMissing() != true) {
						flag = false;
						break;
					}
				}
				return flag;
			}

			public boolean isIsolate(Tooth tooth) {
				int cur_num = tooth.getNum();
				int cur_zone = tooth.getZone();
				if (mouth.getTooth(cur_zone, cur_num + 1).isMissing() == true
						&& mouth.getTooth(cur_zone, cur_num - 1).isMissing() == true) {
					return true;
				} else {
					return false;
				}
			}

			public boolean isBadPeriphery(Tooth tooth) {
				if (tooth.getMobility() != ToothMobility.No) {
					return true;
				}
				if (tooth.getFurcationInvolvement() != FurcationInvolvement.NO) {
					return true;
				}
				if (tooth.getAlveolarAbsorption() != AlveolarAbsorption.No) {
					return true;
				}
				return false;
			}

			public boolean isMesialInclination(Tooth tooth) {
				if (tooth.getClassificationOfSurveyLineOnBuccalSurface() == ClassificationOfSurveyLineOnBuccalSurface.O
						|| tooth.getToothPosition() == ToothPosition.Mesial) {
					return true;
				} else {
					return false;
				}
			}

			public boolean isBothSideMissing(ArrayList<Tooth> teeth) {
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
				if (flag_distal_missing && flag_mesial_missing) {
					return true;
				} else {
					return false;
				}
			}

			public Clasp chooseClaspOnMultiTeeth(ArrayList<Tooth> teeth, RPDPlan plan) throws RuleException {
				if (teeth.size() == 1) {
					return chooseClaspOnTooth(teeth.get(0), plan);
				} else if (teeth.size() == 2) {
					for (Tooth tooth : teeth) {
						if (tooth.getToothType() == ToothType.Canine) {
							return null;
						}
					}
					ClaspMaterial material = ClaspMaterial.Cast;
					for (Tooth tooth : teeth) {
						if (isBadPeriphery(tooth)) {
							material = ClaspMaterial.WW;
							break;
						}
					}

					if (!isBothSideMissing(teeth)) {
						Map<String, Object> info_0 = plan.getNearestEdentulous(teeth.get(0));
						Map<String, Object> info_1 = plan.getNearestEdentulous(teeth.get(1));
						int distance0 = (Integer) info_0.get("distance");
						int distance1 = (Integer) info_1.get("distance");
						if (distance0 >= 2 && distance1 >= 2) {
							if (material == ClaspMaterial.WW) {
								return new EmbrasureClasp(teeth);
							} else {
								return new CombinedClasp(teeth);
							}
						}
					} else {
						if (material == ClaspMaterial.WW) {
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

			public Clasp chooseClaspOnTooth(Tooth tooth, RPDPlan plan) throws RuleException {
				Clasp res = null;
				if (tooth.getToothType() == ToothType.Canine) {
					res = chooseClaspOnCanine(tooth, plan);
				} else if (tooth.getToothType() == ToothType.Premolar) {
					res = chooseClaspOnPremolar(tooth, plan);
				} else if (tooth.getToothType() == ToothType.Molar) {
					res = chooseClaspOnDistomolar(tooth, plan);
				} else {
					System.out.println("Error: Wrong tooth type while chooseClaspOnTooth!");
				}
				return res;
			}

			public Clasp chooseClaspOnCanine(Tooth tooth, RPDPlan plan) throws RuleException {
				ClaspMaterial material = ClaspMaterial.Cast;
				if (isBadPeriphery(tooth)) {
					material = ClaspMaterial.WW;
				}

				if (isDislocate(tooth) && tooth.getCrownRootRatio() == CrownRootRatio.SHORT) {
					return new BackActionClasp(tooth, material);
				} else if (tooth.isCingulum()) {
					return new CanineClasp(tooth, material);
				} else {
//					Map<String, Object> info = plan.getNearestEdentulous(tooth);
//					Position tip_direction = (Position) info.get("direction");
					return new CanineAkerClasp(tooth, Position.Mesial, material);
				}
			}

			public Clasp chooseClaspOnPremolar(Tooth tooth, RPDPlan plan) throws RuleException {
				ClaspMaterial material = ClaspMaterial.Cast;
				if (isBadPeriphery(tooth)) {
					material = ClaspMaterial.WW;
				}

				if (isDislocate(tooth)) {
					if (tooth.getCrownRootRatio() == CrownRootRatio.SHORT
							|| tooth.getBuccalSurfaceSlope() == true
							|| tooth.getLingualSurfaceSlope() == true) {
						return new BackActionClasp(tooth, material);
					} else if (isMesialInclination(tooth)) {
						return new CombinationClasp(tooth);
					} else {
						return new RPAClasp(tooth, material);
					}
				} else if (isIsolate(tooth)) {
					return new HalfHalfClasp(tooth, material);
				} else {
					Map<String, Object> info = plan.getNearestEdentulous(tooth);
					Position tip_direction = (Position) info.get("direction");
					if (material == ClaspMaterial.WW) {
						return new WroughtWireClasp(tooth, tip_direction);
					} else {
						return new AkerClasp(tooth, tip_direction);
					}
				}
			}

			public Clasp chooseClaspOnDistomolar(Tooth tooth, RPDPlan plan) throws RuleException {
				ClaspMaterial material = ClaspMaterial.Cast;
				if (isBadPeriphery(tooth)) {
					material = ClaspMaterial.WW;
				}

				if (isDislocate(tooth)) {
					if (isMesialInclination(tooth)) {
						return new CombinationClasp(tooth);
					} else {
						return new RPAClasp(tooth, material);
					}
				} else if (isIsolate(tooth)) {
					return new RingClasp(tooth, material);
				} else {
					Map<String, Object> info = plan.getNearestEdentulous(tooth);
					Position tip_direction = (Position) info.get("direction");
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
					Clasp clasp = chooseClaspOnMultiTeeth(teeth, abutment_plan);
					if (clasp == null) {
						return;
					}
					new_plan.addComponent(clasp);
				}
				plans.add(new_plan);
			}

			public void getPlans(List<Tooth> no_multi_list, RPDPlan abutment_plan, List<RPDPlan> plans)
					throws RuleException {
				RPDPlan new_plan = new RPDPlan(abutment_plan);
				for (Tooth tooth : no_multi_list) {
					Clasp clasp = chooseClaspOnTooth(tooth, abutment_plan);
					if (clasp == null) {
						return;
					}
					new_plan.addComponent(clasp);
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

		clasp_rules.add(new ClaspRule() {

			public String getExplaination() {
				return "方案打分，处理连续基牙情况";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 2;
			}

			public int getDistanceOppoToothTri(Tooth tooth, Tooth mesialTooth, Tooth distalTooth) {
				int targetNum = (mesialTooth.getNum() + distalTooth.getNum())/2;
				return abs(tooth.getNum() - targetNum);
			}

			public int getDistanceOppoToothRec(Tooth tooth, Tooth mesialTooth, Tooth distalTooth) {
				int dis1 = abs(tooth.getNum()-mesialTooth.getNum());
				int dis2 = abs(tooth.getNum()-distalTooth.getNum());
				return (dis1<dis2)?dis1:dis2;
			}

			public boolean isDisociate(List<EdentulousSpace> edentulousSpaceList) {
				boolean flag = false;
				for (EdentulousSpace edentulousSpace : edentulousSpaceList) {
					if (edentulousSpace.getEdentulousType() == EdentulousType.PosteriorExtension) {
						flag = true;
						break;
					}
				}
				return flag;
			}

			public double scorePlan(RPDPlan plan) throws RuleException {
				double score = 0.0;
				double canine_weight = 0.0;
				double premolar_weight = 0.0;
				double distomolar_weight = 0.0;
				int distance = 0;

				Set<Tooth> abutment_teeth = plan.getAbutmentTeeth();
				ArrayList<Tooth> abutment_missing_teeth = new ArrayList<>(abutment_teeth);
				ArrayList<Tooth> sorted_zone1 = new ArrayList<>();
				ArrayList<Tooth> sorted_zone2 = new ArrayList<>();
				ArrayList<Tooth> sorted_zone3 = new ArrayList<>();
				ArrayList<Tooth> sorted_zone4 = new ArrayList<>();

				if (plan.getPosition() == Position.Mandibular) {
					List<Tooth> missing_teeth = mouth.getMandibular().getMissingTeeth();
					abutment_missing_teeth.addAll(missing_teeth);

					for (Tooth tooth : abutment_missing_teeth) {
						if (tooth.getZone() == 3) {
							sorted_zone3.add(tooth);
						} else {
							sorted_zone4.add(tooth);
						}
					}
					Collections.sort(sorted_zone3);
					Collections.sort(sorted_zone4);
				}
				else {
					List<Tooth> missing_teeth = mouth.getMaxillary().getMissingTeeth();
					abutment_missing_teeth.addAll(missing_teeth);

					for (Tooth tooth : abutment_missing_teeth) {
						if (tooth.getZone() == 1) {
							sorted_zone1.add(tooth);
						} else {
							sorted_zone2.add(tooth);
						}
					}
					Collections.sort(sorted_zone1);
					Collections.sort(sorted_zone2);
				}


				for (Tooth tooth : abutment_teeth) {
					if (plan.getPosition() == Position.Mandibular) {
						if (mouth.getMandibular().isZoneNoMissing(tooth.getZone())) {
							canine_weight = 2.2;
							premolar_weight = 0.5;
							distomolar_weight = 0.0;
							if (isDisociate(mouth.getMandibular().getEdentulousSpaces())) {
								if (tooth.getZone() == 3) {
									distance = getDistanceOppoToothRec(
											tooth, sorted_zone4.get(0), sorted_zone4.get(sorted_zone4.size()-1));
								}
								else {
									distance = getDistanceOppoToothRec(
											tooth, sorted_zone3.get(0), sorted_zone3.get(sorted_zone3.size()-1));
								}
							}
							else {
								if (tooth.getZone() == 3) {
									distance = getDistanceOppoToothTri(
											tooth, sorted_zone4.get(0), sorted_zone4.get(sorted_zone4.size()-1));
								}
								else {
									distance = getDistanceOppoToothTri(
											tooth, sorted_zone3.get(0), sorted_zone3.get(sorted_zone3.size()-1));
								}
							}

						} else {
							canine_weight = 0.8;
							premolar_weight = 0.5;
							distomolar_weight = 0.0;
							Map<String, Object> info = plan.getNearestEdentulous(tooth);
							distance = (Integer) info.get("distance");
						}
					} else {
						if (mouth.getMaxillary().isZoneNoMissing(tooth.getZone())) {
							canine_weight = 2.2;
							premolar_weight = 0.5;
							distomolar_weight = 0.0;

							if (isDisociate(mouth.getMaxillary().getEdentulousSpaces())) {
								if (tooth.getZone() == 1) {
									distance = getDistanceOppoToothRec(
											tooth, sorted_zone2.get(0), sorted_zone2.get(sorted_zone2.size()-1));
								}
								else {
									distance = getDistanceOppoToothRec(
											tooth, sorted_zone1.get(0), sorted_zone1.get(sorted_zone1.size()-1));
								}
							}
							else {
								if (tooth.getZone() == 1) {
									distance = getDistanceOppoToothTri(
											tooth, sorted_zone2.get(0), sorted_zone2.get(sorted_zone2.size()-1));
								}
								else {
									distance = getDistanceOppoToothTri(
											tooth, sorted_zone1.get(0), sorted_zone1.get(sorted_zone1.size()-1));
								}
							}
						} else {
							canine_weight = 0.8;
							premolar_weight = 0.5;
							distomolar_weight = 0.0;
							Map<String, Object> info = plan.getNearestEdentulous(tooth);
							distance = (Integer) info.get("distance");
						}
					}

//					Map<String, Object> info = plan.getNearestEdentulous(tooth);
//					distance = (Integer) info.get("distance");
					if (tooth.getToothType() == ToothType.Canine) {
						score += distance + canine_weight;
					} else if (tooth.getToothType() == ToothType.Premolar) {
						score += distance + premolar_weight;
					} else {
						score += distance + distomolar_weight;
					}
				}

				for (Component component : plan.getComponents()) {
					if (plan.getPosition() == Position.Mandibular) {
						Tooth cur_tooth = component.getToothPos().get(0);
						if (mouth.getMandibular().isZoneNoMissing(cur_tooth.getZone())
								&& isDisociate(mouth.getMandibular().getEdentulousSpaces())) {
							continue;
						}
					}
					else {
						Tooth cur_tooth = component.getToothPos().get(0);
						if (mouth.getMaxillary().isZoneNoMissing(cur_tooth.getZone())
								&& isDisociate(mouth.getMaxillary().getEdentulousSpaces())) {
							continue;
						}
					}

					if (component.getClass() == CombinedClasp.class || component.getClass() == EmbrasureClasp.class) {
						score -= 5;
					}
				}
				return score;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				Map<RPDPlan, Double> score_map = new HashMap<RPDPlan, Double>();
				for (RPDPlan plan : rpd_plans) {
					double score = scorePlan(plan);
					score_map.put(plan, score);
				}
				List<Map.Entry<RPDPlan, Double>> list = new ArrayList<Map.Entry<RPDPlan, Double>>(score_map.entrySet());
				// 通过比较器实现比较排序
				Collections.sort(list, new Comparator<Map.Entry<RPDPlan, Double>>() {
					public int compare(Map.Entry<RPDPlan, Double> mapping1, Map.Entry<RPDPlan, Double> mapping2) {
						return mapping1.getValue().compareTo(mapping2.getValue());
					}
				});

				int total_count = 1;
				int count_2 = 1;
				int count_3 = 1;
				int count_4 = 1;
				for (Map.Entry<RPDPlan, Double> cur_map : list) {
					ArrayList<Tooth> abutment_teeth = new ArrayList<>();
					abutment_teeth.addAll(cur_map.getKey().getAbutmentTeeth());
					Collections.sort(abutment_teeth);
					if (cur_map.getKey().getAbutmentTeeth().size() == 2) {
						if (count_2 <= total_count) {
							res.add(cur_map.getKey());
							count_2++;
						}
					}
					if (cur_map.getKey().getAbutmentTeeth().size() == 3) {
						if (count_3 <= total_count) {
							res.add(cur_map.getKey());
							count_3++;
						}
					}
					if (cur_map.getKey().getAbutmentTeeth().size() == 4) {
						if (count_4 <= total_count) {
							res.add(cur_map.getKey());
							count_4++;
						}
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
