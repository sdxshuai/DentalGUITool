package rpd;

import exceptions.rpd.RuleException;
import rpd.components.Component;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

import java.util.*;

public class RPDPlan {

	private Mouth mouth = null;

	private Position mandibular_or_maxillary = null;

	private Map<ArrayList<Tooth>, Set<Component>> tooth_components = new HashMap<ArrayList<Tooth>, Set<Component>>();

	private Set<Component> components = new HashSet<Component>();

	private Set<Tooth> abutment_teeth = new HashSet<Tooth>();

	private List<EdentulousSpace> edentulousSpaces = new ArrayList<>();

	private StringBuilder planExplanationBuilder = new StringBuilder("设计方案简要说明：\n");

	public String getPlanExplanation() {
		return planExplanationBuilder.toString();
	}

	public void appendPlanExplanation(String s) {
		this.planExplanationBuilder.append(s);
	}

	public RPDPlan(Mouth mouth, Position mandibular_or_maxillary) {
		this.mouth = mouth;
		this.mandibular_or_maxillary = mandibular_or_maxillary;
		if (this.mandibular_or_maxillary == Position.Mandibular) {
			this.edentulousSpaces.addAll(mouth.getMandibular().getEdentulousSpaces());
		} else if (this.mandibular_or_maxillary == Position.Maxillary) {
			this.edentulousSpaces.addAll(mouth.getMaxillary().getEdentulousSpaces());
		}
	}

	public RPDPlan(RPDPlan raw_plan) {
		this.planExplanationBuilder = new StringBuilder(raw_plan.getPlanExplanation());
		this.components.addAll(raw_plan.components);
		this.mandibular_or_maxillary = raw_plan.mandibular_or_maxillary;
		this.tooth_components.putAll(raw_plan.tooth_components);
		this.mouth = raw_plan.mouth;
		this.abutment_teeth.addAll(raw_plan.abutment_teeth);
		if (this.mandibular_or_maxillary == Position.Mandibular) {
			this.edentulousSpaces.addAll(mouth.getMandibular().getEdentulousSpaces());
		} else if (this.mandibular_or_maxillary == Position.Maxillary) {
			this.edentulousSpaces.addAll(mouth.getMaxillary().getEdentulousSpaces());
		}
	}

	public boolean isEmptyPlan() {
		return this.components.size() == 0;
	}

	public Mouth getMouth() {
		return this.mouth;
	}

	public Position getPosition() {
		return this.mandibular_or_maxillary;
	}

	public void addTooth(ArrayList<Tooth> tooth_list) {
		if (this.tooth_components.containsKey(tooth_list))
			return;
		this.tooth_components.put(tooth_list, new HashSet<Component>());
	}

	public void addTooth(Tooth tooth) {
		ArrayList<Tooth> tooth_list = new ArrayList<Tooth>();
		tooth_list.add(tooth);
		if (this.tooth_components.containsKey(tooth))
			return;
		this.tooth_components.put(tooth_list, new HashSet<Component>());
	}

	public void removeTooth(Tooth tooth) {
		ArrayList<Tooth> tooth_list = new ArrayList<Tooth>();
		tooth_list.add(tooth);
		if (!this.tooth_components.containsKey(tooth_list))
			return;
		Set<Component> components = this.tooth_components.get(tooth_list);
		for (Component component : components)
			this.components.remove(component);
		this.tooth_components.remove(tooth_list);
	}

	public void removeTooth(ArrayList<Tooth> tooth_list) {
		if (!this.tooth_components.containsKey(tooth_list))
			return;
		Set<Component> components = this.tooth_components.get(tooth_list);
		for (Component component : components)
			this.components.remove(component);
		this.tooth_components.remove(tooth_list);
	}

	public void addComponent(Component component) {
		this.components.add(component);
		ArrayList<Tooth> tooth_pos = component.getToothPos();
		if (!this.tooth_components.containsKey(tooth_pos))
			this.tooth_components.put(tooth_pos, new HashSet<Component>());
		this.tooth_components.get(tooth_pos).add(component);
	}

	public Set<Tooth> getAbutmentTeeth() {
		return this.abutment_teeth;
	}

	public Set<ArrayList<Tooth>> getKeysToothComponents() {
		return this.tooth_components.keySet();
	}

	public void addAbutmentTeeth(ArrayList<Tooth> tooth) {

		this.abutment_teeth.addAll(tooth);
	}

	public void addAbutmentTeeth(Tooth tooth) {
		this.abutment_teeth.add(tooth);
	}

	public Set<Component> getComponents() {
		return this.components;
	}

	public Set<Component> getComponents(ArrayList<Tooth> tooth) {
		return this.tooth_components.get(tooth);
	}

	public Map<ArrayList<Tooth>, Set<Component>> getToothComponents() {
		return this.tooth_components;
	}

	public void removeComponent(Component component) {
		this.components.remove(component);
		ArrayList<Tooth> tooth_pos = component.getToothPos();
		Set<Component> tooth_components = this.tooth_components.get(tooth_pos);
		tooth_components.remove(component);
		if (tooth_components.size() == 0)
			this.tooth_components.remove(tooth_pos);
	}

	public List<EdentulousSpace> getEdentulousSpaces() throws RuleException {
		if (this.mandibular_or_maxillary == Position.Mandibular) {
			return this.mouth.getMandibular().getEdentulousSpaces();
		} else if (this.mandibular_or_maxillary == Position.Maxillary) {
			return this.mouth.getMaxillary().getEdentulousSpaces();
		} else {
			throw new RuleException("rpd has no position");
		}

	}

	public List<Tooth> getExistingTeeth() throws RuleException {
		if (this.mandibular_or_maxillary == Position.Mandibular) {
			return this.mouth.getMandibular().getExistingTeeth();
		} else if (this.mandibular_or_maxillary == Position.Maxillary) {
			return this.mouth.getMaxillary().getExistingTeeth();
		} else {
			throw new RuleException("rpd has no position");
		}

	}

	private int getDistanceBetweenToothAndEdentulous(Tooth tooth, EdentulousSpace edentulous_space) {
		//return 牙位与缺失区域之间牙的个数
		int res = 0;
		int left_dis = 20;
		int right_dis = 20;
		Tooth left_tooth = edentulous_space.getLeftNeighbor();
		if (left_tooth != null) {
			left_dis = tooth.getToothDistance(left_tooth);
		}
		Tooth right_tooth = edentulous_space.getRightNeighbor();
		if (right_tooth != null) {
			right_dis = tooth.getToothDistance(right_tooth);
		}

		res = left_dis > right_dis ? right_dis : left_dis;

		return res;
	}

	//return 牙齿相对于缺失区的位置
	public Position getDirectionToothToEdentulous(Tooth tooth, EdentulousSpace edentulousSpace) {
		if (tooth.getZone() == 1 || tooth.getZone() == 4) {
			Tooth left_tooth = edentulousSpace.getLeftNeighbor();
			if (left_tooth != null && left_tooth.getZone() == tooth.getZone()) {
				if (left_tooth.getNum() <= tooth.getNum()) {
					return Position.Distal;
				} else {
					return Position.Mesial;
				}
			} else if (left_tooth != null && left_tooth.getZone() != tooth.getZone()) {
				return Position.Distal;
			} else if (left_tooth == null) {
				return Position.Mesial;
			} else {
				System.out.println("Error: wrong left tooth!");
				return null;
			}
		} else if (tooth.getZone() == 2 || tooth.getZone() == 3) {
			Tooth right_tooth = edentulousSpace.getRightNeighbor();
			if (right_tooth != null && right_tooth.getZone() == tooth.getZone()) {
				if (right_tooth.getNum() <= tooth.getNum()) {
					return Position.Distal;
				} else {
					return Position.Mesial;
				}
			} else if (right_tooth != null && right_tooth.getZone() != tooth.getZone()) {
				return Position.Distal;
			} else if (right_tooth == null) {
				return Position.Mesial;
			} else {
				System.out.println("Error: wrong right tooth!");
				return null;
			}
		} else {
			System.out.println("Error: Tooth has no proper zone!");
			return null;
		}

	}

	public Map<String, Object> getNearestEdentulous(Tooth tooth) throws RuleException {
		Map<String, Object> res = new HashMap<String, Object>();
		Position res_direction = null; //间隔牙数
		int res_distance = 20;
		EdentulousSpace res_edentulousSpace = null;
		for (EdentulousSpace edentulousSpace : this.getEdentulousSpaces()) {
			int cur_distance = getDistanceBetweenToothAndEdentulous(tooth, edentulousSpace);
			if (cur_distance < res_distance) {
				res_distance = cur_distance;
				res_edentulousSpace = edentulousSpace;
			}
		}
		res_direction = getDirectionToothToEdentulous(tooth, res_edentulousSpace);
		res.put("direction", res_direction);
		res.put("distance", res_distance);
		res.put("edentulous", res_edentulousSpace);
		return res;
	}

	public String toString() {

		Set<String> plan_texts = new HashSet<String>();
		for (Map.Entry<ArrayList<Tooth>, Set<Component>> ent : this.tooth_components.entrySet()) {
			for (Component component : ent.getValue())
				plan_texts.add(component.toString());
		}

		String[] plan_texts_str = plan_texts.toArray(new String[0]);
		Arrays.sort(plan_texts_str);

		StringBuilder s = new StringBuilder();
		for (String plan_text : plan_texts_str)
			s.append(plan_text + "\n");
		return s.toString();
	}
	
	/*public String toString() {
		
		StringBuilder s = new StringBuilder();
		for(Map.Entry<Tooth, List<Component>> ent : this.tooth_components.entrySet()) {
			for(Component component : ent.getValue())
				s.append(component.toString() + "\n");
		}
		return s.toString();
	}*/
	
	/*public String toString() {
		
		StringBuilder s = new StringBuilder();
		for(Component component : components)
			s.append(component.toString() + "\n");
		return s.toString();
	}*/
}
