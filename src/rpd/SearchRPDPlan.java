package rpd;

import java.util.ArrayList;
import java.util.List;
import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mandibular;
import rpd.oral.Maxillary;
import rpd.oral.Mouth;
import rpd.rules.*;

//生成设计方案
public class SearchRPDPlan {

    public static boolean noMissing(Mandibular mandibular) throws ToothPosException {

        boolean no_missing = true;
        for(int num = 1; num <= 7; num++) {
            no_missing = no_missing && (!mandibular.getTooth(3, num).isMissing());
            no_missing = no_missing && (!mandibular.getTooth(4, num).isMissing());
        }
        return no_missing;
    }

    public static boolean noMissing(Maxillary maxillary) throws ToothPosException {

        boolean no_missing = true;
        for(int num = 1; num <= 7; num++) {
            no_missing = no_missing && (!maxillary.getTooth(1, num).isMissing());
            no_missing = no_missing && (!maxillary.getTooth(2, num).isMissing());
        }
        return no_missing;
    }

    public static List<RPDPlan> searchMandibular(Mouth mouth) throws RuleException, ClaspAssemblyException, ToothPosException, EdentulousTypeException {

        Mandibular mandibular = mouth.getMandibular();

        if(noMissing(mandibular))
            return null;

//        EdentulousTypeRule.initRules();
//        AssemblyRule.initRules(mouth);
//        AddtionalAssemblyRule.initRules(mouth);
//        IndirectRetainerRule.initRules(mouth);
//        PlatingRule.initRules(mouth);
//        RemovingRule.initRules(mouth);

        ChooseAbutmentRule.initRules(mouth);

        List<RPDPlan> res = new ArrayList<RPDPlan>();
        RPDPlan empty_plan = new RPDPlan(mouth, Position.Mandibular);

        List<RPDPlan> abutment_teeth_plans = new ArrayList<RPDPlan>();
        List<RPDPlan> abutment_teeth_plans_buffer = new ArrayList<RPDPlan>();
        abutment_teeth_plans.add(empty_plan);
        boolean all_space_restored = true;

        for(RPDPlan old_plan : abutment_teeth_plans) {
            boolean plan_changed = false;
            for(ChooseAbutmentRule rule: ChooseAbutmentRule.choose_abutment_rules) {
                RPDPlan plan = rule.apply(old_plan);

                if(plan != null) {
                    abutment_teeth_plans_buffer.add(plan);
                    plan_changed = true;
                    //System.out.println(plan.toString());
                }
            }
            if(!plan_changed) {
                abutment_teeth_plans_buffer.add(old_plan);
                all_space_restored = false;
            }
        }
        abutment_teeth_plans.clear();
        abutment_teeth_plans.addAll(abutment_teeth_plans_buffer);
        abutment_teeth_plans_buffer.clear();
        if(!all_space_restored) {
            abutment_teeth_plans.clear();
            return abutment_teeth_plans;
        }

//        List<RPDPlan> addtional_assembly_plans = new ArrayList<RPDPlan>();
//        for(RPDPlan old_plan : abutment_teeth_plans) {
//
//            boolean plan_changed = false;
//            for(AddtionalAssemblyRule rule : AddtionalAssemblyRule.addtional_assembly_rules) {
//                RPDPlan plan = rule.apply(old_plan);
//                if(plan != null) {
//                    addtional_assembly_plans.add(plan);
//                    plan_changed = true;
//                }
//            }
//            if(!plan_changed)
//                addtional_assembly_plans.add(old_plan);
//        }


        res.addAll(abutment_teeth_plans);

        if(res.size() == 1) {
            RPDPlan plan = res.get(0);
            if(plan.isEmptyPlan())
                res.remove(0);
        }

        return res;
    }

}
