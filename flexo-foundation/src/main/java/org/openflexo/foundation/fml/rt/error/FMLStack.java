/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml.rt.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * The FML stack of an {@link FMLExecutionFailure}, built while the failure propagates (CORE-F-11).
 * <p>
 * The statement that is executing when the failure goes through it is recorded first ({@link #recordStatement(FMLControlGraph)}: only the
 * innermost one is kept); when the failure leaves a behaviour, that statement and the behaviour make a frame
 * ({@link #pushBehaviourFrame(FlexoBehaviour, FlexoConceptInstance)}), and the next statement recorded belongs to the calling behaviour.
 * Frames are therefore in the order of a Java stack trace: innermost first.
 * <p>
 * A stack keeps no model object: a statement is described (text, position) when it is recorded, a behaviour and an instance when their
 * frame is made. A failure may outlive its model (kept by an action, in the console, after an unload).
 */
public final class FMLStack {

	private static final int MAX_STATEMENT_LENGTH = 100;

	private final List<FMLStackFrame> frames = new ArrayList<>();
	/** The statement recorded for the current frame, already described, or null */
	private FMLStackFrame pendingStatement;

	/**
	 * Record the statement executing in the current frame, unless a statement was already recorded for it (the innermost one wins).
	 * Sequences are not statements: they are skipped.
	 */
	public void recordStatement(FMLControlGraph statement) {
		if (pendingStatement == null && statement != null && !(statement instanceof Sequence)) {
			pendingStatement = describeStatement(statement);
		}
	}

	/**
	 * Record the location of the current frame as supplied FML object (for instance a behaviour, for a failure that belongs to no
	 * statement of it), unless a statement was already recorded for it
	 */
	public void recordLocation(FMLPrettyPrintable object) {
		if (pendingStatement == null && object != null) {
			pendingStatement = describeStatement(object);
		}
	}

	/**
	 * Close the current frame: supplied behaviour, run on supplied instance, with the statement recorded for it (if any)
	 */
	public void pushBehaviourFrame(FlexoBehaviour behaviour, FlexoConceptInstance instance) {
		String conceptName = null;
		String signature = null;
		if (behaviour != null) {
			signature = behaviour.getSignature();
			conceptName = behaviour.getFlexoConcept() != null ? behaviour.getFlexoConcept().getName() : null;
		}
		pushFrame(signature, conceptName, instance);
	}

	/**
	 * Close the current frame: a body that is not a behaviour (the getter or the setter of a property), named by supplied text, run on
	 * supplied instance
	 */
	public void pushFrame(String bodyName, String conceptName, FlexoConceptInstance instance) {
		String instanceText = null;
		if (instance != null) {
			instanceText = (instance.getFlexoConcept() != null ? instance.getFlexoConcept().getName() : "instance") + "#"
					+ instance.getFlexoID();
		}
		FMLStackFrame statement = pendingStatement;
		frames.add(new FMLStackFrame(bodyName, conceptName, instanceText, statement != null ? statement.getCompilationUnitURI() : null,
				statement != null ? statement.getStatement() : null, statement != null ? statement.getLine() : -1,
				statement != null ? statement.getColumn() : -1));
		pendingStatement = null;
	}

	/**
	 * The frames, innermost first. A statement recorded and not closed by a behaviour (a statement run at the top level, for instance by
	 * a script) makes a last frame without behaviour.
	 */
	public List<FMLStackFrame> getFrames() {
		if (pendingStatement == null) {
			return Collections.unmodifiableList(frames);
		}
		List<FMLStackFrame> returned = new ArrayList<>(frames);
		returned.add(pendingStatement);
		return Collections.unmodifiableList(returned);
	}

	public boolean isEmpty() {
		return frames.isEmpty() && pendingStatement == null;
	}

	/** Copy the frames of supplied stack into this one (used when a failure is wrapped into another) */
	void copyFrom(FMLStack other) {
		frames.addAll(other.frames);
		if (pendingStatement == null) {
			pendingStatement = other.pendingStatement;
		}
	}

	/** The FML stack as text, one frame per line, innermost first */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (FMLStackFrame frame : getFrames()) {
			sb.append("\t").append(frame).append('\n');
		}
		return sb.toString();
	}

	/** A frame without behaviour, describing supplied statement */
	private static FMLStackFrame describeStatement(FMLPrettyPrintable statement) {
		String unitURI = null;
		int line = -1;
		int column = -1;
		FMLPrettyPrintDelegate<?> delegate = statement.getPrettyPrintDelegate();
		RawSourcePosition start = delegate != null ? delegate.getStartLocation() : null;
		if (start != null) {
			line = start.getLine();
			column = start.getPos();
		}
		FMLCompilationUnit unit = statement.getDeclaringCompilationUnit();
		if (unit != null && unit.getVirtualModel() != null) {
			unitURI = unit.getVirtualModel().getURI();
		}
		return new FMLStackFrame(null, null, null, unitURI, statementText(statement), line, column);
	}

	private static String statementText(FMLPrettyPrintable statement) {
		String text;
		try {
			text = statement.getFMLPrettyPrint();
		} catch (RuntimeException e) {
			// The pretty-print of a statement must not hide the failure being reported
			text = null;
		}
		if (text == null || text.trim().isEmpty()) {
			return statement.getImplementedInterface().getSimpleName();
		}
		text = text.trim();
		int eol = text.indexOf('\n');
		if (eol >= 0) {
			text = text.substring(0, eol).trim() + " ...";
		}
		if (text.length() > MAX_STATEMENT_LENGTH) {
			text = text.substring(0, MAX_STATEMENT_LENGTH) + "...";
		}
		return text;
	}
}
