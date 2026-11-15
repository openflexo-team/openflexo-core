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
 * The unchecked carrier of a checked FML failure across an interface whose signature cannot declare it and is not a Connie path element:
 * {@link org.openflexo.connie.BindingEvaluationContext#getValue(org.openflexo.connie.BindingVariable)} and
 * {@link org.openflexo.connie.SettableBindingEvaluationContext#setValue(Object, org.openflexo.connie.BindingVariable)}, when a property read
 * through its bare name has a getter or a setter that fails (CORE-F-11).
 * <p>
 * It is never reported nor logged: {@link FMLEvaluation} (or {@link FMLExecution#convertUnchecked(RuntimeException)} at the first statement
 * it crosses) unwraps it and rethrows the same failure.
 */
@SuppressWarnings("serial")
public class FMLFailureTunnel extends RuntimeException {

	private final FMLExecutionException failure;

	public FMLFailureTunnel(FMLExecutionException failure) {
		super(failure.getMessage(), failure, false, false);
		this.failure = failure;
	}

	/** The failure carried, to be rethrown unchanged */
	public FMLExecutionException getFailure() {
		return failure;
	}
}
