package ontologies;

import exceptions.PropertyValueException;

public class StringPropertyValue extends PropertyValue {
	
	public StringPropertyValue(String value) {
		this.string_value = value;
	}
	
	public boolean isListValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isBooleanValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isIntValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isDoubleValue() throws PropertyValueException {
		return false;
	}
	
	public boolean isStringValue() throws PropertyValueException {
		return true;
	}
	
	public String toString() {
		return String.valueOf(string_value);
	}
}
