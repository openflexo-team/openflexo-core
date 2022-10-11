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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElementImpl;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction.ParameterValues;

@Deprecated
// Should be refactored
public class FlexoBehaviourParameterValuePathElement extends SimplePathElementImpl implements PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(FlexoBehaviourParameterValuePathElement.class.getPackage().getName());

	private final FlexoBehaviourParameter parameter;
	private Type lastKnownType = null;

	public FlexoBehaviourParameterValuePathElement(IBindingPathElement parent, FlexoBehaviourParameter parameter, Bindable bindable) {
		super(parent, parameter != null ? parameter.getName() : null, parameter != null ? parameter.getType() : null, bindable);
		this.parameter = parameter;
		lastKnownType = parameter != null ? parameter.getType() : null;
	}

	@Override
	public void activate() {
		super.activate();
		if (parameter != null && parameter.getPropertyChangeSupport() != null) {
			parameter.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	@Override
	public void desactivate() {
		if (parameter != null && parameter.getPropertyChangeSupport() != null) {
			parameter.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.desactivate();
	}

	public FlexoBehaviourParameter getParameter() {
		return parameter;
	}

	@Override
	public Type getType() {
		if (parameter != null) {
			return parameter.getType();
		}
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getParameter()) {
			if (evt.getPropertyName().equals(FlexoBehaviourParameter.NAME_KEY)) {
				System.out.println("Notify parameter name changing for " + getParameter() + " new=" + getParameter().getName());
				if (getParameter() != null && getParameter().getBindingModel() != null
						&& getParameter().getBindingModel().getPropertyChangeSupport() != null) {
					getParameter().getBindingModel().getPropertyChangeSupport()
							.firePropertyChange(BindingModel.BINDING_PATH_ELEMENT_NAME_CHANGED, null, this);
				}
			}
			if (lastKnownType != getType()) {
				lastKnownType = getType();
				if (getParameter() != null && getParameter().getBindingModel() != null
						&& getParameter().getBindingModel().getPropertyChangeSupport() != null) {
					getParameter().getBindingModel().getPropertyChangeSupport()
							.firePropertyChange(BindingModel.BINDING_PATH_ELEMENT_TYPE_CHANGED, null, this);
				}
			}

		}
	}

	@Override
	public String getLabel() {
		if (parameter != null) {
			return parameter.getName();
		}
		return null;
	}

	@Override
	public String getPropertyName() {
		if (parameter != null) {
			return parameter.getName();
		}
		return null;
	}

	@Override
	public String getTooltipText(Type resultingType) {
		if (parameter != null) {
			return parameter.getDescription();
		}
		return null;
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof ParameterValues) {
			ParameterValues allParameters = (ParameterValues) target;
			return allParameters.get(parameter.getArgumentName());
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException {
		if (target instanceof ParameterValues) {
			ParameterValues allParameters = (ParameterValues) target;
			// System.out.println("Setting value " + value + " for " + parameter);
			// System.out.println("Parent=" + getParent() + " of " + getParent().getClass());
			if (value != null) {
				allParameters.put(parameter.getArgumentName(), value);
			}
			else {
				allParameters.remove(parameter);
			}
			return;
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
	}

	/*@Override
	public String toString() {
		return "FlexoBehaviourParameterValuePathElement:" + getLabel();
	}*/

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public void resolve() {
		// Not applicable
	}

}
