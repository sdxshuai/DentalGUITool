package ontologies;

import exceptions.PropertyValueException;

public class IntPropertyValue extends PropertyValue {

	public IntPropertyValue(int value) {
		this.int_value = value;
	}

	public boolean isListValue() throws PropertyValueException {
		return false;
	}

	public boolean isBooleanValue() throws PropertyValueException {
		return false;
	}

	public boolean isIntValue() throws PropertyValueException {
		return true;
	}

	public boolean isDoubleValue() throws PropertyValueException {
		return false;
	}

	public boolean isStringValue() throws PropertyValueException {
		return false;
	}

	public String toString() {
		return String.valueOf(int_value);
	}
}
