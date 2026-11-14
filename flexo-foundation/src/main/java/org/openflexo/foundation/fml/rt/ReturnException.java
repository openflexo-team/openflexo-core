package org.openflexo.foundation.fml.rt;

/**
 * This exception does not reflect an issue while executing FML, but is used to prioritary intercept "return" statement.
 * <p>
 * Control flow, never an error: it does not leave the execution of a behaviour, which takes its value as the returned value, whatever
 * the kind of behaviour (CORE-F-11). It builds no stack trace, since one is thrown at every {@code return}.
 * 
 * @author sylvain
 */
@SuppressWarnings("serial")
public class ReturnException extends Exception {

	private final Object returnedValue;

	public ReturnException(Object returnedValue) {
		super(null, null, false, false);
		this.returnedValue = returnedValue;
	}

	public Object getReturnedValue() {
		return returnedValue;
	}
}