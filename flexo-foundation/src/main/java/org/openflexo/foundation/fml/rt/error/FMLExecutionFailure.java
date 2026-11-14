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

import org.openflexo.connie.exception.InvocationTargetTransformException;

/**
 * A failure of an FML execution, which carries the FML stack it went through (CORE-F-11).
 * <p>
 * Implemented by the checked {@link org.openflexo.foundation.fml.rt.error.FMLExecutionException} (and its typed subclasses {@link FMLError},
 * {@link FMLCancellation}) and by the unchecked {@link FMLInternalError}.
 */
public interface FMLExecutionFailure {

	/** The FML stack of this failure, never null */
	public FMLStack getFMLStack();

	/** The message of the failure, without its FML stack */
	public String getMessage();

	/**
	 * Find the FML stack carried by supplied throwable, or by its first cause that carries one
	 * 
	 * @return the stack, or null when no throwable of the chain is an {@link FMLExecutionFailure}
	 */
	public static FMLStack findFMLStack(Throwable t) {
		FMLExecutionFailure failure = findFailure(t);
		return failure != null ? failure.getFMLStack() : null;
	}

	/** The first throwable of the cause chain of supplied throwable that is an {@link FMLExecutionFailure}, or null */
	public static FMLExecutionFailure findFailure(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof FMLExecutionFailure) {
				return (FMLExecutionFailure) c;
			}
			// InvocationTargetTransformException holds its exception without making it its cause
			if (c instanceof InvocationTargetTransformException && ((InvocationTargetTransformException) c).getException() != null) {
				FMLExecutionFailure inTarget = findFailure(((InvocationTargetTransformException) c).getException());
				if (inTarget != null) {
					return inTarget;
				}
			}
			if (c.getCause() == c) {
				break;
			}
		}
		return null;
	}

	/**
	 * A stack for a new failure wrapping supplied cause: the frames of the cause are copied, so that wrapping a failure does not lose
	 * where it happened
	 */
	public static FMLStack stackFor(Throwable cause) {
		FMLStack stack = new FMLStack();
		FMLStack causeStack = findFMLStack(cause);
		if (causeStack != null) {
			stack.copyFrom(causeStack);
		}
		return stack;
	}
}
