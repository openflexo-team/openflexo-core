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

/**
 * One frame of the FML stack of an {@link FMLExecutionFailure}: a behaviour being executed (or the top level, when the failing
 * statement was not run by a behaviour), and the statement of that behaviour that was executing when the failure went through it.
 * <p>
 * A frame only holds text and positions, never the model objects themselves: a failure may be kept (in an action, in the
 * console) after its model is unloaded.
 */
public final class FMLStackFrame {

	private final String behaviour;
	private final String conceptName;
	private final String instance;
	private final String compilationUnitURI;
	private final String statement;
	private final int line;
	private final int column;

	FMLStackFrame(String behaviour, String conceptName, String instance, String compilationUnitURI, String statement, int line,
			int column) {
		this.behaviour = behaviour;
		this.conceptName = conceptName;
		this.instance = instance;
		this.compilationUnitURI = compilationUnitURI;
		this.statement = statement;
		this.line = line;
		this.column = column;
	}

	/** The signature of the behaviour of this frame, or null for the top level */
	public String getBehaviour() {
		return behaviour;
	}

	/** The name of the concept (or virtual model) declaring the behaviour, or null */
	public String getConceptName() {
		return conceptName;
	}

	/** A short text identifying the instance on which the behaviour runs (concept name and identifier), or null */
	public String getInstance() {
		return instance;
	}

	/** The URI of the compilation unit declaring the statement, or null when it is not known */
	public String getCompilationUnitURI() {
		return compilationUnitURI;
	}

	/** The first line of the FML text of the statement executing in this frame, or null when no statement was recorded */
	public String getStatement() {
		return statement;
	}

	/** The line (1-based) of the statement in its source, or -1 when the statement has no source position (built by code) */
	public int getLine() {
		return line;
	}

	/** The column (0-based) of the statement in its source, or -1 */
	public int getColumn() {
		return column;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("at ");
		if (behaviour != null) {
			if (conceptName != null) {
				sb.append(conceptName).append('.');
			}
			sb.append(behaviour);
		}
		else {
			sb.append("<top level>");
		}
		if (line > 0) {
			sb.append(", line ").append(line);
			if (column >= 0) {
				sb.append(':').append(column);
			}
		}
		if (statement != null) {
			sb.append(": ").append(statement);
		}
		if (instance != null) {
			sb.append(" [on ").append(instance).append(']');
		}
		return sb.toString();
	}
}
