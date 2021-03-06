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

	public boolean needIndirectRetainer(RPDPlan rpd_plan, EdentulousSpace edentulous_space, Mouth mouth)
			throws EdentulousTypeException {
		boolean res = true;
		int edentulousZone = 0;
		if (edentulous_space.getLeftMost().getNum() == 7) {
			edentulousZone = edentulous_space.getLeftMost().getZone();
		}
		else {
			edentulousZone = edentulous_space.getRightMost().getZone();
		}


		if (edentulousZone == 1 || edentulousZone == 4) {
			if (this.left_indirect_retainer.getToothPos().get(0).getNum() > 3) {
				//判断同侧是否有间接固位体
				for (Component component:rpd_plan.getComponents()) {
					if (component.getToothPos().get(0).getZone() == edentulousZone) {
						if (this.canBePosteriorIndirectRetainer(component)) {
							return false;
						}
					}
				}
				//如果同侧没有，且同侧3,4位置missing，无法放置支托，判断对侧是否有间接固位体
				if (
						(edentulousZone == 1
								&& mouth.getMaxillary().getTooth(1, 3).isMissing()
								&& mouth.getMaxillary().getTooth(1, 4).isMissing())
						|| (edentulousZone == 4
								&& mouth.getMandibular().getTooth(4, 3).isMissing()
								&& mouth.getMandibular().getTooth(4, 4).isMissing())) {
					for (Component component : rpd_plan.getComponents()) {
						if (component.getToothPos().get(0).getZone() != edentulousZone) {
							if (this.canBePosteriorIndirectRetainer(component)) {
								return false;
							}
						}
					}
				}
			}
			else {
				for (Component component:rpd_plan.getComponents()) {
					if (this.canBePosteriorIndirectRetainer(component)) {
						return false;
					}
				}
			}
		}
		else if (edentulousZone == 2 || edentulousZone == 3) {
			if (this.right_indirect_retainer.getToothPos().get(0).getNum() > 3) {
				for (Component component:rpd_plan.getComponents()) {
					if (component.getToothPos().get(0).getZone() == edentulousZone) {
						if (this.canBePosteriorIndirectRetainer(component)) {
							return false;
						}
					}
				}
				if (
						(edentulousZone == 2
								&& mouth.getMaxillary().getTooth(2, 3).isMissing()
								&& mouth.getMaxillary().getTooth(2, 4).isMissing())
						|| (edentulousZone == 3
								&& mouth.getMandibular().getTooth(3, 3).isMissing()
								&& mouth.getMandibular().getTooth(3, 4).isMissing())) {
					for (Component component : rpd_plan.getComponents()) {
						if (component.getToothPos().get(0).getZone() != edentulousZone) {
							if (this.canBePosteriorIndirectRetainer(component)) {
								return false;
							}
						}
					}
				}
			}
			else {
				for (Component component:rpd_plan.getComponents()) {
					if (this.canBePosteriorIndirectRetainer(component)) {
						return false;
					}
				}
			}
		}
		return res;
	}

	public Component properIndirectRetainet(Mouth mouth, RPDPlan plan) throws ToothPosException {

		int zone = -1, oppisite_zone = -1;
		EdentulousType edentulous_type = edentulous_space.getEdentulousType();
		if (edentulous_type.equals(EdentulousType.PosteriorExtension)) {
//			int left_num = left_indirect_retainer.getToothPos().get(0).getNum();
//			int right_num = right_indirect_retainer.getToothPos().get(0).getNum();

			if (edentulous_space.getLeftMost().getNum() == 7) {
				zone = edentulous_space.getLeftMost().getZone();
			}
			else {
				zone = edentulous_space.getRightMost().getZone();
			}

			if (zone == 1 || zone == 3) {
				oppisite_zone = zone + 1;
			}
			else {
				oppisite_zone = zone - 1;
			}

			if (zone == 1 || zone == 4) {
				if (!mouth.getTooth(zone, 4).isMissing()
						&& this.left_indirect_retainer.getToothPos().get(0).getNum() > 4
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(zone, 4))) {
					return new OcclusalRest(mouth.getTooth(zone, 4), Position.Mesial);
				}
				else if (!mouth.getTooth(zone, 3).isMissing()
						&& this.left_indirect_retainer.getToothPos().get(0).getNum() > 3
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(zone, 3))) {
					return new LingualRest(mouth.getTooth(zone, 3));
				}
				else if (!mouth.getTooth(oppisite_zone, 4).isMissing()
						&& this.right_indirect_retainer.getToothPos().get(0).getNum() > 4
						&& this.left_indirect_retainer.getToothPos().get(0).getNum() > 4
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(oppisite_zone, 4))) {
					return new OcclusalRest(mouth.getTooth(oppisite_zone, 4), Position.Mesial);
				}
				else if (!mouth.getTooth(oppisite_zone, 3).isMissing()
						&& this.right_indirect_retainer.getToothPos().get(0).getNum() > 3
						&& this.left_indirect_retainer.getToothPos().get(0).getNum() > 3
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(oppisite_zone, 3))) {
					return new LingualRest(mouth.getTooth(oppisite_zone, 3));
				}
			}
			else if (zone == 2 || zone == 3) {
				if (!mouth.getTooth(zone, 4).isMissing()
						&& this.right_indirect_retainer.getToothPos().get(0).getNum() > 4
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(zone, 4))) {
					return new OcclusalRest(mouth.getTooth(zone, 4), Position.Mesial);
				}
				else if (!mouth.getTooth(zone, 3).isMissing()
						&& this.right_indirect_retainer.getToothPos().get(0).getNum() > 3
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(zone, 3))) {
					return new LingualRest(mouth.getTooth(zone, 3));
				}
				else if (!mouth.getTooth(oppisite_zone, 4).isMissing()
						&& this.left_indirect_retainer.getToothPos().get(0).getNum() > 4
						&& this.right_indirect_retainer.getToothPos().get(0).getNum() > 4
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(oppisite_zone, 4))) {
					return new OcclusalRest(mouth.getTooth(oppisite_zone, 4), Position.Mesial);
				}
				else if (!mouth.getTooth(oppisite_zone, 3).isMissing()
						&& this.left_indirect_retainer.getToothPos().get(0).getNum() > 3
						&& this.right_indirect_retainer.getToothPos().get(0).getNum() >3
						&& !plan.getAbutmentTeeth().contains(mouth.getTooth(oppisite_zone, 3))) {
					return new LingualRest(mouth.getTooth(oppisite_zone, 3));
				}
			}
			else {
				return null;
			}
		} else {
			return null;
		}
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

		if (component_zone == 2 || component_zone == 3) {
			return (component_num < axis_right_num && component_num <= 4);
		}
		else if (component_zone == 1 || component_zone == 4) {
			return (component_num < axis_left_num && component_num <= 4);
		}
		else {
			return false;
		}
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
