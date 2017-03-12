package rpd.rules;

import java.util.ArrayList;
import java.util.List;

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

    public RPDPlan apply(RPDPlan rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
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

            public RPDPlan apply(RPDPlan rpd_plan) throws RuleException {
                if (rpd_plan.getPosition() == Position.Mandibular) {
                    RPDPlan new_plan = new RPDPlan(rpd_plan);
                    return new_plan;
                }
                else if (rpd_plan.getPosition() == Position.Maxillary) {
                    RPDPlan new_plan = new RPDPlan(rpd_plan);
                    return new_plan;
                }
                else {
                    throw new RuleException("Plan has no position");
                }
            }
        });

        choose_abutment_rules.add(new ChooseAbutmentRule() {

            public String getExplaination() {
                return "";
            }

            public String toString() {
                return this.getExplaination();
            }

            public int getRuleNum() {
                return 2;
            }

            public RPDPlan apply(RPDPlan rpd_plan) throws RuleException {
                if (rpd_plan.getPosition() == Position.Mandibular) {
                    RPDPlan new_plan = new RPDPlan(rpd_plan);
                    return new_plan;
                }
                else if (rpd_plan.getPosition() == Position.Maxillary) {
                    RPDPlan new_plan = new RPDPlan(rpd_plan);
                    return new_plan;
                }
                else {
                    throw new RuleException("Plan has no position");
                }
            }
        });
    }
}
