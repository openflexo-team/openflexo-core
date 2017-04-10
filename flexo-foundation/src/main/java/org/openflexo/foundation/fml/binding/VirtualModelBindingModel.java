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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;

/**
 * This is the {@link BindingModel} exposed by a {@link VirtualModel}<br>
 * This {@link BindingModel} is based on ViewPoint's (owner of this VirtualModel) {@link BindingModel}
 * 
 * Provides access to the {@link View}<br>
 * Allows reflexive access to the {@link VirtualModel} itself<br>
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is a {@link VirtualModelInstance}
 * 
 * 
 * @author sylvain
 * 
 */
public class VirtualModelBindingModel extends FlexoConceptBindingModel implements PropertyChangeListener {

	private final AbstractVirtualModel<?> virtualModel;

	private BindingVariable reflexiveAccessBindingVariable;
	private BindingVariable virtualModelInstanceBindingVariable;

	public static final String REFLEXIVE_ACCESS_PROPERTY = "virtualModelDefinition";
	public static final String VIRTUAL_MODEL_INSTANCE_PROPERTY = "virtualModelInstance";

	/**
	 * Build a new {@link BindingModel} dedicated to a VirtualModel
	 * 
	 * @param viewPoint
	 */
	protected VirtualModelBindingModel(AbstractVirtualModel<?> virtualModel) {
		super(virtualModel.getViewPoint() != null && virtualModel != virtualModel.getViewPoint()
				? virtualModel.getViewPoint().getBindingModel() : null, virtualModel);
		this.virtualModel = virtualModel;
	}

	public VirtualModelBindingModel(VirtualModel virtualModel) {
		this((AbstractVirtualModel<?>) virtualModel);
		virtualModelInstanceBindingVariable = new BindingVariable(VIRTUAL_MODEL_INSTANCE_PROPERTY,
				VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel));
		addToBindingVariables(virtualModelInstanceBindingVariable);
		reflexiveAccessBindingVariable = new BindingVariable(REFLEXIVE_ACCESS_PROPERTY, VirtualModel.class);
		addToBindingVariables(reflexiveAccessBindingVariable);
	}

	public AbstractVirtualModel<?> getVirtualModel() {
		return virtualModel;
	}

	public BindingVariable getVirtualModelInstanceBindingVariable() {
		return virtualModelInstanceBindingVariable;
	}

	/**
	 * Return the reflexive access {@link BindingVariable}<br>
	 * (Allows reflexive access to the {@link VirtualModel} itself)
	 * 
	 * @return
	 */
	@Override
	public BindingVariable getReflexiveAccessBindingVariable() {
		return reflexiveAccessBindingVariable;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// System.out.println("Hop, je recois " + evt.getPropertyName() + " source =" + evt.getSource() + " evt=" + evt);
		super.propertyChange(evt);
		if (evt.getSource() == virtualModel) {
			if (evt.getPropertyName().equals(VirtualModel.VIEW_POINT_KEY)) {
				// The VirtualModel changes it's ViewPoint
				setBaseBindingModel(virtualModel.getViewPoint() != null ? virtualModel.getViewPoint().getBindingModel() : null);
				// viewBindingVariable.setType(virtualModel.getViewPoint() != null ? ViewType.getViewType(virtualModel.getViewPoint())
				// : View.class);
				virtualModelInstanceBindingVariable.setType(getVirtualModelInstanceType());
			}
		}
	}

	protected VirtualModelInstanceType getVirtualModelInstanceType() {
		return VirtualModelInstanceType.getVirtualModelInstanceType(virtualModel);
	}

}
