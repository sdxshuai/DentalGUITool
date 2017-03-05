package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class BackActionClasp extends Clasp {

    private ClaspArm buccal_arm = null;
    private ClaspArm lingual_arm = null;
    private OcclusalRest occlusal_rest = null;

    public BackActionClasp(Tooth tooth_pos, ClaspMaterial buccal_material, ClaspMaterial lingual_material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, buccal_material);
        this.lingual_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Lingual, lingual_material);
        this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
    }

    public BackActionClasp(Tooth tooth_pos, ClaspMaterial material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, material);
        this.lingual_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Lingual, material);
        this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
    }

    public BackActionClasp(Tooth tooth_pos) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, ClaspMaterial.Cast);
        this.lingual_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Lingual, ClaspMaterial.Cast);
        this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
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
        s.append("回力（Back Action）卡环");

        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
