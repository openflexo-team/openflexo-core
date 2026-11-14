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
 * An unforeseen failure during an FML execution: a defect of the platform (a {@link NullPointerException}, a
 * {@link ClassCastException}, an {@link IllegalStateException}... raised by OpenFlexo code), wrapped once, where it crosses the
 * execution of a statement, so that it carries the FML stack (CORE-F-11). Unchecked, as a programming error is: it is never catchable from
 * FML, and is reported as a bug.
 */
@SuppressWarnings("serial")
public class FMLInternalError extends RuntimeException implements FMLExecutionFailure {

	private final FMLStack stack;

	public FMLInternalError(Throwable cause) {
		this("Unexpected " + cause.getClass().getSimpleName() + (cause.getMessage() != null ? ": " + cause.getMessage() : ""), cause);
	}

	public FMLInternalError(String message, Throwable cause) {
		super(message, cause);
		stack = FMLExecutionFailure.stackFor(cause);
	}

	@Override
	public FMLStack getFMLStack() {
		return stack;
	}
}
