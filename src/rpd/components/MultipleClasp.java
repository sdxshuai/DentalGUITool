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
public class MultipleClasp extends Clasp {

    private ClaspArm buccal_mesial_arm = null;
    private ClaspArm lingual_distal_arm = null;
    private ClaspArm lingual_mesial_arm = null;
    private OcclusalRest distal_rest = null;
    private OcclusalRest mesial_rest = null;

    public MultipleClasp(ArrayList<Tooth> tooth_pos) {

        super(tooth_pos);
        Collections.sort(tooth_pos);
        Tooth mesial_tooth = tooth_pos.get(0);
        Tooth distal_tooth = tooth_pos.get(1);

        this.buccal_mesial_arm = new ClaspArm(mesial_tooth, Position.Distal, Position.Buccal, ClaspMaterial.Cast);
        this.lingual_distal_arm = new ClaspArm(distal_tooth, Position.Mesial, Position.Lingual, ClaspMaterial.Cast);
        this.lingual_mesial_arm = new ClaspArm(mesial_tooth, Position.Distal, Position.Lingual, ClaspMaterial.Cast);

        this.distal_rest = new OcclusalRest(distal_tooth, Position.Distal);
        this.mesial_rest = new OcclusalRest(mesial_tooth, Position.Mesial);
    }

    public MultipleClasp(ArrayList<Tooth> tooth_pos, ClaspMaterial material) {

        super(tooth_pos);
        Collections.sort(tooth_pos);
        Tooth mesial_tooth = tooth_pos.get(0);
        Tooth distal_tooth = tooth_pos.get(1);

        this.buccal_mesial_arm = new ClaspArm(mesial_tooth, Position.Distal, Position.Buccal, material);
        this.lingual_distal_arm = new ClaspArm(distal_tooth, Position.Mesial, Position.Lingual, material);
        this.lingual_mesial_arm = new ClaspArm(mesial_tooth, Position.Distal, Position.Lingual, material);

        this.distal_rest = new OcclusalRest(distal_tooth, Position.Distal);
        this.mesial_rest = new OcclusalRest(mesial_tooth, Position.Mesial);
    }

    public MultipleClasp(ArrayList<Tooth> tooth_pos, ClaspMaterial buccal_material, ClaspMaterial mesial_material) {

        super(tooth_pos);
        Collections.sort(tooth_pos);
        Tooth mesial_tooth = tooth_pos.get(0);
        Tooth distal_tooth = tooth_pos.get(1);

        this.buccal_mesial_arm = new ClaspArm(mesial_tooth, Position.Distal, Position.Buccal, buccal_material);
        this.lingual_distal_arm = new ClaspArm(distal_tooth, Position.Mesial, Position.Lingual, mesial_material);
        this.lingual_mesial_arm = new ClaspArm(mesial_tooth, Position.Distal, Position.Lingual, mesial_material);

        this.distal_rest = new OcclusalRest(distal_tooth, Position.Distal);
        this.mesial_rest = new OcclusalRest(mesial_tooth, Position.Mesial);
    }

    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }


    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("复合（Multiple）卡环，");

        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
