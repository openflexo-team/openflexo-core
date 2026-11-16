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

package org.openflexo.foundation.fml;

import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A {@link FlexoConcept} representing an exception, declared in FML with {@code exception} (CORE-F-11 phase 7).
 * <p>
 * An exception is created and raised with {@code throw new ExceptionType(...)}, and handled by a {@code catch} clause naming it or one of
 * its super exceptions, or {@code FMLError} (which catches every foreseen error). Not caught, it fails the action, and is reported as
 * every other FML error, with its FML stack. Its message is its string representation: the renderer of the exception when it has one,
 * otherwise its name and the values of its properties.
 * <p>
 * An exception extends only exceptions, and a concept that is not an exception never extends one. An exception instance
 * ({@link org.openflexo.foundation.fml.rt.FlexoExceptionInstance}) is not registered in any virtual model instance: its life is restricted
 * to its propagation and its handling. Besides its properties, a caught exception exposes {@code message}, {@code kind} and
 * {@code fmlStackTrace}.
 * <p>
 * Example:
 *
 * <pre>
 * public model Stock {
 *     int quantity;
 *
 *     public take(int n) {
 *         if (n &gt; quantity) {
 *             throw new OutOfStock(n, quantity);
 *         }
 *         quantity = quantity - n;
 *     }
 *
 *     public int tryToTake(int n) {
 *         try {
 *             take(n);
 *             return n;
 *         } catch (OutOfStock e) {
 *             return 0;
 *         }
 *     }
 *
 *     &#64;Renderer("cannot take " + requested + ", only " + available)
 *     exception OutOfStock {
 *         int requested;
 *         int available;
 *         create(int requested, int available) {
 *             requested = parameters.requested;
 *             available = parameters.available;
 *         }
 *     }
 * }
 * </pre>
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FlexoExceptionConcept.FlexoExceptionConceptImpl.class)
public interface FlexoExceptionConcept extends FlexoConcept {

	public static abstract class FlexoExceptionConceptImpl extends FlexoConceptImpl implements FlexoExceptionConcept {

	}

}
