package rpd.rules;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import org.apache.poi.ss.formula.functions.T;
import rpd.RPDPlan;
import rpd.components.*;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.KennedyType;
import rpd.conceptions.Position;
import rpd.conceptions.ToothPosition;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//规则2
public class MajorConnectorRule {

	public static List<MajorConnectorRule> major_connector_rules = null;

	private static Mouth mouth = null;

	public static void initRules(Mouth mouth_obj) {

		mouth = mouth_obj;
		major_connector_rules = new ArrayList<MajorConnectorRule>();

		major_connector_rules.add(new MajorConnectorRule() {

			public String getExplaination() {
				return "生成大连接体方案";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 1;
			}

			public MajorConnector chooseMajorConnector(RPDPlan plan, StringBuilder explanation) {
				MajorConnector majorConnector = null;
				if (plan.getPosition() == Position.Mandibular) {
					majorConnector = chooseMajorConnectorOnMandibular(plan, explanation);
				} else {
					majorConnector = chooseMajorConnectorOnMaxillary(plan, explanation);
				}
				return majorConnector;
			}

			public MajorConnector chooseMajorConnectorOnMandibular(RPDPlan plan, StringBuilder explanation) {
				explanation.append("下颌大连接体：");
				if (isBadCondition(plan.getAbutmentTeeth())) {
					explanation.append("有口底浅或前牙舌侧倾斜或骨突情况，选择舌板大连接体\n");
					return new LingualPlateConnector(plan.getAbutmentTeeth(), mouth.getMandibular());
				}
				else if (isMissingTooMuchOnMandibular()) {
					explanation.append("双侧缺失大于等于7颗，或后牙双侧缺失大于等于5颗，选择舌板大连接体\n");
					return new LingualPlateConnector(plan.getAbutmentTeeth(), mouth.getMandibular());
				}
				else {
					explanation.append("无特殊情况，选择舌杆大连接体\n");
					return new LingualBarConnector(plan.getAbutmentTeeth(), mouth.getMandibular());
				}
			}

//			public MajorConnector chooseMajorConnectorOnMaxillary(RPDPlan plan, StringBuilder explanation) {
//				explanation.append("上颌大连接体：");
//				if (isMissingTooMuchOnMaxillary()) {
//					explanation.append("大部分缺失，选择上颌全腭板大连接体\n");
//					return new FullPalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
//				} else {
//					KennedyType kennedyType = getKennedyType(mouth, plan.getPosition());
//					if (kennedyType == KennedyType.KENNEDY_TYPE_I) {
//						explanation.append("Kennedy I型可摘局部义齿，选择上颌腭板大连接体\n");
//						return new PalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
//					} else if (kennedyType == KennedyType.KENNEDY_TYPE_II) {
//						explanation.append("Kennedy II型可摘局部义齿，选择上颌变异腭板大连接体\n");
//						return new ModifiedPalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
//					} else if (kennedyType == KennedyType.KENNEDY_TYPE_III) {
//						explanation.append("Kennedy III型牙支持式可摘局部义齿，选择上颌颚带大连接体\n");
//						return new SinglePalatalStrapConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
//					} else {
//						explanation.append("Kennedy IV型或Kennedy III型非牙支持式可摘局部义齿，选择上颌前后颚带大连接体\n");
//						return new CombinationAnteriorPosteriorPalatalStrapConnector(
//								plan.getAbutmentTeeth(), mouth.getMaxillary());
//					}
//				}
//			}

			public MajorConnector chooseMajorConnectorOnMaxillary(RPDPlan plan, StringBuilder explanation) {
				explanation.append("上颌大连接体：");
				if (isMissingTooMuchOnMaxillary()) {
					explanation.append("大部分缺失，选择上颌全腭板大连接体\n");
					return new FullPalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
				} else {
					KennedyType kennedyType = mouth.getMaxillary().getKennedyType();
					if (kennedyType == KennedyType.KENNEDY_TYPE_I) {
						explanation.append("Kennedy I型可摘局部义齿，选择上颌腭板大连接体\n");
						return new PalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
					} else if (kennedyType == KennedyType.KENNEDY_TYPE_II) {
						explanation.append("Kennedy II型可摘局部义齿，选择上颌变异腭板大连接体\n");
						return new ModifiedPalatalPlateConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
					} else if (kennedyType == KennedyType.KENNEDY_TYPE_III) {
						explanation.append("Kennedy III型牙支持式可摘局部义齿，选择上颌颚带大连接体\n");
						return new SinglePalatalStrapConnector(plan.getAbutmentTeeth(), mouth.getMaxillary());
					} else {
						explanation.append("Kennedy IV型或Kennedy III型非牙支持式可摘局部义齿，选择上颌前后颚带大连接体\n");
						return new CombinationAnteriorPosteriorPalatalStrapConnector(
								plan.getAbutmentTeeth(), mouth.getMaxillary());
					}
				}
			}

			public boolean isBadCondition(Set<Tooth> abutment_teeth) {
				boolean flag = false;
				for (Tooth tooth : abutment_teeth) {
					if (tooth.getToothPosition() == ToothPosition.Lingual
							|| tooth.isTorus()
							|| tooth.isSpaceBelowGingivalMargins()) {
						flag = true;
						break;
					}
				}
				return flag;
			}

			public boolean isMissingTooMuchOnMandibular() {
				List<Tooth> missingTeeth = mouth.getMandibular().getMissingTeeth();
				if (missingTeeth.size() >= 6) {
					return true;
				}

				int countBackteeth = 0;
				for (Tooth tooth:missingTeeth) {
					if (tooth.getNum() >= 4) {
						countBackteeth++;
					}
				}
				if (countBackteeth >= 5) {
					return true;
				}

				return false;
			}

			public boolean isMissingTooMuchOnMaxillary() {
				List<Tooth> missingTeeth = mouth.getMaxillary().getMissingTeeth();
				if (missingTeeth.size() >= 7) {
					return true;
				}

				int countLeftMissing = 0;
				int countRightMissing = 0;
				for (Tooth tooth:missingTeeth) {
					if (tooth.getZone() == 1) {
						countLeftMissing++;
					}
					else if (tooth.getZone() == 2) {
						countRightMissing++;
					}
				}

				if (countLeftMissing >=5 || countRightMissing >=5) {
					return true;
				}

				return false;
			}

			public boolean isDislocateWithMoreThanFiveMissing(Mouth mouth, Position maxillary_or_mandibular) {
				boolean flag_left = true;
				boolean flag_right = true;
				if (maxillary_or_mandibular == Position.Mandibular) {
					for (int numTooth = 8; numTooth >= 3; numTooth--) {
						if (!mouth.getMandibular().getTooth(3, numTooth).isMissing()) {
							flag_right = false;
							break;
						}
					}
					for (int numTooth = 8; numTooth >= 3; numTooth--) {
						if (!mouth.getMandibular().getTooth(4, numTooth).isMissing()) {
							flag_left = false;
							break;
						}
					}
				} else {
					for (int numTooth = 8; numTooth >= 3; numTooth--) {
						if (!mouth.getMaxillary().getTooth(1, numTooth).isMissing()) {
							flag_left = false;
							break;
						}
					}
					for (int numTooth = 8; numTooth >= 3; numTooth--) {
						if (!mouth.getMaxillary().getTooth(2, numTooth).isMissing()) {
							flag_right = false;
							break;
						}
					}
				}
				return flag_left || flag_right;
			}

			public boolean isOneSideMissing(Mouth mouth, Position position) {
				if (position == Position.Mandibular) {
					return mouth.getMandibular().isOneSideMissing();
				}
				else {
					return mouth.getMaxillary().isOneSideMissing();
				}
			}

            public boolean needMajorConnector(Position planPosition) {
				boolean flag = false;
				List<EdentulousSpace> edentulousSpaceList;
				List<Tooth> missingTeeth;
				boolean isMissingFrontTeeth;
				if (planPosition == Position.Mandibular) {
					edentulousSpaceList = mouth.getMandibular().getEdentulousSpaces();
					isMissingFrontTeeth = mouth.getMandibular().isMissingFrontTeeth();
					missingTeeth = mouth.getMandibular().getMissingTeeth();
				}
				else {
					edentulousSpaceList = mouth.getMaxillary().getEdentulousSpaces();
					isMissingFrontTeeth = mouth.getMaxillary().isMissingFrontTeeth();
					missingTeeth = mouth.getMaxillary().getMissingTeeth();
				}

				if (isMissingFrontTeeth) {
					return true;
				}

				//缺隙必须为非游离缺失
				for (EdentulousSpace edentulousSpace:edentulousSpaceList) {
					if (edentulousSpace.getEdentulousType() == EdentulousType.PosteriorExtension) {
			    		return true;
					}
				}

				if (isOneSideMissing(mouth, planPosition)) {
					if (missingTeeth.size() == 2) {
						if (missingTeeth.get(0).getNum() - missingTeeth.get(1).getNum() > 2) {
							return true;
						}
					}
				}
				else {
					int countLeft = 0;
					int countRight = 0;
					for (Tooth tooth:missingTeeth) {
						if (tooth.getZone() == 1 || tooth.getZone() == 4) {
							countLeft++;
							if (countLeft > 1) {
								return true;
							}
						}
						else {
							countRight++;
							if (countRight > 1) {
								return true;
							}
						}
					}
				}
				return flag;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				if (rpd_plans == null || rpd_plans.size() == 0) {
					return null;
				}

				List<RPDPlan> res = new ArrayList<>();

				//后牙非游离缺失，且前牙不缺失，且每侧最多缺一颗时，不用大连接体(遍历缺隙，判断缺隙大小是否都为1，是否都为游离缺失)
				Position planPosition = rpd_plans.get(0).getPosition();

				if (needMajorConnector(planPosition)) {
					for (RPDPlan plan : rpd_plans) {

						StringBuilder explanation = new StringBuilder();
						MajorConnector majorConnector = chooseMajorConnector(plan, explanation);
						if (majorConnector != null) {
							plan.addComponent(majorConnector);
							HashSet<Tooth> lingualConfrontation = majorConnector.getLingualConfrontation();
							if (lingualConfrontation != null) {
								for (Tooth tooth : lingualConfrontation) {
									if (tooth.getNum() == 3) {
										for (Component component : plan.getComponents()) {
											int comZone = component.getToothPos().get(0).getZone();
											int comNum = component.getToothPos().get(0).getNum();
											if (comZone == tooth.getZone() && comNum == tooth.getNum()) {
												if (component.getClass().getName().equals("rpd.components.LingualRest")) {
													plan.removeComponent(component);
												}
												break;
											}
										}
									}
								}
							}
							plan.appendPlanExplanation(explanation.toString());
						}
					}
				}

				res.addAll(rpd_plans);
				return res;
			}
		});


	}

	public List<RPDPlan> apply(List<RPDPlan> rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
}
