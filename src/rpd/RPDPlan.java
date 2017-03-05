package rpd;

import java.util.*;

import rpd.components.Component;
import rpd.conceptions.Position;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

public class RPDPlan {

	private Mouth mouth = null;
	
	private Position mandibular_or_maxillary = null;
	
	private Map<ArrayList<Tooth>, Set<Component>> tooth_components = new HashMap<ArrayList<Tooth>, Set<Component>>();

	public RPDPlan(Mouth mouth, Position mandibular_or_maxillary) {
		this.mouth = mouth;
		this.mandibular_or_maxillary = mandibular_or_maxillary;
	}
	
	public RPDPlan(RPDPlan raw_plan) {
		this.mandibular_or_maxillary = raw_plan.mandibular_or_maxillary;
		this.tooth_components.putAll(raw_plan.tooth_components);
		this.mouth = raw_plan.mouth;
	}
	
	public boolean isEmptyPlan() {
		return this.tooth_components.size() == 0;
	}
	
	public Mouth getMouth() {
		return this.mouth;
	}
	
	public Position getPosition() {
		return this.mandibular_or_maxillary;
	}
	
	public void addTooth(ArrayList<Tooth> tooth) {
		if(this.tooth_components.containsKey(tooth))
			return;
		this.tooth_components.put(tooth, new HashSet<Component>());
	}
	
	public void removeTooth(ArrayList<Tooth> tooth) {
		if(!this.tooth_components.containsKey(tooth))
			return;
		Set<Component> components = this.tooth_components.get(tooth);
		this.tooth_components.remove(tooth);
	}
	
	public void addComponent(Component component) {
		ArrayList<Tooth> tooth_pos = component.getToothPos();
		if(!this.tooth_components.containsKey(tooth_pos))
			this.tooth_components.put(tooth_pos, new HashSet<Component>());
		this.tooth_components.get(tooth_pos).add(component);
	}
	
	public Set<ArrayList<Tooth>> getAbutmentTeeth() {
		return this.tooth_components.keySet();
	}
	
	public Map<ArrayList<Tooth>, Set<Component>> getComponents() {
		return this.tooth_components;
	}
	
	public Set<Component> getComponents(Tooth tooth) {
		ArrayList<Tooth> tooth_list = new ArrayList<Tooth>();
		tooth_list.add(tooth);
		return this.tooth_components.get(tooth_list);
	}

	public Set<Component> getComponents(ArrayList<Tooth> tooth_list) {
		return this.tooth_components.get(tooth_list);
	}

	
	public void removeComponent(Component component) {
		ArrayList<Tooth> tooth_pos = component.getToothPos();
		Set<Component> tooth_components = this.tooth_components.get(tooth_pos);
		tooth_components.remove(component);
		if(tooth_components.size() == 0)
			this.tooth_components.remove(tooth_pos);
	}
	
	public String toString() {
		
		Set<String> plan_texts = new HashSet<String>();
		for(Map.Entry<ArrayList<Tooth>, Set<Component>> ent : this.tooth_components.entrySet()) {
			for(Component component : ent.getValue())
				plan_texts.add(component.toString());
		}
		
		String[] plan_texts_str = plan_texts.toArray(new String[0]);
		Arrays.sort(plan_texts_str);
		
		StringBuilder s = new StringBuilder();
		for(String plan_text : plan_texts_str)
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
