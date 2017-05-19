package rpd.components;

import rpd.RPDPlan;
import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.HashSet;
import java.util.Set;

public class LingualRest extends Rest {

	public LingualRest(Tooth tooth_pos) {
		this.type = Position.Lingual;
		this.tooth_pos = tooth_pos;
	}

	public static void main(String[] args) {

		Tooth tooth1 = new Tooth(1, 1);
		LingualRest r1 = new LingualRest(tooth1);
		LingualRest r2 = new LingualRest(tooth1);
		Set<LingualRest> test = new HashSet<LingualRest>();
		test.add(r1);
		test.add(r2);
		System.out.println(r1.equals(r2));
	}

	public int hashCode() {
		return this.type.hashCode() + this.tooth_pos.hashCode();
	}

	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;

		LingualRest lingual_rest = (LingualRest) obj;
		if (this.tooth_pos == lingual_rest.tooth_pos &&
				this.type == lingual_rest.type)

			return true;
		else
			return false;
	}

	@Override
	public void addToPlan(RPDPlan rpd_plan) {
		rpd_plan.addComponent(this);
	}

	@Override
	public String print() {

		StringBuilder s = new StringBuilder();
		s.append(this.tooth_pos.toString() + ":");
		s.append("舌支托");
		return s.toString();
	}

	public String toString() {
		return this.print();
	}
}
