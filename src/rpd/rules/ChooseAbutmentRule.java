package rpd.rules;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.conceptions.*;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.util.*;

//规则2
public class ChooseAbutmentRule {

	public static List<ChooseAbutmentRule> choose_abutment_rules = null;

	private static Mouth mouth = null;

	public static void initRules(Mouth mouth_obj) {

		mouth = mouth_obj;
		choose_abutment_rules = new ArrayList<ChooseAbutmentRule>();

		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "初始化搜索树，遍历选择2-4颗基牙";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 1;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {

				List<RPDPlan> res = new ArrayList<>();
				List<Tooth> existing_teeth = new ArrayList<Tooth>();
				existing_teeth.addAll(rpd_plans.get(0).getExistingTeeth());
				Iterator<Tooth> tooth_iterator = existing_teeth.iterator();
				while (tooth_iterator.hasNext()) {
					Tooth tooth = tooth_iterator.next();
					if (tooth.getToothType().equals(ToothType.Incisor)) {
						tooth_iterator.remove();
					}
				}

				for (RPDPlan plan : rpd_plans) {
					int list_size = existing_teeth.size();
					HashSet<Tooth> hashSet = new HashSet<Tooth>();
					ArrayList<ArrayList<Tooth>> abutment_teeth_list = new ArrayList<ArrayList<Tooth>>();
					getCrossCombination(existing_teeth, 0, list_size, 2, hashSet, abutment_teeth_list);
					getCrossCombination(existing_teeth, 0, list_size, 3, hashSet, abutment_teeth_list);
					getCrossCombination(existing_teeth, 0, list_size, 4, hashSet, abutment_teeth_list);

					for (ArrayList<Tooth> abutment_teeth : abutment_teeth_list) {
						RPDPlan new_plan = new RPDPlan(plan);
						new_plan.addAbutmentTeeth(abutment_teeth);
						res.add(new_plan);
					}
				}
				return res;
			}
		});
		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "基牙情况极差不能做基牙";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 2;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					if (!isPlanWithExtremeBadAbutment(plan)) {
						res.add(plan);
					}
				}
				return res;
			}
		});

		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "如果不是一侧除切牙外全部缺牙，不能全部在同一个zone";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 3;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				if (rpd_plans.size() == 0 || rpd_plans == null) {
					return rpd_plans;
				}
				List<RPDPlan> res = new ArrayList<>();
				if (isOneSideAllMissingExceptIncisor(mouth, rpd_plans.get(0).getPosition())) {
					res.addAll(rpd_plans);
				} else {
					for (RPDPlan plan : rpd_plans) {
						if (!isSingleZone(plan.getAbutmentTeeth())) {
							res.add(plan);
						}
					}
				}
				return res;
			}
		});

		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "缺失距离检测，基牙距离缺失区不能大于3个牙位";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 4;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					if (!isDistanceGreaterThanThres(plan)) {
						res.add(plan);
					}
				}
				return res;
			}
		});

		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "最多两个连续牙位作为基牙";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 5;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					if (getMaxSuccessiveAbutmentNum(plan) <= 2) {
						res.add(plan);
					}
				}
				return res;
			}
		});
//        choose_abutment_rules.add(new ChooseAbutmentRule() {
//
//            public String getExplaination() {
//                return "排序剪枝，每种数量的基牙选择3种，同时保留有连续基牙的方案，后续处理";
//            }
//
//            public String toString() {
//                return this.getExplaination();
//            }
//
//            public int getRuleNum() {
//                return 6;
//            }
//
//            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
//                List<RPDPlan> res = new ArrayList<>();
//                Map<RPDPlan, Double> score_map = new HashMap<RPDPlan, Double>();
//                for (RPDPlan plan:rpd_plans) {
//                    double score = scorePlan(plan);
//                    score_map.put(plan, score);
//                }
//                List<Map.Entry<RPDPlan, Double>> list = new ArrayList<Map.Entry<RPDPlan, Double>>(score_map.entrySet());
//                // 通过比较器实现比较排序
//                Collections.sort(list, new Comparator<Map.Entry<RPDPlan, Double>>() {
//                    public int compare(Map.Entry<RPDPlan, Double> mapping1, Map.Entry<RPDPlan, Double> mapping2) {
//                        return mapping1.getValue().compareTo(mapping2.getValue());
//                    }
//                });
//
//                int total_count = 10;
//                int count_2 = 1;
//                int count_3 = 1;
//                int count_4 = 1;
//                for (Map.Entry<RPDPlan, Double> cur_map:list) {
//                    ArrayList<Tooth> abutment_teeth = new ArrayList<>();
//                    abutment_teeth.addAll(cur_map.getKey().getAbutmentTeeth());
//                    Collections.sort(abutment_teeth);
//                	if (hasSuccessiveAbutment(abutment_teeth)) {
//                	    res.add(cur_map.getKey());
//                	    continue;
//                    }
//                    if (cur_map.getKey().getAbutmentTeeth().size() == 2) {
//                        if (count_2 <= total_count) {
//                            res.add(cur_map.getKey());
//                            count_2++;
//                        }
//                    }
//                    if (cur_map.getKey().getAbutmentTeeth().size() == 3) {
//                        if (count_3 <= total_count) {
//                            res.add(cur_map.getKey());
//                            count_3++;
//                        }
//                    }
//                    if (cur_map.getKey().getAbutmentTeeth().size() == 4) {
//                        if (count_4 <= total_count) {
//                            res.add(cur_map.getKey());
//                            count_4++;
//                        }
//                    }
//                }
//                return res;
//            }
//        });
	}

	public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException, ClaspAssemblyException, ToothPosException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public boolean isAllDistomolar(Set<Tooth> abutment_teeth) {
		boolean flag = true;
		int tooth_num = 0;
		for (Tooth tooth : abutment_teeth) {
			tooth_num = tooth.getNum();
			if (tooth_num != 6 && tooth_num != 7 && tooth_num != 8) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public boolean isAllPremolar(Set<Tooth> abutment_teeth) {
		boolean flag = true;
		int tooth_num = 0;
		for (Tooth tooth : abutment_teeth) {
			tooth_num = tooth.getNum();
			if (tooth_num != 4 && tooth_num != 5) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public boolean isAllCanine(Set<Tooth> abutment_teeth) {
		boolean flag = true;
		int tooth_num = 0;
		for (Tooth tooth : abutment_teeth) {
			tooth_num = tooth.getNum();
			if (tooth_num != 3) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public boolean isAllIncisor(Set<Tooth> abutment_teeth) {
		boolean flag = true;
		int tooth_num = 0;
		for (Tooth tooth : abutment_teeth) {
			tooth_num = tooth.getNum();
			if (tooth_num != 1 && tooth_num != 2) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public boolean isOneSideAllMissing(Mouth mouth, Position position) {
		boolean flag = false;
		if (position == Position.Mandibular) {
			if (mouth.getMandibular().isZone3AllMissing() || mouth.getMandibular().isZone4AllMissing()) {
				flag = true;
			}
		} else if (position == Position.Maxillary) {
			if (mouth.getMaxillary().isZone1AllMissing() || mouth.getMaxillary().isZone2AllMissing()) {
				flag = true;
			}
		}
		return flag;
	}

	public boolean isOneSideAllMissingExceptIncisor(Mouth mouth, Position position) {
		boolean flag = false;
		if (position == Position.Mandibular) {
			if (mouth.getMandibular().isZone3AllMissingExceptIncisor()
					|| mouth.getMandibular().isZone4AllMissingExceptIncisor()) {
				flag = true;
			}
		} else if (position == Position.Maxillary) {
			if (mouth.getMaxillary().isZone1AllMissingExceptIncisor()
					|| mouth.getMaxillary().isZone2AllMissingExceptIncisor()) {
				flag = true;
			}
		}
		return flag;
	}

	public boolean isSingleZone(Set<Tooth> abutment_teeth) {
		boolean flag = true;
		int tooth_zone = 0;
		int pre_tooth_zone = 0;
		for (Tooth tooth : abutment_teeth) {
			pre_tooth_zone = tooth_zone;
			tooth_zone = tooth.getZone();
			if (pre_tooth_zone != tooth_zone && pre_tooth_zone != 0) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public void getCrossCombination(List<Tooth> existing_teeth, int start, int end, int length,
	                                HashSet<Tooth> hashSet, ArrayList<ArrayList<Tooth>> res_list) {

		if (length == 0) {
			ArrayList<Tooth> res = new ArrayList<Tooth>();
			res.addAll(hashSet);
			res_list.add(res);
			return;
		}

		for (int i = start; i <= end - length; i++) {
			hashSet.add(existing_teeth.get(i));
			getCrossCombination(existing_teeth, i + 1, end, length - 1, hashSet, res_list);
			hashSet.remove((existing_teeth.get(i)));
		}
	}

	public int getDistanceBetweenAbutmentEdentulous(Tooth tooth, EdentulousSpace edentulous_space) {
		//return 基牙与缺失区域之间牙的个数
		int res = 0;
		int left_dis = 20;
		int right_dis = 20;
		Tooth left_tooth = edentulous_space.getLeftNeighbor();
		if (left_tooth != null) {
			left_dis = tooth.getToothDistance(left_tooth);
		}
		Tooth right_tooth = edentulous_space.getRightNeighbor();
		if (right_tooth != null) {
			right_dis = tooth.getToothDistance(right_tooth);
		}

		res = left_dis > right_dis ? right_dis : left_dis;

		return res;
	}

	public int getMaxSuccessiveAbutmentNum(RPDPlan plan) {
		List<Tooth> abutment_teeth = new ArrayList<>(plan.getAbutmentTeeth());
		Collections.sort(abutment_teeth);
		int maxSuc = 1;
		int tempSuc = 1;

		for (int i = 1; i < abutment_teeth.size(); i++) {
			if (abutment_teeth.get(i).getZone() == abutment_teeth.get(i - 1).getZone()
					&& abutment_teeth.get(i).getNum() - 1 == abutment_teeth.get(i - 1).getNum()) {
				tempSuc++;
			} else {
				if (tempSuc > maxSuc) {
					maxSuc = tempSuc;
				}
				tempSuc = 1;
			}
		}
		return maxSuc;
	}

	public boolean isDistanceGreaterThanThres(RPDPlan plan) throws RuleException {

		int distance = 0;
		int thres = 0;
		boolean flag = false;
		//true = 所有基牙距离缺失区超过3个牙位
		for (Tooth tooth : plan.getAbutmentTeeth()) {
			if (plan.getPosition() == Position.Mandibular) {
				if (mouth.getMandibular().isZoneNoMissing(tooth.getZone())) {
					thres = 20;
				} else {
					thres = 4;
				}
			} else {
				if (mouth.getMaxillary().isZoneNoMissing(tooth.getZone())) {
					thres = 20;
				} else {
					thres = 4;
				}
			}
			Map<String, Object> info = plan.getNearestEdentulous(tooth);
			distance = (Integer) info.get("distance");
			if (distance >= thres) {
				flag = true;
				break;
			}
		}

		return flag;
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

	public boolean isExtremeBadAbutment(Tooth tooth) {
		if (tooth.getMobility() == ToothMobility.III) {
			return true;
		}
		if (tooth.getFurcationInvolvement() == FurcationInvolvement.V
				|| tooth.getFurcationInvolvement() == FurcationInvolvement.IV) {
			return true;
		}
		if (tooth.getAlveolarAbsorption() == AlveolarAbsorption.III) {
			return true;
		}
		return false;
	}

	public boolean isPlanWithExtremeBadAbutment(RPDPlan plan) {
		boolean flag = false;
		for (Tooth tooth : plan.getAbutmentTeeth()) {
			if (isExtremeBadAbutment(tooth)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public double scorePlan(RPDPlan plan) throws RuleException {
		double canine_weight = 1.2;
		double premolar_weight = 0.5;
		double distomolar_weight = 0.0;
		double score = 0.0;
		for (Tooth tooth : plan.getAbutmentTeeth()) {
			Map<String, Object> info = plan.getNearestEdentulous(tooth);
			int distance = (Integer) info.get("distance");
			if (tooth.getToothType() == ToothType.Canine) {
				score += distance + canine_weight;
			} else if (tooth.getToothType() == ToothType.Premolar) {
				score += distance + premolar_weight;
			} else {
				score += distance + distomolar_weight;
			}
		}
		return score;
	}

	public boolean hasSuccessiveAbutment(ArrayList<Tooth> abutment_teeth) {

		int last_num = 0;
		for (Tooth tooth : abutment_teeth) {
			int tooth_num = Integer.parseInt(tooth.toString().substring(5));
			if (tooth_num - last_num == 1 || last_num - tooth_num == 1) {
				return true;
			}
			last_num = tooth_num;
		}
		return false;
	}
}
