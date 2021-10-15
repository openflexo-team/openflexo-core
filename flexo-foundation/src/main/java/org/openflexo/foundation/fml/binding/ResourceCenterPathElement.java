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
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.resource.FlexoResourceCenter;

/**
 * A path element which represents a {@link FlexoResourceCenter} accessible at run-time<br>
 * 
 * @author sylvain
 *
 */
public class ResourceCenterPathElement extends SimplePathElement<FMLNativeProperty> {

	private static final Logger logger = Logger.getLogger(ResourceCenterPathElement.class.getPackage().getName());

	public ResourceCenterPathElement(IBindingPathElement parent) {
		super(parent, FMLBindingFactory.RESOURCE_CENTER_PROPERTY_NAME, FlexoResourceCenter.class);
		setProperty(FMLBindingFactory.RESOURCE_CENTER_PROPERTY);
	}

	@Override
	public String getLabel() {
		return getPropertyName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return null;
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {

		if (target instanceof FlexoBehaviourAction) {
			return ((FlexoBehaviourAction<?, ?, ?>) target).getVirtualModelInstance().getResourceCenter();
		}
		if (target instanceof FlexoConceptInstance) {
			return ((FlexoConceptInstance) target).getVirtualModelInstance().getResourceCenter();
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException {
		logger.warning("Please implement me, target=" + target + " context=" + context);
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
