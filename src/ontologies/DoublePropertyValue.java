package ontologies;

import exceptions.PropertyValueException;

	public class DoublePropertyValue extends PropertyValue {

	public DoublePropertyValue(double value) {
		this.double_value = value;
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
		return true;
	}
	
	public boolean isStringValue() throws PropertyValueException {
		return false;
	}
	
	public String toString() {
		return String.valueOf(double_value);
	}
}
