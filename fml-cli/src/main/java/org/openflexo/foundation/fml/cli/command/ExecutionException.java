package org.openflexo.foundation.fml.cli.command;

public class ExecutionException extends Exception {

	public ExecutionException(String message) {
		super(message);
	}

	public ExecutionException(String message, Throwable cause) {
		super(message + " : " + cause.getMessage(), cause);
	}

	public ExecutionException(Throwable cause) {
		super("ExecutionException caused by " + cause.getMessage());
	}

}