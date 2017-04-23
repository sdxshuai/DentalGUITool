package rpd.components;

import exceptions.rpd.EdentulousTypeException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;

public class RotationAxis {

	private Component left_indirect_retainer = null;
	private Component right_indirect_retainer = null;

	private EdentulousSpace edentulous_space = null;

	public RotationAxis(Component left_indirect_retainer, Component right_indirect_retainer, EdentulousSpace edentulous_space) {
		this.left_indirect_retainer = left_indirect_retainer;
		this.right_indirect_retainer = right_indirect_retainer;
		this.edentulous_space = edentulous_space;
	}

	public boolean needIndirectRetainer(RPDPlan rpd_plan, EdentulousSpace edentulous_space) throws EdentulousTypeException {
		boolean res = true;
		for (Component component : rpd_plan.getComponents())
			if (this.canBeIndirectRetainer(component))
				res = false;
		return res;
	}

	public Component properIndirectRetainet(Mouth mouth, Position mandibular_or_maxillary) throws ToothPosException {

		int zone = -1, oppisite_zone = -1;
		EdentulousType edentulous_type = edentulous_space.getEdentulousType();
		if (edentulous_type.equals(EdentulousType.PosteriorExtension)) {

			int left_num = left_indirect_retainer.getToothPos().get(0).getNum();
			int right_num = right_indirect_retainer.getToothPos().get(0).getNum();
			if (left_num < right_num) {
				if (mandibular_or_maxillary.equals(Position.Mandibular)) {
					zone = 3;
					oppisite_zone = 4;
				} else if (mandibular_or_maxillary.equals(Position.Maxillary)) {
					zone = 2;
					oppisite_zone = 1;
				} else {
				}
			} else if (left_num >= right_num) {
				if (mandibular_or_maxillary.equals(Position.Mandibular)) {
					zone = 4;
					oppisite_zone = 3;
				} else if (mandibular_or_maxillary.equals(Position.Maxillary)) {
					zone = 1;
					oppisite_zone = 2;
				} else {
				}
			} else {
			}

			if (!mouth.getTooth(zone, 4).isMissing())
				return new OcclusalRest(mouth.getTooth(zone, 4), Position.Mesial);
			else if (!mouth.getTooth(zone, 3).isMissing())
				return new LingualRest(mouth.getTooth(zone, 3));
			else if (!mouth.getTooth(oppisite_zone, 3).isMissing())
				return new LingualRest(mouth.getTooth(oppisite_zone, 3));
			else if (!mouth.getTooth(oppisite_zone, 4).isMissing())
				return new OcclusalRest(mouth.getTooth(oppisite_zone, 4), Position.Mesial);
			else
				return null;
		} else
			return null;
	}

	public boolean canBeIndirectRetainer(Component component) throws EdentulousTypeException {

		if (!component.isIndirectRetainer())
			return false;

		if (edentulous_space.getEdentulousType().equals(EdentulousType.PosteriorExtension))
			return canBePosteriorIndirectRetainer(component);
		else if (edentulous_space.getEdentulousType().equals(EdentulousType.AnteriorExtension))
			return canBeAnteriorIndirectRetainer(component);
		else
			throw new EdentulousTypeException("not an extension area");
	}

	public boolean canBePosteriorIndirectRetainer(Component component) {

		int axis_left_num = left_indirect_retainer.getToothPos().get(0).getNum();
		int axis_right_num = right_indirect_retainer.getToothPos().get(0).getNum();

		int component_zone = component.getToothPos().get(0).getZone();
		int component_num = component.getToothPos().get(0).getNum();

		if (axis_left_num == axis_right_num)
			if (component_num < axis_left_num && component_num <= 4)
				return true;
			else
				return false;
		else if (axis_left_num < axis_right_num)
			if ((component_zone == 2 || component_zone == 3) && component_num < axis_right_num && component_num <= 4)
				return true;
			else
				return false;
		else if ((component_zone == 1 || component_zone == 4) && component_num < axis_left_num && component_num <= 4)
			return true;
		else
			return false;
	}

	public boolean canBeAnteriorIndirectRetainer(Component component) {

		int axis_left_num = left_indirect_retainer.getToothPos().get(0).getNum();
		int axis_right_num = right_indirect_retainer.getToothPos().get(0).getNum();

		int component_zone = component.getToothPos().get(0).getZone();
		int component_num = component.getToothPos().get(0).getNum();

		if (component_zone == 4 || component_zone == 1)
			if (component_num > axis_left_num && component_num >= 4)
				return true;
			else
				return false;
		else if (component_num > axis_right_num && component_num >= 4)
			return true;
		else
			return false;
	}
}
