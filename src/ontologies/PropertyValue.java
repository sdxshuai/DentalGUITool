package ontologies;

import exceptions.PropertyValueException;

public abstract class PropertyValue {

	protected int list_value = -1;
	
	protected boolean boolean_value = false;
	
	protected int int_value = -1;
	
	protected double double_value = 0.0d;
	
	protected String string_value = null;
	
	protected PropertyValue() {}
	
	public boolean isListValue() throws PropertyValueException {
		throw new PropertyValueException("illegal call");
	}
	
	public boolean isBooleanValue() throws PropertyValueException {
		throw new PropertyValueException("illegal call");
	}
	
	public boolean isIntValue() throws PropertyValueException {
		throw new PropertyValueException("illegal call");
	}
	
	public boolean isDoubleValue() throws PropertyValueException {	
		throw new PropertyValueException("illegal call");
	}
	
	public boolean isStringValue() throws PropertyValueException {	
		throw new PropertyValueException("illegal call");
	}
	
	public int getListValue() throws PropertyValueException {
		
		if(!this.isListValue())
			throw new PropertyValueException("illegal call");
		return this.list_value;
	}
	
	public boolean getBooleanValue() throws PropertyValueException {
		
		if(!this.isBooleanValue())
			throw new PropertyValueException("illegal call");
		return this.boolean_value;
	}
	
	public int getIntValue() throws PropertyValueException {
		
		if(!this.isIntValue())
			throw new PropertyValueException("illegal call");
		return this.int_value;
	}
	
	public double getDoubleValue() throws PropertyValueException {
		
		if(!this.isDoubleValue())
			throw new PropertyValueException("illegal call");
		return this.double_value;
	}
	
	public String getStringValue() throws PropertyValueException {
		
		if(!this.isStringValue())
			throw new PropertyValueException("illegal call");
		return this.string_value;
	}
}
