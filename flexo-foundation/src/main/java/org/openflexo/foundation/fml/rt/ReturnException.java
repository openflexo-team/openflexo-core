package org.openflexo.foundation.fml.rt;

/**
 * This exception does not reflect an issue while executing FML, but is used to prioritary intercept "return" statement
 * 
 * @author sylvain
 */
@SuppressWarnings("serial")
public class ReturnException extends Exception {

	private final Object returnedValue;

	public ReturnException(Object returnedValue) {
		this.returnedValue = returnedValue;
	}

	public Object getReturnedValue() {
		return returnedValue;
	}
}