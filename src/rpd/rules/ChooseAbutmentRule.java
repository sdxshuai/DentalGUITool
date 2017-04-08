package rpd.rules;

import java.util.*;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import org.apache.jena.rdf.model.RDFNode;
import rpd.RPDPlan;
import rpd.components.CircumferencialClaspAssembly;
import rpd.components.ClaspAssembly;
import rpd.components.RPAAssembly;
import rpd.components.RPIAssembly;
import rpd.components.RingClaspAssembly;
import rpd.components.WWClaspAssembly;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.conceptions.ToothType;
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

    public boolean isAllDistomolar(Set<Tooth> abutment_teeth) {
        boolean flag = true;
        int tooth_num = 0;
        for (Tooth tooth:abutment_teeth) {
            tooth_num = tooth.getNum();
            if (tooth_num!=6&&tooth_num!=7&&tooth_num!=8) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public boolean isAllPremolar(Set<Tooth> abutment_teeth) {
        boolean flag = true;
        int tooth_num = 0;
        for (Tooth tooth:abutment_teeth) {
            tooth_num = tooth.getNum();
            if (tooth_num!=4&&tooth_num!=5) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public boolean isAllCanine(Set<Tooth> abutment_teeth) {
        boolean flag = true;
        int tooth_num = 0;
        for (Tooth tooth:abutment_teeth) {
            tooth_num = tooth.getNum();
            if (tooth_num!=3) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public boolean isAllIncisor(Set<Tooth> abutment_teeth) {
        boolean flag = true;
        int tooth_num = 0;
        for (Tooth tooth:abutment_teeth) {
            tooth_num = tooth.getNum();
            if (tooth_num!=1&&tooth_num!=2) {
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
        }
        else if (position == Position.Maxillary) {
            if (mouth.getMaxillary().isZone1AllMissing() || mouth.getMaxillary().isZone2AllMissing()) {
                flag = true;
            }
        }
        return flag;
    }

    public boolean isSingleZone(Set<Tooth> abutment_teeth) {
        boolean flag = true;
        int tooth_zone = 0;
        int pre_tooth_zone = 0;
        for (Tooth tooth:abutment_teeth) {
            pre_tooth_zone = tooth_zone;
            tooth_zone = tooth.getZone();
            if (pre_tooth_zone!=tooth_zone&&pre_tooth_zone!=0) {
                flag = false;
                break;
            }
        }
        return flag;
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

    public int getMaxSuccessiveAbutmentNum(RPDPlan plan){
        List<Tooth> abutment_teeth = new ArrayList<>(plan.getAbutmentTeeth());
        Collections.sort(abutment_teeth);
        int maxSuc = 1;
        int tempSuc = 1;

        for (int i = 1;i < abutment_teeth.size();i++) {
            if (abutment_teeth.get(i).getZone() == abutment_teeth.get(i-1).getZone()
                    && abutment_teeth.get(i).getNum() - 1 == abutment_teeth.get(i-1).getNum()) {
                tempSuc++;
            }
            else {
                if (tempSuc > maxSuc) {
                    maxSuc = tempSuc;
                }
                tempSuc = 1;
            }
        }
        return maxSuc;
    }

    public boolean isDistanceGreaterThanThres(RPDPlan plan, List<EdentulousSpace> edentulousSpaceList) {
        int distance = 0;
        int cur_distance = 0;
        boolean flag = false;
        //true = 所有基牙距离缺失区超过3个牙位
        for (Tooth tooth:plan.getAbutmentTeeth()) {
            distance = 0;
            for (EdentulousSpace edentulous_space : edentulousSpaceList) {
                cur_distance = getDistanceBetweenAbutmentEdentulous(tooth, edentulous_space);
                if (cur_distance < distance || distance == 0) {
                    distance = cur_distance;
                }
            }
            if (distance >= 4) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    public boolean isDisociate(List<EdentulousSpace> edentulousSpaceList) {
        boolean flag = false;
        for (EdentulousSpace edentulousSpace:edentulousSpaceList){
            if (edentulousSpace.getEdentulousType()==EdentulousType.PosteriorExtension) {
                flag = true;
                break;
            }
        }
        return flag;
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

                for (RPDPlan plan:rpd_plans) {
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

                return res;
            }
        });

        choose_abutment_rules.add(new ChooseAbutmentRule() {

            public String getExplaination() {
                return "如果不是一侧全部缺牙，不能全部在同一个zone";
            }

            public String toString() {
                return this.getExplaination();
            }

            public int getRuleNum() {
                return 2;
            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
                List<RPDPlan> res = new ArrayList<>();
                if (isOneSideAllMissing(mouth_obj, rpd_plans.get(0).getPosition())) {
                    res.addAll(rpd_plans);
                }
                else {
                    for (RPDPlan plan:rpd_plans) {
                        if(!isSingleZone(plan.getAbutmentTeeth())) {
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
                return 2;
            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
                List<RPDPlan> res = new ArrayList<>();
                for (RPDPlan plan:rpd_plans) {
                    if (!isDistanceGreaterThanThres(plan, plan.getEdentulousSpaces())) {
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
                return 2;
            }

            public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
                List<RPDPlan> res = new ArrayList<>();
                for (RPDPlan plan:rpd_plans) {
                    if (getMaxSuccessiveAbutmentNum(plan) <= 2) {
                        res.add(plan);
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
