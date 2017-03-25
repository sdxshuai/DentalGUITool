package rpd.rules;

import java.util.*;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.*;
import rpd.conceptions.*;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mandibular;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则2
public class ClaspRule {

    public static List<ClaspRule> clasp_rules = null;

    private static Mouth mouth = null;

    public List<RPDPlan> apply(List<RPDPlan> rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
        throw new RuleException("call from abstract class");
    }

    public int getRuleNum() throws RuleException {
        throw new RuleException("call from abstract class");
    }

    public String getExplaination() throws RuleException {
        throw new RuleException("call from abstract class");
    }

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

            //遍历只有一个continuous_tooth的情况，生成with_multi_list
            public void getPlansFromWithMultiList(ArrayList<ArrayList<Tooth>> continuous_tooth_list,
                                                  ArrayList<Tooth> abutment_teeth,
                                                  List<RPDPlan> plans) {

                for (ArrayList<Tooth> continuous_tooth:continuous_tooth_list) {
                    ArrayList<ArrayList<Tooth>> with_multi_list = new ArrayList<ArrayList<Tooth>>();;
                    Set<Tooth> differ_set = new HashSet<>();
                    differ_set.addAll(abutment_teeth);
                    differ_set.removeAll(continuous_tooth);
                    with_multi_list.add(continuous_tooth);
                    for (Tooth differ_tooth:differ_set) {
                        ArrayList<Tooth> current_list = new ArrayList<Tooth>();
                        current_list.add(differ_tooth);
                        with_multi_list.add(current_list);
                    }
                    getPlans(with_multi_list, plans);
                }
            }

            public void getPlans(ArrayList<ArrayList<Tooth>> with_multi_list, List<RPDPlan> plans) {

            }

            public void getPlans(List<Tooth> no_multi_list, List<RPDPlan> plans) {

            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
                List<RPDPlan> res = new ArrayList<>();
                for (RPDPlan plan:rpd_plans) {
                    ArrayList<Tooth> abutment_teeth = new ArrayList<>();
                    abutment_teeth.addAll(plan.getAbutmentTeeth());
                    Collections.sort(abutment_teeth);
                    List<Tooth> no_multi_list = new ArrayList<>(abutment_teeth);
                    ArrayList<ArrayList<Tooth>> continuous_tooth_list = new ArrayList<ArrayList<Tooth>>(); //连续牙位列表
                    Set<Tooth> continuous_tooth_set = new HashSet<>(); //连续牙位位置集合

                    int last_num = 0;
                    Tooth last_tooth = null;
                    for (Tooth tooth:abutment_teeth) {
                        int tooth_num = Integer.parseInt(tooth.toString().substring(5));
                        if (tooth_num - last_num == 1 || last_num - tooth_num == 1) {
                            ArrayList<Tooth> current_tooth = new ArrayList<Tooth>();
                            current_tooth.add(last_tooth);
                            current_tooth.add(tooth);
                            continuous_tooth_list.add(current_tooth);
                            continuous_tooth_set.addAll(current_tooth);
                        }
                        last_num = tooth_num;
                        last_tooth = tooth;
                    }


                    if (continuous_tooth_set.size() == 4) {
                        for (int i=1;i<=2;i++) {
                            if (i == 1) {
                                //只有一个连续牙位
                                getPlansFromWithMultiList(continuous_tooth_list, abutment_teeth, res);
                            }
                            else {
                                //有两个连续牙位
                                ArrayList<ArrayList<Tooth>> with_multi_list = new ArrayList<ArrayList<Tooth>>();
                                with_multi_list.add(continuous_tooth_list.get(0));
                                with_multi_list.add(continuous_tooth_list.get(continuous_tooth_list.size()-1));
                                getPlans(with_multi_list, res);
                            }
                        }

                    }
                    else {
                        getPlansFromWithMultiList(continuous_tooth_list, abutment_teeth, res);
                    }

                    getPlans(no_multi_list, res);

                }
                return res;
            }
        });

        clasp_rules.add(new ClaspRule() {

            public String getExplaination() {
                return "占位符";
            }

            public String toString() {
                return this.getExplaination();
            }

            public int getRuleNum() {
                return 2;
            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
                List<RPDPlan> res = new ArrayList<>();
                res.addAll(rpd_plans);
                return res;
            }
        });
    }
}
