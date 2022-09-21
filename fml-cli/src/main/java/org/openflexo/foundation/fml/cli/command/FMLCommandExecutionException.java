package org.openflexo.foundation.fml.cli.command;

import org.openflexo.foundation.FlexoException;

/**
 * This is an exception which may be thrown during FML command or script execution
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLCommandExecutionException extends FlexoException {

	public FMLCommandExecutionException(String message) {
		super(message);
	}

	public FMLCommandExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FMLCommandExecutionException(Throwable cause) {
		super(cause);
	}

}
