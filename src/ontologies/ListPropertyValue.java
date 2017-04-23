package ontologies;

import exceptions.PropertyValueException;

public class ListPropertyValue extends PropertyValue {

	public ListPropertyValue(int value) {
		this.list_value = value;
	}

	public boolean isListValue() throws PropertyValueException {
		return true;
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
		return false;
	}

	public String toString() {
		return String.valueOf(list_value);
	}

}
