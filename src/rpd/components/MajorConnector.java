package rpd.components;

/**
 * Created by sdxshuai on 2017/4/23.
 */

import rpd.conceptions.Position;
import rpd.oral.Tooth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class MajorConnector implements Component {
	protected ArrayList<Tooth> tooth_pos = null;
	protected HashSet<Tooth> lingual_confrontation = null;
	protected Position mandibular_or_maxillary = null;

	protected String rule_explanation = null;

	public MajorConnector() {
	}

	public MajorConnector(ArrayList<Tooth> tooth_pos) {
		this.tooth_pos = tooth_pos;
	}

	public MajorConnector(Tooth tooth_pos) {

		this.tooth_pos = new ArrayList<Tooth>();
		this.tooth_pos.add(tooth_pos);
	}

	public ArrayList<Tooth> getToothPos() {
		return this.tooth_pos;
	}

	public void addLingualConfrontation(Tooth tooth) {
		this.lingual_confrontation.add(tooth);
	}

	public void addLingualConfrontation(ArrayList<Tooth> tooth_list) {
		this.lingual_confrontation.addAll(tooth_list);
	}

	public HashSet<Tooth> getLingualConfrontation() {
		return lingual_confrontation;
	}

	@Override
	public boolean isRest() {
		return false;
	}

	@Override
	public boolean isClasp() {
		return false;
	}

	@Override
	public boolean isGuidingPlate() {
		return false;
	}

	@Override
	public boolean isPlating() {
		return false;
	}

	public boolean isIndirectRetainer() {
		return false;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Tooth tooth : this.tooth_pos) {
			s.append(tooth.toString());
		}
		s.append(": ");
		return s.toString();
	}
}
