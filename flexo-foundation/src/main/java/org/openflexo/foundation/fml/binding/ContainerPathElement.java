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

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElementImpl;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

/**
 * This is the "container" path element, applicable to a {@link FlexoConceptInstance} with allow to access to it's
 * {@link FMLRTVirtualModelInstance} container
 * 
 * There are three cases:
 * <ul>
 * <li>either the path element applies to a {@link VirtualModel}: in this case the container is bound to container VirtualModel
 * instance</li>
 * <li>either the path element applied to a basic {@link FlexoConcept} which is not embedded in another concept: in this case, the container
 * is bound to VirtualModel instance in which the FCI is defined</li>
 * <li>either the path element applied to a basic {@link FlexoConcept} which is embedded in another concept: in this case, the container is
 * bound to instance of FlexoConcept in which the FCI is defined</li>
 * </ul>
 * 
 * @author sylvain
 *
 */
public class ContainerPathElement extends SimplePathElementImpl<FMLNativeProperty> {

	private static final Logger logger = Logger.getLogger(ContainerPathElement.class.getPackage().getName());

	private FlexoConcept applicableFlexoConcept;
	private FlexoConcept containerType;

	public ContainerPathElement(IBindingPathElement parent, FlexoConcept applicableFlexoConcept, Bindable bindable) {
		super(parent, FlexoConceptBindingModel.CONTAINER_PROPERTY_NAME, Object.class, bindable);
		this.applicableFlexoConcept = applicableFlexoConcept;
		setProperty(FlexoConceptBindingModel.CONTAINER_PROPERTY);
		if (applicableFlexoConcept instanceof VirtualModel) {
			containerType = ((VirtualModel) applicableFlexoConcept).getContainerVirtualModel();
		}
		else if (applicableFlexoConcept.getApplicableContainerFlexoConcept() != null) {
			containerType = applicableFlexoConcept.getApplicableContainerFlexoConcept();
		}
		else {
			containerType = applicableFlexoConcept.getOwningVirtualModel();
		}
	}

	/**
	 * Return {@link FlexoConcept} on which this path element applies
	 * 
	 * @return
	 */
	public FlexoConcept getApplicableFlexoConcept() {
		return applicableFlexoConcept;
	}

	/**
	 * Return type of accessed container
	 * 
	 * @return
	 */
	public FlexoConcept getContainerType() {
		return containerType;
	}

	@Override
	public Type getType() {
		return FlexoConceptInstanceType.getFlexoConceptInstanceType(containerType);
	}

	@Override
	public String getLabel() {
		return getPropertyName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		if (containerType != null) {
			return containerType.getDescription();
		}
		return null;
	}

	@Override
	public boolean isSettable() {
		return false;
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof FlexoBehaviourAction) {
			return ((FlexoBehaviourAction<?, ?, ?>) target).getVirtualModelInstance();
		}
		if (target instanceof VirtualModelInstance) {
			return ((VirtualModelInstance<?, ?>) target).getContainerVirtualModelInstance();
		}
		else if (target instanceof FlexoConceptInstance) {
			if (applicableFlexoConcept.getApplicableContainerFlexoConcept() != null) {
				return ((FlexoConceptInstance) target).getContainerFlexoConceptInstance();
			}
			return ((FlexoConceptInstance) target).getVirtualModelInstance();
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
