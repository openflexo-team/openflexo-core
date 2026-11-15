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


package org.openflexo.foundation.fml.rt;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.EvaluationPolicy;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;

/**
 * The context of a condition evaluated for each candidate of a query ({@code where} of a fetch request, condition of a {@code match}):
 * the variable of supplied name gives the candidate, every other variable is read in the enclosing context, whose evaluation policy is
 * passed on (CORE-F-11)
 */
public class CandidateEvaluationContext implements BindingEvaluationContext {

	private final String candidateVariableName;
	private final Object candidate;
	private final BindingEvaluationContext enclosingContext;

	public CandidateEvaluationContext(String candidateVariableName, Object candidate, BindingEvaluationContext enclosingContext) {
		this.candidateVariableName = candidateVariableName;
		this.candidate = candidate;
		this.enclosingContext = enclosingContext;
	}

	@Override
	public Object getValue(BindingVariable variable) {
		if (variable.getVariableName().equals(candidateVariableName)) {
			return candidate;
		}
		return enclosingContext.getValue(variable);
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return new FMLExpressionEvaluator(this);
	}

	@Override
	public EvaluationPolicy getEvaluationPolicy() {
		return enclosingContext != null ? enclosingContext.getEvaluationPolicy() : EvaluationPolicy.LENIENT;
	}
}
