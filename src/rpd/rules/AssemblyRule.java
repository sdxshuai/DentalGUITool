package rpd.rules;

import java.util.ArrayList;
import java.util.List;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.CircumferencialClaspAssembly;
import rpd.components.ClaspAssembly;
import rpd.components.RPAAssembly;
import rpd.components.RPIAssembly;
import rpd.components.RingClaspAssembly;
import rpd.components.WWClaspAssembly;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则2
public class AssemblyRule {

	public static List<AssemblyRule> assembly_rules = null;
	
	private static Mouth mouth = null;
	
	public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
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
		assembly_rules = new ArrayList<AssemblyRule>();
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "后侧游离缺失且末端基牙为后牙：末端基牙放置RPI";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 1;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.PosteriorExtension)) {
					Tooth abutment = null;
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor == null && right_neighbor != null)
						abutment = right_neighbor;
					else if(left_neighbor != null && right_neighbor == null)
						abutment = left_neighbor;
					else
						throw new RuleException("no abutment tooth found");
					
					RPDPlan new_plan = new RPDPlan(rpd_plan);
					int abutment_num = abutment.getNum();
					if(abutment_num >= 4) {
						ClaspAssembly clasp_assembly = new RPIAssembly(abutment, EdentulousType.PosteriorExtension);
						clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						return null;
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "后侧游离缺失且末端基牙为后牙：末端基牙放置RPA";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 2;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.PosteriorExtension)) {
					Tooth abutment = null;
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor == null && right_neighbor != null)
						abutment = right_neighbor;
					else if(left_neighbor != null && right_neighbor == null)
						abutment = left_neighbor;
					else
						throw new RuleException("no abutment tooth found");
					
					int abutment_num = abutment.getNum();
					if(abutment_num >= 4) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						ClaspAssembly clasp_assembly = new RPAAssembly(abutment, EdentulousType.PosteriorExtension);
						clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					return null;
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "后侧游离缺失且末端基牙为后牙：末端基牙放置圆形卡环和远中合支托";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 3;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ClaspAssemblyException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.PosteriorExtension)) {
					Tooth abutment = null;
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor == null && right_neighbor != null)
						abutment = right_neighbor;
					else if(left_neighbor != null && right_neighbor == null)
						abutment = left_neighbor;
					else
						throw new RuleException("no abutment tooth found");
					
					int abutment_num = abutment.getNum();
					if(abutment_num >= 4) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						ClaspAssembly clasp_assembly = new CircumferencialClaspAssembly(abutment, Position.Mesial);
						clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						return null;
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "后侧游离缺失且末端基牙为尖牙：尖牙放置弯制卡环和舌支托";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 4;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.PosteriorExtension)) {
					Tooth abutment = null;
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor == null && right_neighbor != null)
						abutment = right_neighbor;
					else if(left_neighbor != null && right_neighbor == null)
						abutment = left_neighbor;
					else
						throw new RuleException("no abutment tooth found");
					
					int abutment_num = abutment.getNum();
					if(abutment_num == 3) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						ClaspAssembly clasp_assembly = new WWClaspAssembly(abutment, Position.Mesial, false);
						clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						return null;
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "后侧游离缺失且末端基牙为切牙：末端基牙放置弯制卡环并且舌板覆盖";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 5;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.PosteriorExtension)) {
					Tooth abutment = null;
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor == null && right_neighbor != null)
						abutment = right_neighbor;
					else if(left_neighbor != null && right_neighbor == null)
						abutment = left_neighbor;
					else
						throw new RuleException("no abutment tooth found");
					
					int abutment_num = abutment.getNum();
					if(abutment_num <= 2) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						ClaspAssembly clasp_assembly = new WWClaspAssembly(abutment, Position.Mesial, true);
						clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						return null;
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "前侧游离缺失：游离侧末端基牙放置RPI，非游离侧末端基牙放置弯制卡环和舌支托";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 6;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ToothPosException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.AnteriorExtension)) {
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor != null && right_neighbor != null) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						int left_neighbor_num = left_neighbor.getNum();
						int right_neighbor_num = right_neighbor.getNum();
						ClaspAssembly left_clasp_assembly = null;
						ClaspAssembly right_clasp_assembly = null;
						if(left_neighbor_num >= 4)
							left_clasp_assembly = new RPIAssembly(left_neighbor, EdentulousType.AnteriorExtension);
						else
							left_clasp_assembly = new WWClaspAssembly(mouth.getTooth(4, 3), Position.Mesial, false);
						if(right_neighbor_num >= 4)
							right_clasp_assembly = new RPIAssembly(right_neighbor, EdentulousType.AnteriorExtension);
						else
							right_clasp_assembly = new WWClaspAssembly(mouth.getTooth(3, 3), Position.Mesial, false);
						left_clasp_assembly.addToPlan(new_plan);
						right_clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						throw new RuleException("no abutment tooth found");
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "前侧游离缺失：游离侧末端基牙放置RPA，非游离侧末端基牙放置弯制卡环和舌支托";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 7;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ToothPosException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.AnteriorExtension)) {
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor != null && right_neighbor != null) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						int left_neighbor_num = left_neighbor.getNum();
						int right_neighbor_num = right_neighbor.getNum();
						ClaspAssembly left_clasp_assembly = null;
						ClaspAssembly right_clasp_assembly = null;
						if(left_neighbor_num >= 4)
							left_clasp_assembly = new RPAAssembly(left_neighbor, EdentulousType.AnteriorExtension);
						else
							left_clasp_assembly = new WWClaspAssembly(mouth.getTooth(4, 3), Position.Mesial, false);
						if(right_neighbor_num >= 4)
							right_clasp_assembly = new RPAAssembly(right_neighbor, EdentulousType.AnteriorExtension);
						else
							right_clasp_assembly = new WWClaspAssembly(mouth.getTooth(3, 3), Position.Mesial, false);
						left_clasp_assembly.addToPlan(new_plan);
						right_clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						throw new RuleException("no abutment tooth found");
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "前侧游离缺失：游离侧末端基牙放置圆形卡环和合支托，非游离侧末端基牙放置弯制卡环和舌支托";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 8;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ToothPosException, ClaspAssemblyException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.AnteriorExtension)) {
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor != null && right_neighbor != null) {
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						int left_neighbor_num = left_neighbor.getNum();
						int right_neighbor_num = right_neighbor.getNum();
						ClaspAssembly left_clasp_assembly = null;
						ClaspAssembly right_clasp_assembly = null;
						if(left_neighbor_num >= 4)
							left_clasp_assembly = new CircumferencialClaspAssembly(left_neighbor, Position.Distal);
						else
							left_clasp_assembly = new WWClaspAssembly(mouth.getTooth(4, 3), Position.Mesial, false);
						if(right_neighbor_num >= 4)
							right_clasp_assembly = new CircumferencialClaspAssembly(right_neighbor, Position.Distal);
						else
							right_clasp_assembly = new WWClaspAssembly(mouth.getTooth(3, 3), Position.Mesial, false);
						left_clasp_assembly.addToPlan(new_plan);
						right_clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						throw new RuleException("no abutment tooth found");
				}
				else
					return null;
			}
		});
		
		assembly_rules.add(new AssemblyRule() {
			
			public String getExplaination() {
				return "牙支持式：缺隙两端基牙放置弯制卡环或圈形卡环，合支托或舌支托";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 9;
			}
			
			public RPDPlan apply(EdentulousSpace edentulous_space, RPDPlan rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
				if(edentulous_space.getEdentulousType().equals(EdentulousType.ToothSupport)) {
					Tooth left_neighbor = edentulous_space.getLeftNeighbor();
					Tooth right_neighbor = edentulous_space.getRightNeighbor();
					if(left_neighbor != null && right_neighbor != null) {
						
						RPDPlan new_plan = new RPDPlan(rpd_plan);
						int left_neighbor_zone = left_neighbor.getZone();
						int right_neighbor_zone = right_neighbor.getZone();
						int left_neighbor_num = left_neighbor.getNum();
						int right_neighbor_num = right_neighbor.getNum();
						ClaspAssembly left_clasp_assembly = null;
						ClaspAssembly right_clasp_assembly = null;
						if(left_neighbor_num == 8)
							left_clasp_assembly = new RingClaspAssembly(left_neighbor, Position.Buccal);
						else if(left_neighbor_num <= 3)
							left_clasp_assembly = new WWClaspAssembly(mouth.getTooth(left_neighbor_zone, 3), Position.Mesial, false);
						else
							if(left_neighbor_zone == 4 || left_neighbor_zone == 1)
								left_clasp_assembly = new CircumferencialClaspAssembly(left_neighbor, Position.Distal);
							else if(left_neighbor_zone == 2 || left_neighbor_zone == 3)
								left_clasp_assembly = new CircumferencialClaspAssembly(left_neighbor, Position.Mesial);
							else {}
						if(right_neighbor_num == 8)
							right_clasp_assembly = new RingClaspAssembly(right_neighbor, Position.Buccal);
						else if(right_neighbor_num <= 3)
							right_clasp_assembly = new WWClaspAssembly(mouth.getTooth(right_neighbor_zone, 3), Position.Mesial, false);
						else
							if(right_neighbor_zone == 4 || right_neighbor_zone == 1)
								right_clasp_assembly = new CircumferencialClaspAssembly(right_neighbor, Position.Mesial);
							else if(right_neighbor_zone == 2 || right_neighbor_zone == 3)
								right_clasp_assembly = new CircumferencialClaspAssembly(right_neighbor, Position.Distal);
							else {}
						left_clasp_assembly.addToPlan(new_plan);
						right_clasp_assembly.addToPlan(new_plan);
						return new_plan;
					}
					else
						throw new RuleException("no abutment tooth found");
				}
				else
					return null;
			}
		});
	}
	
}
