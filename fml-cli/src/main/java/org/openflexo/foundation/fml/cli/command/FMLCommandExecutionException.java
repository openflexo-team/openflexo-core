package org.openflexo.foundation.fml.cli.command;

import org.openflexo.foundation.OpenflexoRuntimeException;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLExecutionFailure;

/**
 * This is an exception which may be thrown during FML command or script execution.<br>
 * 
 * It knows where the failing command is: its line in the script and the script itself, set by {@link FMLScript#execute()} and by the
 * {@code execute} directive (CORE-F-11). When the command failed because FML failed, the FML failure (an {@code FMLError}, or an
 * {@code FMLInternalError} for a defect of the platform) is its cause, and {@link #describe()} gives its kind, its message and its FML
 * stack.
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FMLCommandExecutionException extends OpenflexoRuntimeException {

	private int line = -1;
	private String source;

	public FMLCommandExecutionException(String message) {
		super(message);
	}

	public FMLCommandExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FMLCommandExecutionException(Throwable cause) {
		super(cause);
	}

	/** The line of the failing command in its script, -1 when unknown (a command typed in the interpreter) */
	public int getLine() {
		return line;
	}

	/** Set the line of the failing command, unless it is known already (the innermost script sets it first) */
	public void setLine(int line) {
		if (this.line < 0) {
			this.line = line;
		}
	}

	/** The name of the script holding the failing command, null when unknown */
	public String getSource() {
		return source;
	}

	/** Set the name of the script holding the failing command, unless it is known already */
	public void setSource(String source) {
		if (this.source == null) {
			this.source = source;
		}
	}

	/** Where the failing command is, as text ("Line 12", "Foo.fmlscript, line 12"), or null when unknown */
	public String getLocation() {
		if (line < 0) {
			return source;
		}
		return source != null ? source + ", line " + line : "Line " + line;
	}

	/** The message of the failure, without its location */
	public String getDetail() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		String location = getLocation();
		return location != null ? location + ": " + getDetail() : getDetail();
	}

	/**
	 * The failure of FML this command failure stands for: a foreseen {@code FMLError}, or an unforeseen failure (a defect of the platform).
	 * Null when the command failed by itself (an invalid command, a failed assertion).
	 */
	public Throwable getFailure() {
		FMLExecutionFailure fmlFailure = FMLExecutionFailure.findFailure(getCause());
		if (fmlFailure instanceof Throwable) {
			return (Throwable) fmlFailure;
		}
		if (getCause() != null && !FMLErrorReporter.isForeseen(getCause())) {
			return getCause();
		}
		return null;
	}

	/** Tell if this failure is foreseen by FML (the script or its data are at fault), as opposed to a defect of the platform */
	public boolean isForeseen() {
		return FMLErrorReporter.isForeseen(this);
	}

	/**
	 * A text describing this failure for the user: where the command is, the kind and the message of the FML failure and its FML stack
	 * (see {@link FMLErrorReporter#describe(String, Throwable)}), or the message of the command
	 */
	public String describe() {
		String location = getLocation();
		Throwable failure = getFailure();
		if (failure != null) {
			return FMLErrorReporter.describe(location != null ? location : "Command failed", failure);
		}
		return getMessage();
	}

}
