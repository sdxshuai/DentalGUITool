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
				return "如果不需要大连接体，且单侧缺失，基牙只在缺失侧选择";
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
				Position position = rpd_plans.get(0).getPosition();
				if (!needMajorConnector(position) && isOneSideMissing(mouth, position)){
					for (RPDPlan plan:rpd_plans) {
						if (isSingleZone(plan.getAbutmentTeeth())) {
							ArrayList<Tooth> abutmentTeeth = new ArrayList<>(plan.getAbutmentTeeth());
							int abutmentZone = abutmentTeeth.get(0).getZone();
							int missingZone;
							if (position == Position.Mandibular) {
								missingZone = mouth.getMandibular().getMissingTeeth().get(0).getZone();
							}
							else {
								missingZone = mouth.getMaxillary().getMissingTeeth().get(0).getZone();
							}
							if (missingZone == abutmentZone) {
								res.add(plan);
							}
						}
					}

				}
				else {
					res.addAll(rpd_plans);
				}
				return res;
			}


		});

		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "如果非（一侧除切牙外全部缺牙，或不需要大连接体），不能全部在同一个zone";
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
				if (isOneSideAllMissingExceptIncisor(mouth, rpd_plans.get(0).getPosition())
						|| !needMajorConnector(rpd_plans.get(0).getPosition())) {
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

		choose_abutment_rules.add(new ChooseAbutmentRule() {

			public String getExplaination() {
				return "Kennedy IV类，第一前磨牙（不包括第一前磨牙）之前不放置卡环";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 6;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan : rpd_plans) {
					KennedyType kennedyType = null;
					if (plan.getPosition() == Position.Mandibular) {
						kennedyType = mouth.getMandibular().getKennedyType();
					}
					else {
						kennedyType = mouth.getMaxillary().getKennedyType();
					}
					if (kennedyType == KennedyType.KENNEDY_TYPE_IV) {
						int tooth_num = 0;
						boolean wrong_flag = false;
						for (Tooth tooth : plan.getAbutmentTeeth()) {
							tooth_num = tooth.getNum();
							if (tooth_num <= 3) {
								wrong_flag = true;
								break;
							}
						}
						if (!wrong_flag) {
							res.add(plan);
						}
					}
					else {
						res.add(plan);
					}
				}
				return res;
			}
		});
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

	public boolean isOneSideMissing(Mouth mouth, Position position) {
		boolean flag = true;
		if (position == Position.Mandibular) {
			List<Tooth> missingTeeth = mouth.getMandibular().getMissingTeeth();
			int curMissingZone = missingTeeth.get(0).getZone();
			for (Tooth tooth:missingTeeth) {
				if (tooth.getZone() != curMissingZone) {
					flag = false;
					break;
				}
			}
		}
		else if (position == Position.Maxillary) {
			List<Tooth> missingTeeth = mouth.getMaxillary().getMissingTeeth();
			int curMissingZone = missingTeeth.get(0).getZone();
			for (Tooth tooth:missingTeeth) {
				if (tooth.getZone() != curMissingZone) {
					flag = false;
					break;
				}
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

	public boolean needMajorConnector(Position planPosition) {
		boolean flag = false;
		List<EdentulousSpace> edentulousSpaceList;
		List<Tooth> missingTeeth;
		boolean isMissingFrontTeeth;
		if (planPosition == Position.Mandibular) {
			edentulousSpaceList = mouth.getMandibular().getEdentulousSpaces();
			isMissingFrontTeeth = mouth.getMandibular().isMissingFrontTeeth();
			missingTeeth = mouth.getMandibular().getMissingTeeth();
		}
		else {
			edentulousSpaceList = mouth.getMaxillary().getEdentulousSpaces();
			isMissingFrontTeeth = mouth.getMaxillary().isMissingFrontTeeth();
			missingTeeth = mouth.getMaxillary().getMissingTeeth();
		}

		if (isMissingFrontTeeth) {
			return true;
		}

		//缺隙必须为非游离缺失
		for (EdentulousSpace edentulousSpace:edentulousSpaceList) {
			if (edentulousSpace.getEdentulousType() == EdentulousType.PosteriorExtension) {
			    return true;
			}
//
//			if (edentulousSpace.getLeftMost().getZone() != edentulousSpace.getRightMost().getZone()
//					|| edentulousSpace.getLeftMost().getNum() != edentulousSpace.getRightMost().getNum()) {
//				flag = true;
//				break;
//			}
		}

		if (isOneSideMissing(mouth, planPosition)) {
			if (missingTeeth.size() == 2) {
				if (missingTeeth.get(0).getNum() - missingTeeth.get(1).getNum() > 2) {
					return true;
				}
			}
		}
		else {
			int countLeft = 0;
			int countRight = 0;
			for (Tooth tooth:missingTeeth) {
				if (tooth.getZone() == 1 || tooth.getZone() == 4) {
					countLeft++;
					if (countLeft > 1) {
						return true;
					}
				}
				else {
					countRight++;
					if (countRight > 1) {
						return true;
					}
				}
			}
		}

		return flag;
	}
}
