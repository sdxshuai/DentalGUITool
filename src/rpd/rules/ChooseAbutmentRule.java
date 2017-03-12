package rpd.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.CircumferencialClaspAssembly;
import rpd.components.ClaspAssembly;
import rpd.components.RPAAssembly;
import rpd.components.RPIAssembly;
import rpd.components.RingClaspAssembly;
import rpd.components.WWClaspAssembly;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mandibular;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则2
public class ChooseAbutmentRule {

    public static List<ChooseAbutmentRule> choose_abutment_rules = null;

    private static Mouth mouth = null;

    public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException, ClaspAssemblyException, ToothPosException {
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

            public void getCrossCombination(List<Tooth> existing_teeth, int start, int end, int length,
                                            HashSet<Tooth> hashSet, ArrayList<ArrayList<Tooth>> res_list) {

                if(length ==0){
                    ArrayList<Tooth> res = new ArrayList<Tooth>();
                    res.addAll(hashSet);
                    res_list.add(res);
                    return;
                }

                for(int i=start;i<=end-length;i++){

                    hashSet.add(existing_teeth.get(i));
                    getCrossCombination(existing_teeth, i+1, end, length-1, hashSet, res_list);
                    hashSet.remove((existing_teeth.get(i)));
                }
            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {

                List<RPDPlan> res = new ArrayList<>();
                List<RPDPlan> res_buffer = new ArrayList<>();
                for (RPDPlan plan:rpd_plans) {
                    if (plan.getPosition() == Position.Mandibular) {
                        List<Tooth> existing_teeth = new ArrayList<Tooth>(plan.getMouth().getMandibular().getExistingTeeth());
                        int list_size = existing_teeth.size();
                        HashSet<Tooth> hashSet = new HashSet<Tooth>();
                        ArrayList<ArrayList<Tooth>> abutment_teeth_list = new ArrayList<ArrayList<Tooth>>();
                        getCrossCombination(existing_teeth, 0, list_size, 2, hashSet, abutment_teeth_list);
                        getCrossCombination(existing_teeth, 0, list_size, 3, hashSet, abutment_teeth_list);
                        getCrossCombination(existing_teeth, 0, list_size, 4, hashSet, abutment_teeth_list);

                        for (ArrayList<Tooth> abutment_teeth:abutment_teeth_list) {
                            RPDPlan new_plan = new RPDPlan(plan);
                            new_plan.addAbutmentTeeth(abutment_teeth);
                            res.add(new_plan);
//                            res_buffer.add(new_plan);
//                            res.addAll(res_buffer);
//                            res_buffer.clear();
                        }
                    }
                    else if (plan.getPosition() == Position.Maxillary) {
                        List<Tooth> existing_teeth = new ArrayList<Tooth>(plan.getAbutmentTeeth());
                        int list_size = existing_teeth.size();
                        HashSet<Tooth> hashSet = new HashSet<Tooth>();
                        ArrayList<ArrayList<Tooth>> abutment_teeth_list = new ArrayList<ArrayList<Tooth>>();
                        getCrossCombination(existing_teeth, 0, list_size, 2, hashSet, abutment_teeth_list);
                        getCrossCombination(existing_teeth, 0, list_size, 3, hashSet, abutment_teeth_list);
                        getCrossCombination(existing_teeth, 0, list_size, 4, hashSet, abutment_teeth_list);

                        for (ArrayList<Tooth> abutment_teeth:abutment_teeth_list) {
                            RPDPlan new_plan = new RPDPlan(plan);
                            new_plan.addAbutmentTeeth(abutment_teeth);
                            res.add(new_plan);
                        }
                    }
                    else {
                        throw new RuleException("rpd has no position");
                    }
                }

                return res;
            }
        });

        choose_abutment_rules.add(new ChooseAbutmentRule() {

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
