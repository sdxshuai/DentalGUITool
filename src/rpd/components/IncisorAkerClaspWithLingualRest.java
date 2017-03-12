package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/12.
 */
public class IncisorAkerClaspWithLingualRest extends Clasp {

    private ClaspArm buccal_arm = null;
    private LingualRest lingual_rest = null;

    public IncisorAkerClaspWithLingualRest(Tooth tooth_pos, ClaspMaterial material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, material);
        this.lingual_rest = new LingualRest(tooth_pos);
    }

    public IncisorAkerClaspWithLingualRest(Tooth tooth_pos) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, ClaspMaterial.Cast);
        this.lingual_rest = new LingualRest(tooth_pos);
    }


    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }

    public Position getTipDirection() {

        return Position.Mesial;
    }

    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("切牙上Aker卡环，舌支托，");
        s.append("卡环臂尖朝向近中");

        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
