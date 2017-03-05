package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class ReverseHookClasp extends Clasp {

    private ClaspArm buccal_arm = null;
    private ClaspArm lingual_arm = null;
    private OcclusalRest occlusal_rest = null;

    public ReverseHookClasp(Tooth tooth_pos, Position tip_direction, ClaspMaterial buccal_material,
                     ClaspMaterial lingual_material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, buccal_material);
        this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, lingual_material);
        if (tip_direction.equals(Position.Distal)) {
            this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
        }
        else if (tip_direction.equals(Position.Mesial)) {
            this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
        }
    }

    public ReverseHookClasp(Tooth tooth_pos, Position tip_direction, ClaspMaterial material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, material);
        this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, material);
        if (tip_direction.equals(Position.Distal)) {
            this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
        }
        else if (tip_direction.equals(Position.Mesial)) {
            this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
        }
    }

    public ReverseHookClasp(Tooth tooth_pos, Position tip_direction) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, tip_direction, Position.Buccal, ClaspMaterial.Cast);
        this.lingual_arm = new ClaspArm(tooth_pos, tip_direction, Position.Lingual, ClaspMaterial.Cast);
        if (tip_direction.equals(Position.Distal)) {
            this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);
        }
        else if (tip_direction.equals(Position.Mesial)) {
            this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Distal);
        }
    }


    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }

    public Position getTipDirection() {

        if (this.buccal_arm != null) {
            return this.buccal_arm.getTipDirection();
        }
        else if (this.lingual_arm != null) {
            return this.lingual_arm.getTipDirection();
        }
        else {
            System.out.println("There is no clasp arm!");
            return null;
        }
    }

    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("倒钩（ReverseHook）卡环，");
        if(this.getTipDirection().equals(Position.Mesial))
            s.append("卡环臂尖朝向近中");
        else if(this.getTipDirection().equals(Position.Distal))
            s.append("卡环臂尖朝向远中");
        else {}
        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
