package rpd.rules;

import java.util.ArrayList;
import java.util.List;

import exceptions.rpd.RuleException;
import rpd.conceptions.EdentulousType;
import rpd.oral.EdentulousSpace;
import rpd.oral.Tooth;

//规则1
public abstract class EdentulousTypeRule {
	
	public static void main(String[] args) throws RuleException {
		
		if(EdentulousTypeRule.edentulous_type_rules == null)
			EdentulousTypeRule.initRules();
		
		for(EdentulousTypeRule edentulous_type_rule : EdentulousTypeRule.edentulous_type_rules)
			System.out.println(edentulous_type_rule.getExplaination());
	}
	
	public static List<EdentulousTypeRule> edentulous_type_rules = null;
	
	public EdentulousType apply(EdentulousSpace edentulous_space) throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}
	
	public static void initRules() {
		
		edentulous_type_rules = new ArrayList<EdentulousTypeRule>();
		
		edentulous_type_rules.add(new EdentulousTypeRule() {
			
			public String getExplaination() {
				return "缺隙未过中线，两端均有天然牙：牙支持式";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 1;
			}
			
			public EdentulousType apply(EdentulousSpace edentulous_space) {
				
				Tooth left_neighbor = edentulous_space.getLeftNeighbor();
				Tooth right_neighbor = edentulous_space.getRightNeighbor();
				if(left_neighbor != null && right_neighbor != null) {
					int left_neighbor_zone = left_neighbor.getZone();
					int right_neignbor_zone = right_neighbor.getZone();
					if(left_neighbor_zone == right_neignbor_zone)
						return EdentulousType.ToothSupport;
					else
						return null;
				}
				return null;
			}
		});
		
		edentulous_type_rules.add(new EdentulousTypeRule() {
			
			public String getExplaination() {
				return "缺隙过中线，两侧尖牙均在：牙支持式";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 2;
			}
			
			public EdentulousType apply(EdentulousSpace edentulous_space) {
				
				Tooth left_neighbor = edentulous_space.getLeftNeighbor();
				Tooth right_neignbor = edentulous_space.getRightNeighbor();
				if(left_neighbor != null && right_neignbor != null) {
					int left_neighbor_zone = left_neighbor.getZone();
					int right_neignbor_zone = right_neignbor.getZone();
					int left_neighbor_num = left_neighbor.getNum();
					int right_neignbor_num = right_neignbor.getNum();
					if((left_neighbor_zone != right_neignbor_zone) && 
						left_neighbor_num <= 3 && right_neignbor_num <= 3)
						return EdentulousType.ToothSupport;
					else
						return null;
				}
				return null;
			}
		});
		
		edentulous_type_rules.add(new EdentulousTypeRule() {
			
			public String getExplaination() {
				return "缺隙未过中线，远端无天然牙，近端尖牙后侧有天然牙：后端游离";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 3;
			}
			
			public EdentulousType apply(EdentulousSpace edentulous_space) {
				
				Tooth left_neighbor = edentulous_space.getLeftNeighbor();
				Tooth right_neighbor = edentulous_space.getRightNeighbor();
				if(left_neighbor == null && right_neighbor != null) {
					int right_neignbor_zone = right_neighbor.getZone();
					int right_neignbor_num = right_neighbor.getNum();
					if((right_neignbor_zone == 4 || right_neignbor_zone == 1) && right_neignbor_num >= 3)
						return EdentulousType.PosteriorExtension;
					else
						return null;
				}
				else if(left_neighbor != null && right_neighbor == null) {
					int left_neighbor_zone = left_neighbor.getZone();
					int left_neighbor_num = left_neighbor.getNum();
					if((left_neighbor_zone == 2 || left_neighbor_zone == 3) && left_neighbor_num >= 3)
						return EdentulousType.PosteriorExtension;
					else
						return null;
				}
				return null;
			}
		});
		
		edentulous_type_rules.add(new EdentulousTypeRule() {
			
			public String getExplaination() {
				return "缺隙过中线，尖牙缺失，末端有天然牙：前端游离";
			}
			
			public String toString() {
				return this.getExplaination();
			}
			
			public int getRuleNum() {
				return 4;
			}
			
			public EdentulousType apply(EdentulousSpace edentulous_space) {
				
				Tooth left_neighbor = edentulous_space.getLeftNeighbor();
				Tooth right_neignbor = edentulous_space.getRightNeighbor();
				if(left_neighbor != null && right_neignbor != null) {
					int left_neighbor_zone = left_neighbor.getZone();
					int right_neignbor_zone = right_neignbor.getZone();
					int left_neighbor_num = left_neighbor.getNum();
					int right_neignbor_num = right_neignbor.getNum();
					if((left_neighbor_zone != right_neignbor_zone) && 
						(left_neighbor_num > 3 || right_neignbor_num > 3))
						return EdentulousType.AnteriorExtension;
					else
						return null;
				}
				return null;
			}
		});
		
	}
}
