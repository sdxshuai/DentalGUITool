package rpd.rules;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.CombinedClasp;
import rpd.components.Component;
import rpd.components.EmbrasureClasp;
import rpd.conceptions.*;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.util.*;

import static java.lang.Math.abs;

//规则2
public class ScoreRule {

    public static List<ScoreRule> score_rules = null;

    private static Mouth mouth = null;

    public static void initRules(Mouth mouth_obj) {

        mouth = mouth_obj;
        score_rules = new ArrayList<ScoreRule>();

        score_rules.add(new ScoreRule() {

            public String getExplaination() {
                return "方案打分";
            }

            public String toString() {
                return this.getExplaination();
            }

            public int getRuleNum() {
                return 2;
            }

            public double getDistanceOppoToothTri(Tooth tooth, Tooth mesialTooth, Tooth distalTooth) {
                int targetNum = (mesialTooth.getNum() + distalTooth.getNum())/2;
                return 0.1 + 0.1 * abs(tooth.getNum() - targetNum);
            }

            public double getDistanceOppoToothRec(Tooth tooth, Tooth mesialTooth, Tooth distalTooth) {
                int mesialNum = mesialTooth.getNum();
                int distalNum = distalTooth.getNum();
                if (mesialTooth.getNum() <= 3) {
                    mesialNum = 4;
                }
                int dis1 = abs(tooth.getNum()-mesialNum);
                int dis2 = abs(tooth.getNum()-distalNum);
                return 0.1 + 0.1 * ((dis1<dis2)?dis1:dis2);
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
                double distance = 0.0;

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

                ArrayList<Tooth> abutment_teeth_without_rest = new ArrayList<>();
                for (Component component:plan.getComponents()) {
                    if (!component.isRest()) {
                        abutment_teeth_without_rest.addAll(component.getToothPos());
                    }
                }

                boolean recPenaltyFlag = false;
                for (Tooth tooth : abutment_teeth_without_rest) {
                    if (plan.getPosition() == Position.Mandibular) {
                        if (mouth.getMandibular().isZoneNoMissing(tooth.getZone())) {
                            canine_weight = 2.2;
                            premolar_weight = 0.02;
                            distomolar_weight = 0.01;
                            if (isDisociate(mouth.getMandibular().getEdentulousSpaces())) {
                                //如果当前未惩罚对侧四边形布局，则惩罚
                                recPenaltyFlag = !recPenaltyFlag;
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
                            canine_weight = 0.3;
                            premolar_weight = 0.2;
                            distomolar_weight = 0.1;
                            Map<String, Object> info = plan.getNearestEdentulous(tooth);
                            distance = (Integer) info.get("distance");
                        }
                    } else {
                        if (mouth.getMaxillary().isZoneNoMissing(tooth.getZone())) {
                            canine_weight = 2.2;
                            premolar_weight = 0.02;
                            distomolar_weight = 0.01;

                            if (isDisociate(mouth.getMaxillary().getEdentulousSpaces())) {
                                recPenaltyFlag = !recPenaltyFlag;
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
                            canine_weight = 0.3;
                            premolar_weight = 0.2;
                            distomolar_weight = 0.1;
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
                if (recPenaltyFlag) {
                    score += 10;
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
                        if (plan.getPosition() == Position.Mandibular) {
                            if (mouth.getMandibular().isZoneNoMissing(component.getToothPos().get(0).getZone())) {
                                score -= 0.1;
                            }
                            else {
                                score -= 10;
                            }
                        }
                        else {
                            if (mouth.getMaxillary().isZoneNoMissing(component.getToothPos().get(0).getZone())) {
                                score -= 0.1;
                            }
                            else {
                                score -= 10;
                            }
                        }
//						score -= 10;
                    }
                }
                return score;
            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
                List<RPDPlan> res = new ArrayList<>();
                Map<RPDPlan, Double> score_map = new HashMap<RPDPlan, Double>();
                for (RPDPlan plan : rpd_plans) {
                    if (plan.getAbutmentTeeth().contains(mouth.getTooth(1, 7))
                            && plan.getAbutmentTeeth().contains(mouth.getTooth(2, 6))
                            && plan.getAbutmentTeeth().contains(mouth.getTooth(1, 5))
                            && plan.getAbutmentTeeth().contains(mouth.getTooth(2, 5))) {
                        int temp = 0;
                    }
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
                    int abutmentSize = cur_map.getKey().getAbutmentTeeth().size();
                    for (Component component:cur_map.getKey().getComponents()) {
                        if (component.isRest()) {
                            abutmentSize = abutmentSize - 1;
                        }
                    }

                    if (abutmentSize == 2) {
                        if (count_2 <= total_count) {
                            res.add(cur_map.getKey());
                            count_2++;
                        }
                    }
                    if (abutmentSize == 3) {
                        if (count_3 <= total_count) {
                            res.add(cur_map.getKey());
                            count_3++;
                        }
                    }
                    if (abutmentSize == 4) {
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

    public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException, ClaspAssemblyException, ToothPosException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}


}
