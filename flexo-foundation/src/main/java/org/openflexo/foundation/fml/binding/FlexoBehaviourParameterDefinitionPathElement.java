/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;

@Deprecated
// Should be refactored
public class FlexoBehaviourParameterDefinitionPathElement extends SimplePathElement {

	private static final Logger logger = Logger.getLogger(FlexoBehaviourParameterDefinitionPathElement.class.getPackage().getName());

	private FlexoBehaviourParameter parameter;

	private static final String PARAMETER_DEFINITION = "definition";

	public FlexoBehaviourParameterDefinitionPathElement(IBindingPathElement parent, FlexoBehaviourParameter parameter) {
		super(parent, PARAMETER_DEFINITION, parameter.getImplementedInterface());
		this.parameter = parameter;
	}

	/**
	 * Return a flag indicating if this BindingPathElement supports computation with 'null' value as entry (target)
	 * 
	 * Returns true here
	 * 
	 * @return
	 */
	@Override
	public boolean supportsNullValues() {
		return true;
	}

	@Override
	public String getLabel() {
		return PARAMETER_DEFINITION;
		// return "prout-" + parameter.getName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return "Definition for parameter " + parameter.getDescription();
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		// Inconditionnaly return parameter
		return parameter;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException {
		logger.warning("Operation not allowed");
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public void resolve() {
		// Not applicable
	}

}
