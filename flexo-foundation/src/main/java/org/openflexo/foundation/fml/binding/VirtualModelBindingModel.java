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

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.FlexoResource;

/**
 * This is the {@link BindingModel} exposed by a {@link VirtualModel}<br>
 * This {@link BindingModel} is based on ViewPoint's (owner of this VirtualModel) {@link BindingModel}
 * 
 * Provides access to the {@link VirtualModelInstance}<br>
 * Allows reflexive access to the {@link VirtualModel} itself<br>
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is a {@link FMLRTVirtualModelInstance}
 * 
 * 
 * @author sylvain
 * 
 */
public class VirtualModelBindingModel extends FlexoConceptBindingModel {

	private CompilationUnitResource lastKnownResource = null;

	/**
	 * Build a new {@link BindingModel} dedicated to a VirtualModel
	 * 
	 * @param viewPoint
	 */
	public VirtualModelBindingModel(VirtualModel virtualModel) {
		super(virtualModel.getContainerVirtualModel() != null && virtualModel != virtualModel.getContainerVirtualModel()
				? virtualModel.getContainerVirtualModel().getBindingModel()
				: null, virtualModel);
		updateCompilationUnitResourceListener();
	}

	private void updateCompilationUnitResourceListener() {

		if (lastKnownResource != getVirtualModel().getResource()) {
			if (lastKnownResource != null) {
				if (lastKnownResource.getPropertyChangeSupport() != null) {
					lastKnownResource.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
			}
			if (getVirtualModel().getResource() != null) {
				if (getVirtualModel().getResource().getPropertyChangeSupport() != null) {
					getVirtualModel().getResource().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
			lastKnownResource = getVirtualModel().getResource();
		}

	}

	public VirtualModel getVirtualModel() {
		return (VirtualModel) getFlexoConcept();
	}

	@Override
	protected void updateContainerBindingVariable() {
		if (getVirtualModel().getContainerVirtualModel() != null) {
			if (containerBindingVariable == null) {
				containerBindingVariable = new BindingVariable(CONTAINER_PROPERTY_NAME,
						getVirtualModel().getContainerVirtualModel().getInstanceType());
				addToBindingVariables(containerBindingVariable);
			}
			containerBindingVariable.setType(getVirtualModel().getContainerVirtualModel().getInstanceType());
		}
		else {
			if (containerBindingVariable != null) {
				removeFromBindingVariables(containerBindingVariable);
				containerBindingVariable = null;
			}
		}
	}

	@Override
	public void update() {
		if (isDeleted()) {
			return;
		}
		super.update();
		updateCompilationUnitResourceListener();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// System.out.println("propertyChange() for VirtualModelBindingModel " + getVirtualModel() + ", je recois " + evt.getPropertyName()
		// + " source ="
		// + evt.getSource() + " evt=" + evt);
		super.propertyChange(evt);
		if (evt.getSource() == getVirtualModel()) {
			/*if (evt.getPropertyName().equals(VirtualModel.CONTAINER_VIRTUAL_MODEL_KEY)) {
				// The VirtualModel changes it's container virtual model
				setBaseBindingModel(getVirtualModel().getContainerVirtualModel() != null
						? getVirtualModel().getContainerVirtualModel().getBindingModel()
						: null);
				// virtualModelInstanceBindingVariable.setType(getVirtualModelInstanceType());
				updateContainerVirtualModelListener();
				updateContainerBindingVariable();
				updatePropertyVariables();
			}*/

			if (evt.getPropertyName().equals(FlexoConcept.CONTAINER_FLEXO_CONCEPT_KEY)) {
				updateContainerVirtualModelListener();
				updateContainerBindingVariable();
				updatePropertyVariables();
			}
		}
		if (getVirtualModel() != null && evt.getSource() == getVirtualModel().getResource()) {
			if (evt.getPropertyName().equals(FlexoResource.CONTAINER)) {
				// System.out.println("OK la resource me notifie " + evt.getPropertyName());
				// The VirtualModel changes it's container virtual model
				setBaseBindingModel(getVirtualModel().getContainerVirtualModel() != null
						? getVirtualModel().getContainerVirtualModel().getBindingModel()
						: null);
				// virtualModelInstanceBindingVariable.setType(getVirtualModelInstanceType());
				updateContainerVirtualModelListener();
				updateContainerBindingVariable();
				updatePropertyVariables();
			}
		}
	}

	private VirtualModel lastKnownContainer = null;

	private void updateContainerVirtualModelListener() {

		if (lastKnownContainer != getVirtualModel().getContainerVirtualModel()) {
			if (lastKnownContainer != null) {
				if (lastKnownContainer.getPropertyChangeSupport() != null) {
					lastKnownContainer.getPropertyChangeSupport().removePropertyChangeListener(this);
				}
				if (lastKnownContainer.getResource() != null && lastKnownContainer.getResource().getPropertyChangeSupport() != null) {
					lastKnownContainer.getResource().getPropertyChangeSupport().removePropertyChangeListener(this);
				}
			}
			if (getVirtualModel().getContainerVirtualModel() != null) {
				if (getVirtualModel().getContainerVirtualModel().getPropertyChangeSupport() != null) {
					getVirtualModel().getContainerVirtualModel().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
				if (getVirtualModel().getContainerVirtualModel().getResource() != null
						&& getVirtualModel().getContainerVirtualModel().getResource().getPropertyChangeSupport() != null) {
					getVirtualModel().getContainerVirtualModel().getResource().getPropertyChangeSupport().addPropertyChangeListener(this);
				}
			}
			lastKnownContainer = getVirtualModel().getContainerVirtualModel();
		}
	}

	@Override
	public void delete() {
		if (lastKnownContainer != null) {
			if (lastKnownContainer.getPropertyChangeSupport() != null) {
				lastKnownContainer.getPropertyChangeSupport().removePropertyChangeListener(this);
			}
		}
		super.delete();
	}

}
