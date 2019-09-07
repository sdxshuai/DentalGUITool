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
                if (targetNum == 3) {
                    targetNum = 4;
                }
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

            public EdentulousSpace getDisociateEdentulous(List<EdentulousSpace> edentulousSpaceList) {
                EdentulousSpace default_res = null;
                for (EdentulousSpace edentulousSpace : edentulousSpaceList) {
                    if (edentulousSpace.getEdentulousType() == EdentulousType.PosteriorExtension) {
                        return edentulousSpace;
                    }
                }
                return default_res;
            }

            public double scorePlan(RPDPlan plan) throws RuleException {
                double score = 0.0;
                double canine_weight = 0.0;
                double premolar_4_weight = 0.0;
                double premolar_5_weight = 0.0;
                double distomolar_6_weight = 0.0;
                double distomolar_7_weight = 0.0;
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

                int num_no_missing_zone_abutment = 0;
                boolean rec_opposite_flag = false;
                boolean tri_opposite_flag = false;
                boolean opposite_combined_clasp_compensate_flag = false;
                boolean KIV_commbied_clasp_penalty_flag = false;
                for (Tooth tooth : abutment_teeth_without_rest) {
                    if (plan.getPosition() == Position.Mandibular) {
                        if (mouth.getMandibular().isZoneNoMissing(tooth.getZone())) {
                            num_no_missing_zone_abutment += 1;
                            canine_weight = 2.2;
                            premolar_4_weight = 0.02;
                            premolar_5_weight = 0.02;
                            distomolar_6_weight = 0.01;
                            distomolar_7_weight = 0.01;
                            EdentulousSpace disociateEdentulous = getDisociateEdentulous(
                                    mouth.getMandibular().getEdentulousSpaces());

                            // 缺失侧游离
                            if (disociateEdentulous != null) {
                                int num_disociate_missing_teeth = disociateEdentulous.getNumMissingTeeth();
                                // 缺失侧游离，且游离缺失牙数目大于等于2：三角形规则，在对侧中间位置设置联合卡环
                                // 其他情况，四边形规则，在缺牙的起始位置的对侧设置两基牙
                                if (tooth.getZone() == 3) {
                                    if (num_disociate_missing_teeth >= 2) {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone4.get(0), sorted_zone4.get(sorted_zone4.size()-1));
                                    } else {
                                        //如果当前未惩罚对侧布局，则惩罚
                                        rec_opposite_flag = true;
                                        distance = getDistanceOppoToothRec(
                                                tooth, sorted_zone4.get(0), sorted_zone4.get(sorted_zone4.size() - 1));
                                    }
                                }
                                else {
                                    if (num_disociate_missing_teeth >= 2) {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone3.get(0), sorted_zone3.get(sorted_zone3.size()-1));
                                    } else {
                                        rec_opposite_flag = true;
                                        distance = getDistanceOppoToothRec(
                                                tooth, sorted_zone3.get(0), sorted_zone3.get(sorted_zone3.size() - 1));
                                    }
                                }
                            }
                            // 缺失侧不游离
                            else {
                                // 缺失侧非游离，且缺牙数目小于等于3，三角形规则，在对侧中间位置设置Aker卡环
                                // 缺失侧非游离，且缺牙数目大于3，三角形规则，在对侧中间位置设置联合卡环
                                int num_missing_teeth = mouth.getMandibular().getMissingTeeth().size();
                                if (tooth.getZone() == 3) {
                                    if (num_missing_teeth <= 3) {
                                        tri_opposite_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone4.get(0), sorted_zone4.get(sorted_zone4.size()-1));
                                    } else {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone4.get(0), sorted_zone4.get(sorted_zone4.size()-1));
                                    }
                                }
                                else {
                                    if (num_missing_teeth <= 3) {
                                        tri_opposite_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone3.get(0), sorted_zone3.get(sorted_zone3.size()-1));
                                    } else {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone3.get(0), sorted_zone3.get(sorted_zone3.size()-1));
                                    }
                                }
                            }

                        } else if (mouth.getMandibular().getKennedyType() == KennedyType.KENNEDY_TYPE_IV) {
                            canine_weight = 0.3;
                            premolar_4_weight = 0.202;
                            premolar_5_weight = 0.201;
                            distomolar_6_weight = 0.102;
                            distomolar_7_weight = 0.101;

                            KIV_commbied_clasp_penalty_flag = true;
                            EdentulousSpace edentulous_space = mouth.getMandibular().getEdentulousSpaces().get(0);
                            Tooth left_most = edentulous_space.getLeftMost();
                            Tooth right_most = edentulous_space.getRightMost();
                            int left_diatal_target;
                            int right_distal_target;
                            if (left_most.getNum() >= 3) {
                                left_diatal_target = 7;
                            } else {
                                left_diatal_target = 5;
                            }
                            if (right_most.getNum() >= 3) {
                                right_distal_target = 7;
                            } else {
                                right_distal_target = 5;
                            }

                            int mesial_distance = 0;
                            int distal_distance = 0;
                            Map<String, Object> info = plan.getNearestEdentulous(tooth);
                            mesial_distance = (Integer) info.get("distance");
                            if (tooth.getZone() == 4) {
                                distal_distance = abs(tooth.getNum() - left_diatal_target);
                            } else {
                                distal_distance = abs(tooth.getNum() - right_distal_target);
                            }
                            distance = Math.min(mesial_distance, distal_distance);

                        } else {
                            canine_weight = 0.3;
                            premolar_4_weight = 0.202;
                            premolar_5_weight = 0.201;
                            distomolar_6_weight = 0.102;
                            distomolar_7_weight = 0.101;
                            Map<String, Object> info = plan.getNearestEdentulous(tooth);
                            distance = (Integer) info.get("distance");
                        }
                    } else {
                        if (mouth.getMaxillary().isZoneNoMissing(tooth.getZone())) {
                            num_no_missing_zone_abutment += 1;
                            canine_weight = 2.2;
                            premolar_4_weight = 0.02;
                            premolar_5_weight = 0.02;
                            distomolar_6_weight = 0.01;
                            distomolar_7_weight = 0.01;
                            EdentulousSpace disociateEdentulous = getDisociateEdentulous(
                                    mouth.getMaxillary().getEdentulousSpaces());

                            // 缺失侧游离
                            if (disociateEdentulous != null) {
                                int num_disociate_missing_teeth = disociateEdentulous.getNumMissingTeeth();
                                // 缺失侧游离，且游离缺失牙数目大于等于2：三角形规则，在对侧中间位置设置联合卡环
                                // 其他情况，四边形规则，在缺牙的起始位置的对侧设置两基牙
                                if (tooth.getZone() == 1) {
                                    if (num_disociate_missing_teeth >= 2) {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone2.get(0), sorted_zone2.get(sorted_zone2.size()-1));
                                    } else {
                                        //如果当前未惩罚对侧布局，则惩罚
                                        rec_opposite_flag = true;
                                        distance = getDistanceOppoToothRec(
                                                tooth, sorted_zone2.get(0), sorted_zone2.get(sorted_zone2.size() - 1));
                                    }
                                }
                                else {
                                    if (num_disociate_missing_teeth >= 2) {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone1.get(0), sorted_zone1.get(sorted_zone1.size()-1));
                                    } else {
                                        rec_opposite_flag = true;
                                        distance = getDistanceOppoToothRec(
                                                tooth, sorted_zone1.get(0), sorted_zone1.get(sorted_zone1.size() - 1));
                                    }
                                }
                            }
                            else {
                                // 缺失侧非游离，且缺牙数目小于等于3，三角形规则，在对侧中间位置设置Aker卡环
                                // 缺失侧非游离，且缺牙数目大于3，三角形规则，在对侧中间位置设置联合卡环
                                int num_missing_teeth = mouth.getMaxillary().getMissingTeeth().size();
                                if (tooth.getZone() == 1) {
                                    if (num_missing_teeth <= 3) {
                                        tri_opposite_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone2.get(0), sorted_zone2.get(sorted_zone2.size()-1));
                                    } else {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone2.get(0), sorted_zone2.get(sorted_zone2.size()-1));
                                    }
                                }
                                else {
                                    if (num_missing_teeth <= 3) {
                                        tri_opposite_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone1.get(0), sorted_zone1.get(sorted_zone1.size()-1));
                                    } else {
                                        tri_opposite_flag = true;
                                        opposite_combined_clasp_compensate_flag = true;
                                        distance = getDistanceOppoToothTri(
                                                tooth, sorted_zone1.get(0), sorted_zone1.get(sorted_zone1.size()-1));
                                    }
                                }
                            }
                        } else if (mouth.getMaxillary().getKennedyType() == KennedyType.KENNEDY_TYPE_IV) {
                            canine_weight = 0.3;
                            premolar_4_weight = 0.202;
                            premolar_5_weight = 0.201;
                            distomolar_6_weight = 0.102;
                            distomolar_7_weight = 0.101;

                            KIV_commbied_clasp_penalty_flag = true;
                            EdentulousSpace edentulous_space = mouth.getMaxillary().getEdentulousSpaces().get(0);
                            Tooth left_most = edentulous_space.getLeftMost();
                            Tooth right_most = edentulous_space.getRightMost();
                            int left_diatal_target;
                            int right_distal_target;
                            if (left_most.getNum() >= 3) {
                                left_diatal_target = 7;
                            } else {
                                left_diatal_target = 5;
                            }
                            if (right_most.getNum() >= 3) {
                                right_distal_target = 7;
                            } else {
                                right_distal_target = 5;
                            }

                            int mesial_distance = 0;
                            int distal_distance = 0;
                            Map<String, Object> info = plan.getNearestEdentulous(tooth);
                            mesial_distance = (Integer) info.get("distance");
                            if (tooth.getZone() == 1) {
                                distal_distance = abs(tooth.getNum() - left_diatal_target);
                            } else {
                                distal_distance = abs(tooth.getNum() - right_distal_target);
                            }
                            distance = Math.min(mesial_distance, distal_distance);

                        } else {
                            canine_weight = 0.3;
                            premolar_4_weight = 0.202;
                            premolar_5_weight = 0.201;
                            distomolar_6_weight = 0.102;
                            distomolar_7_weight = 0.101;
                            Map<String, Object> info = plan.getNearestEdentulous(tooth);
                            distance = (Integer) info.get("distance");
                        }
                    }

//					Map<String, Object> info = plan.getNearestEdentulous(tooth);
//					distance = (Integer) info.get("distance");

                    if (tooth.getToothType() == ToothType.Canine) {
                        score += distance + canine_weight;
                    } else if (tooth.getNum() == 4) {
                        score += distance + premolar_4_weight;
                    } else if (tooth.getNum() == 5) {
                        score += distance + premolar_5_weight;
                    } else if (tooth.getNum() == 6) {
                        score += distance + distomolar_6_weight;
                    } else {
                        score += distance + distomolar_7_weight;
                    }
                }

                // 惩罚对侧布局，提高对侧布局正确优先级
                if (rec_opposite_flag) {
                    score += 10;
                }
                if (tri_opposite_flag) {
                    score += 10;
                }
                // 惩罚单侧缺失对侧布局基牙过多方案
                if (num_no_missing_zone_abutment > 2) {
                    score += 100;
                }
                for (Component component : plan.getComponents()) {
                    if (component.getClass() == CombinedClasp.class || component.getClass() == EmbrasureClasp.class) {
                        if (plan.getPosition() == Position.Mandibular) {
                            if (mouth.getMandibular().isZoneNoMissing(component.getToothPos().get(0).getZone())) {
                                if (opposite_combined_clasp_compensate_flag) {
                                    score -= 0.1;
                                }
                            } else if (KIV_commbied_clasp_penalty_flag) {
                                score += 10;
                            } else {
                                score -= 10;
                            }
                        }
                        else {
                            if (mouth.getMaxillary().isZoneNoMissing(component.getToothPos().get(0).getZone())) {
                                if (opposite_combined_clasp_compensate_flag) {
                                    score -= 0.1;
                                }
                            } else if (KIV_commbied_clasp_penalty_flag) {
                                score += 10;
                            } else {
                                score -= 10;
                            }
                        }
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
