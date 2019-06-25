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

package org.openflexo.foundation.fml.ta;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.AbstractCreateResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

@ModelEntity
@ImplementationClass(CreateTopLevelVirtualModel.CreateTopLevelVirtualModelImpl.class)
@XMLElement
public interface CreateTopLevelVirtualModel extends AbstractCreateResource<FMLModelSlot, VirtualModel, FMLTechnologyAdapter> {

	@PropertyIdentifier(type = String.class)
	public static final String PARENT_VIRTUAL_MODEL_TYPE_URI_KEY = "parentVirtualModelTypeURI";
	@PropertyIdentifier(type = Boolean.class)
	public static final String FORCE_EXECUTE_CONFIRMATION_PANEL_KEY = "forceExecuteConfirmationPanel";

	@Getter(value = PARENT_VIRTUAL_MODEL_TYPE_URI_KEY)
	@XMLAttribute
	public String _getParentVirtualModelTypeURI();

	@Setter(PARENT_VIRTUAL_MODEL_TYPE_URI_KEY)
	public void _setParentVirtualModelTypeURI(String virtualModelTypeURI);

	public VirtualModelResource getParentVirtualModelType();

	public void setParentVirtualModelType(VirtualModelResource virtualModelType);

	@Getter(value = FORCE_EXECUTE_CONFIRMATION_PANEL_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getForceExecuteConfirmationPanel();

	@Setter(FORCE_EXECUTE_CONFIRMATION_PANEL_KEY)
	public void setForceExecuteConfirmationPanel(boolean forceExecuteConfirmationPanel);

	public static abstract class CreateTopLevelVirtualModelImpl
			extends AbstractCreateResourceImpl<FMLModelSlot, VirtualModel, FMLTechnologyAdapter> implements CreateTopLevelVirtualModel {

		private static final Logger logger = Logger.getLogger(CreateTopLevelVirtualModel.class.getPackage().getName());

		private VirtualModelResource parentVirtualModelType;
		private String parentVirtualModelTypeURI;

		@Override
		public String _getParentVirtualModelTypeURI() {
			if (parentVirtualModelType != null) {
				return parentVirtualModelType.getURI();
			}
			return parentVirtualModelTypeURI;
		}

		@Override
		public void _setParentVirtualModelTypeURI(String virtualModelURI) {
			this.parentVirtualModelTypeURI = virtualModelURI;
		}

		private boolean isComputingParentVirtualModelType = false;

		@Override
		public VirtualModelResource getParentVirtualModelType() {

			if (!isComputingParentVirtualModelType && parentVirtualModelType == null && parentVirtualModelTypeURI != null) {
				isComputingParentVirtualModelType = true;
				try {
					parentVirtualModelType = (VirtualModelResource) getVirtualModelLibrary().getVirtualModel(parentVirtualModelTypeURI)
							.getResource();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FlexoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isComputingParentVirtualModelType = false;
			}

			return parentVirtualModelType;
		}

		@Override
		public void setParentVirtualModelType(VirtualModelResource parentVirtualModelType) {
			if (parentVirtualModelType != this.parentVirtualModelType) {
				VirtualModelResource oldValue = this.parentVirtualModelType;
				this.parentVirtualModelType = parentVirtualModelType;
				getPropertyChangeSupport().firePropertyChange("parentVirtualModelType", oldValue, oldValue);
			}
		}

		@Override
		public Type getAssignableType() {
			return VirtualModel.class;
		}

		@Override
		public VirtualModel execute(RunTimeEvaluationContext evaluationContext) throws FlexoException {

			FMLTechnologyAdapter fmlTA = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);

			VirtualModelResource newVirtualModelResource;
			try {
				newVirtualModelResource = createResource(fmlTA, VirtualModelResourceFactory.class, evaluationContext,
						VirtualModelResourceFactory.FML_SUFFIX, true);
				System.out.println("Return new virtualModel resource: " + newVirtualModelResource);

				newVirtualModelResource.setIsModified();

				VirtualModel virtualModel = newVirtualModelResource.getResourceData();

				if (getParentVirtualModelType() != null) {
					virtualModel.addToParentFlexoConcepts(getParentVirtualModelType().getResourceData());
				}

				System.out.println("Return " + virtualModel);
				return virtualModel;
			} catch (ModelDefinitionException | FileNotFoundException | ResourceLoadingCancelledException e) {
				throw new FlexoException(e);
			}
		}
	}

}
