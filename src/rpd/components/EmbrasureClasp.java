package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class EmbrasureClasp extends Clasp {

    private ClaspArm buccal_distal_arm = null;
    private ClaspArm buccal_mesial_arm = null;
    private ClaspArm lingual_distal_arm = null;
    private ClaspArm lingual_mesial_arm = null;
    private OcclusalRest distal_rest = null;
    private OcclusalRest mesial_rest = null;

    public EmbrasureClasp(ArrayList<Tooth> tooth_pos) {

        super(tooth_pos);
        Collections.sort(tooth_pos);
        Tooth mesial_tooth = tooth_pos.get(0);
        Tooth distal_tooth = tooth_pos.get(1);

        this.buccal_distal_arm = new ClaspArm(distal_tooth, Position.Distal, Position.Buccal, ClaspMaterial.WW);
        this.buccal_mesial_arm = new ClaspArm(mesial_tooth, Position.Mesial, Position.Buccal, ClaspMaterial.WW);
        this.lingual_distal_arm = new ClaspArm(distal_tooth, Position.Distal, Position.Lingual, ClaspMaterial.WW);
        this.lingual_mesial_arm = new ClaspArm(mesial_tooth, Position.Mesial, Position.Lingual, ClaspMaterial.WW);

        this.distal_rest = new OcclusalRest(distal_tooth, Position.Mesial);
        this.mesial_rest = new OcclusalRest(mesial_tooth, Position.Distal);
    }

    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }


    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("间隙（Embrasure）卡环，");

        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
