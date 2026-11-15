package org.openflexo.foundation.fml.cli.command;

/**
 * Thrown when a command cannot be executed because it is invalid (an unknown variable, an expression that does not type, a directive
 * used out of its context): the script is at fault before anything runs, as opposed to a failure of the FML it executes (CORE-F-11)
 */
@SuppressWarnings("serial")
public class FMLInvalidCommandException extends FMLCommandExecutionException {

	public FMLInvalidCommandException(String message) {
		super(message);
	}

}
