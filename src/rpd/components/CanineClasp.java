package rpd.components;

import exceptions.rpd.ComponentException;
import rpd.RPDPlan;
import rpd.conceptions.ClaspMaterial;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

public class CanineClasp extends Clasp {


    private ClaspArm arm = null;

    private IncisalRest rest = null;

    public CanineClasp(Tooth tooth_pos, ClaspMaterial material) {

        super(tooth_pos);
        this.arm = new ClaspArm(tooth_pos, Position.Distal, Position.Buccal, material);
        this.rest = new IncisalRest(tooth_pos, Position.Mesial);
    }

    public CanineClasp(Tooth tooth_pos) {

        super(tooth_pos);
        this.arm = new ClaspArm(tooth_pos, Position.Distal, Position.Buccal, ClaspMaterial.WW);
        this.rest = new IncisalRest(tooth_pos, Position.Mesial);
    }

    public Position getTipDirection() {

        return Position.Distal;
    }

    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }

    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("尖牙(Canine)卡环");

        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
