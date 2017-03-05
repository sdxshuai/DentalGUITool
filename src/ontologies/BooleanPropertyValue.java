package ontologies;

import exceptions.PropertyValueException;

//bool type value

public class BooleanPropertyValue extends PropertyValue {

	public BooleanPropertyValue(boolean value) {
		this.boolean_value = value;
	}
	
	public boolean isListValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isBooleanValue() throws PropertyValueException {
		return true;
	}
	
	public boolean isIntValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isDoubleValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isStringValue() throws PropertyValueException {
		return false;
	}
	
	public String toString() {
		return String.valueOf(boolean_value);
	}
}
