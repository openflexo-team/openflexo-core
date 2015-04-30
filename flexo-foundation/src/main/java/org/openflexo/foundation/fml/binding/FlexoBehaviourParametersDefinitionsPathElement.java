/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParametersType;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

public class FlexoBehaviourParametersDefinitionsPathElement extends SimplePathElement {
	static final Logger logger = Logger.getLogger(FlexoBehaviourParametersDefinitionsPathElement.class.getPackage().getName());

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersDefinitionsPathElement(BindingPathElement parent, FlexoBehaviour aFlexoBehaviour) {
		super(parent, "parametersDefinitions", FlexoBehaviourParametersType.getFlexoBehaviourParametersType(aFlexoBehaviour));
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	@Override
	public String getLabel() {
		return "parametersDefinitions";
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return "parametersDefinitions";
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof FlexoBehaviour) {
			return ((FlexoBehaviour) target).getParameters();
		}
		if (target instanceof FlexoBehaviourAction) {
			return ((FlexoBehaviourAction) target).getFlexoBehaviour().getParameters();
		}
		logger.warning("Please implement me, target=" + target + " of " + target.getClass() + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context) throws TypeMismatchException,
			NullReferenceException {
		logger.warning("Please implement me, target=" + target + " context=" + context);
	}

}
