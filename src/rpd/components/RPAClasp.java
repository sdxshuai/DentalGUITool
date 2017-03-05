package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

/**
 * Created by sdxshuai on 2017/3/5.
 */
public class RPAClasp extends Clasp {

    private OcclusalRest occlusal_rest = null;

    public RPAClasp(Tooth tooth_pos) {

        super(tooth_pos);
        this.occlusal_rest = new OcclusalRest(tooth_pos, Position.Mesial);

    }


    @Override
    public void addToPlan(RPDPlan rpd_plan) {
        rpd_plan.addComponent(this);
    }

    public Position getTipDirection() {

        return Position.Distal;
    }

    public String print() {

        StringBuilder s = new StringBuilder();
        s.append(this.tooth_pos.toString() + ":");
        s.append("RPA卡环，");
        s.append("卡环臂尖朝向远中");
        return s.toString();
    }

    public String toString()  {
        return this.print();
    }
}
