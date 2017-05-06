package rpd.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.*;
import rpd.conceptions.KennedyType;
import rpd.conceptions.Position;
import rpd.conceptions.ToothPosition;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则2
public class MajorConnectorRule {

	public static List<MajorConnectorRule> major_connector_rules = null;

	private static Mouth mouth = null;

	public List<RPDPlan> apply(List<RPDPlan> rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public static void initRules(Mouth mouth_obj) {

		mouth = mouth_obj;
		major_connector_rules = new ArrayList<MajorConnectorRule>();

		major_connector_rules.add(new MajorConnectorRule() {

			public String getExplaination() {
				return "初始化生成大连接体方案";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 1;
			}

			public MajorConnector chooseMajorConnector(RPDPlan plan) {
				MajorConnector majorConnector = null;
				if (plan.getPosition() == Position.Mandibular) {
					majorConnector = chooseMajorConnectorOnMandibular(plan);
				}
				else {
					majorConnector = chooseMajorConnectorOnMaxillary(plan);
				}
				return majorConnector;
			}

			public MajorConnector chooseMajorConnectorOnMandibular(RPDPlan plan) {
				if (isBadCondition(plan.getAbutmentTeeth())) {
					return new LingualPlateConnector(plan.getAbutmentTeeth());
				}
				else {
					return new LingualBarConnector(plan.getAbutmentTeeth());
				}
			}

			public MajorConnector chooseMajorConnectorOnMaxillary(RPDPlan plan) {
				if (isDislocateWithMoreThanFiveMissing(mouth, plan.getPosition())) {
					return new FullPalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
				}
				else {
					KennedyType kennedyType = getKennedyType(mouth, plan.getPosition());
					if (kennedyType == KennedyType.KENNEDY_TYPE_I) {
						return new PalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
					}
					else if (kennedyType == KennedyType.KENNEDY_TYPE_II) {
						return new ModifiedPalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
					}
					else if (kennedyType == KennedyType.KENNEDY_TYPE_III
							&& isToothSupportPlan(mouth.getMaxillary().getEdentulousSpaces(), plan.getAbutmentTeeth())) {
						return new SinglePalatalStrapConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
					}
					else {
						return new CombinationAnteriorPosteriorPalatalStrapConnector(
								plan.getAbutmentTeeth(), mouth.getMaxillary());
					}
				}
			}

			public boolean isBadCondition(Set<Tooth> abutment_teeth) {
				boolean flag = false;
				for (Tooth tooth:abutment_teeth) {
					if (tooth.getToothPosition() == ToothPosition.Lingual
							|| tooth.isTorus()
							|| tooth.isSpaceBelowGingivalMargins()) {
						flag = true;
						break;
					}
				}
				return flag;
			}

			public boolean isToothSupportPlan(List<EdentulousSpace> edentulousSpaces, Set<Tooth> abutment_teeth) {
				return false;
			}

			public boolean isDislocateWithMoreThanFiveMissing(Mouth mouth, Position maxillary_or_mandibular) {
				boolean flag_left = true;
				boolean flag_right = true;
				if (maxillary_or_mandibular == Position.Mandibular) {
					for (int numTooth=8; numTooth>=3; numTooth--){
						if (!mouth.getMandibular().getTooth(3, numTooth).isMissing()) {
							flag_right = false;
							break;
						}
					}
					for (int numTooth=8; numTooth>=3; numTooth--){
						if (!mouth.getMandibular().getTooth(4, numTooth).isMissing()) {
							flag_left = false;
							break;
						}
					}
				}
				else {
					for (int numTooth=8; numTooth>=3; numTooth--) {
						if (!mouth.getMaxillary().getTooth(1, numTooth).isMissing()) {
							flag_left = false;
							break;
						}
					}
					for (int numTooth=8; numTooth>=3; numTooth--) {
						if (!mouth.getMaxillary().getTooth(2, numTooth).isMissing()) {
							flag_right = false;
							break;
						}
					}
				}
				return flag_left || flag_right;
			}

			public KennedyType getKennedyType(Mouth mouth, Position maxillary_or_mandibular) {
				if (isLeftDislocate(mouth, maxillary_or_mandibular)
						&& isRightDislocate(mouth, maxillary_or_mandibular)) {
					return KennedyType.KENNEDY_TYPE_I;
				}
				else if ((isLeftDislocate(mouth, maxillary_or_mandibular)
						&& !isRightDislocate(mouth, maxillary_or_mandibular))
						|| (!isLeftDislocate(mouth, maxillary_or_mandibular)
						&& isRightDislocate(mouth, maxillary_or_mandibular))) {
					return KennedyType.KENNEDY_TYPE_II;
				}
				else {
					if (maxillary_or_mandibular == Position.Mandibular) {
						List<EdentulousSpace> edentulousSpaces = mouth.getMandibular().getEdentulousSpaces();
						if (edentulousSpaces.size() == 1) {
							EdentulousSpace edentulousSpace = edentulousSpaces.get(0);
							Tooth left_neighbour = edentulousSpace.getLeftNeighbor();
							Tooth right_neighbour = edentulousSpace.getRightNeighbor();
							if (left_neighbour.getZone() != right_neighbour.getZone()) {
								return KennedyType.KENNEDY_TYPE_IV;
							}
							else {
								return KennedyType.KENNEDY_TYPE_III;
							}
						}
						else {
							return KennedyType.KENNEDY_TYPE_III;
						}
					}
					else {
						List<EdentulousSpace> edentulousSpaces = mouth.getMaxillary().getEdentulousSpaces();
						if (edentulousSpaces.size() == 1) {
							EdentulousSpace edentulousSpace = edentulousSpaces.get(0);
							Tooth left_neighbour = edentulousSpace.getLeftNeighbor();
							Tooth right_neighbour = edentulousSpace.getRightNeighbor();
							if (left_neighbour.getZone() != right_neighbour.getZone()) {
								return KennedyType.KENNEDY_TYPE_IV;
							}
							else {
								return KennedyType.KENNEDY_TYPE_III;
							}
						}
						else {
							return KennedyType.KENNEDY_TYPE_III;
						}
					}
				}
			}


			public boolean isLeftDislocate(Mouth mouth, Position maxillary_or_mandibular) {
				boolean flag = false;
				if (maxillary_or_mandibular == Position.Mandibular) {
					if (mouth.getMandibular().getTooth(4, 7).isMissing()
							&& mouth.getMandibular().getTooth(4, 8).isMissing()) {
						flag = true;
					}
				}
				else {
					if (mouth.getMaxillary().getTooth(1, 7).isMissing()
							&& mouth.getMaxillary().getTooth(1, 8).isMissing()) {
						flag = true;
					}
				}
				return flag;
			}

			public boolean isRightDislocate(Mouth mouth, Position maxillary_or_mandibular) {
				boolean flag = false;
				if (maxillary_or_mandibular == Position.Mandibular) {
					if (mouth.getMandibular().getTooth(3, 7).isMissing()
							&& mouth.getMandibular().getTooth(3, 8).isMissing()) {
						flag = true;
					}
				}
				else {
					if (mouth.getMaxillary().getTooth(2, 7).isMissing()
							&& mouth.getMaxillary().getTooth(2, 8).isMissing()) {
						flag = true;
					}
				}
				return flag;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				for (RPDPlan plan:rpd_plans) {
					MajorConnector majorConnector = chooseMajorConnector(plan);
					if (majorConnector != null) {
						plan.addComponent(majorConnector);
					}
				}

				res.addAll(rpd_plans);
				return res;
			}
		});
	}
}
