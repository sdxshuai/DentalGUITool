package ontologies;

import org.apache.jena.ontology.OntProperty;

import misc.ToothMap;
import ontologies.LabelModifier;
import ontologies.PropertyValue;

//label属性
public class PropertyLabel {

	private int start_offset = -1;
	
	private int end_offset = -1;

	private ToothMap tooth_map = null;
	
	private String property_text = null;
	
	private OntProperty property = null;
	
	private PropertyValue property_value = null;
	
	private LabelModifier modifier = null;
	
	public PropertyLabel(int start_offset, int end_offset, String property_text, 
			OntProperty property, PropertyValue property_value, LabelModifier modifier, ToothMap tooth_map) {
		
		this.start_offset = start_offset;
		this.end_offset = end_offset;
		this.property_text = property_text;
		this.property = property;
		this.property_value = property_value;
		this.modifier = modifier;
		this.tooth_map = tooth_map;
	}
	
	public int getStartOffset() {
		return this.start_offset;
	}
	
	public int getEndOffset() {
		return this.end_offset;
	}
	
	public String getPropertyText() {
		return this.property_text;
	}
	
	public OntProperty getProperty() {
		return this.property;
	}
	
	public PropertyValue getPropertyValue() {
		return this.property_value;
	}
	
	public void deleteToothMap() {
		this.tooth_map = null;
	}
	
	public ToothMap getToothMap() {
		return this.tooth_map;
	}
	
	public void setToothMap(ToothMap tooth_map) {
		this.tooth_map = tooth_map;
	}
	
	public String toString() {
		return this.property_text + " (" + this.modifier.name() + ") " + "(" + this.property.getLocalName() + ")";
	}
	
	public void setModifier(LabelModifier modifier) {
		this.modifier = modifier;
	}
	
	public LabelModifier getModifier() {
		return this.modifier;
	}
}

