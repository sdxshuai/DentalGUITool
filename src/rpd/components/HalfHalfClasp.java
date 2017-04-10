package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class HalfHalfClasp extends Clasp {
    private ClaspArm buccal_arm = null;
    private ClaspArm lingual_arm = null;
    private OcclusalRest distal_rest = null;
    private OcclusalRest mesial_rest = null;

    public HalfHalfClasp(Tooth tooth_pos, ClaspMaterial buccal_material,
                     ClaspMaterial lingual_material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, buccal_material);
        this.lingual_arm = new ClaspArm(tooth_pos, Position.Distal, Position.Lingual, lingual_material);
        this.distal_rest = new OcclusalRest(tooth_pos, Position.Distal);
        this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
    }

    public HalfHalfClasp(Tooth tooth_pos,  ClaspMaterial material) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, material);
        this.lingual_arm = new ClaspArm(tooth_pos, Position.Distal, Position.Lingual, material);
        this.distal_rest = new OcclusalRest(tooth_pos, Position.Distal);
        this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
    }

    public HalfHalfClasp(Tooth tooth_pos) {

        super(tooth_pos);
        this.buccal_arm = new ClaspArm(tooth_pos, Position.Mesial, Position.Buccal, ClaspMaterial.Cast);
        this.lingual_arm = new ClaspArm(tooth_pos, Position.Distal, Position.Lingual, ClaspMaterial.Cast);
        this.distal_rest = new OcclusalRest(tooth_pos, Position.Distal);
        this.mesial_rest = new OcclusalRest(tooth_pos, Position.Mesial);
    }


    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }

    public boolean isIndirectRetainer() {
    	if (this.distal_rest != null) {
    	    return this.distal_rest.isIndirectRetainer();
        }
        else if (this.mesial_rest != null) {
    	    return this.mesial_rest.isIndirectRetainer();
        }
        else {
    	    System.out.println("No rest!");
    	    return false;
        }
	}

	public ClaspMaterial getMaterial() {
		if (this.buccal_arm != null) {
			return this.buccal_arm.getClaspMaterial();
		}
		else if (this.lingual_arm != null) {
			return this.lingual_arm.getClaspMaterial();
		}
		else {
			System.out.println("There is no clasp arm!");
			return null;
		}
	}

    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("对半（Half and Half）卡环");

        if(this.getMaterial().equals(ClaspMaterial.WW))
			s.append("弯制材料");
		else if(this.getMaterial().equals(ClaspMaterial.Cast))
			s.append("铸造材料");
		else {}

        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
