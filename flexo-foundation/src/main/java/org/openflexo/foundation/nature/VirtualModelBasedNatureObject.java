/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Freemodellingeditor, a component of the software infrastructure 
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

package org.openflexo.foundation.nature;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

/**
 * Base implementation of a {@link NatureObject} which points to a {@link VirtualModel}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(VirtualModelBasedNatureObject.VirtualModelBasedNatureObjectImpl.class)
public interface VirtualModelBasedNatureObject<N extends ProjectNature<N>> extends NatureObject<N> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_URI_KEY = "virtualModelURI";

	@Getter(value = VIRTUAL_MODEL_URI_KEY)
	@XMLAttribute(xmlTag = "virtualModelURI")
	public String getAccessedVirtualModelURI();

	@Setter(VIRTUAL_MODEL_URI_KEY)
	public void setAccessedVirtualModelURI(String virtualModelURI);

	public VirtualModelResource getAccessedVirtualModelResource();

	public void setAccessedVirtualModelResource(VirtualModelResource virtualModelResource);

	public VirtualModel getAccessedVirtualModel();

	public void setAccessedVirtualModel(VirtualModel aVirtualModel);

	public abstract class VirtualModelBasedNatureObjectImpl<N extends ProjectNature<N>> extends FlexoProjectObjectImpl
			implements VirtualModelBasedNatureObject<N> {

		private static final Logger logger = FlexoLogger.getLogger(VirtualModelBasedNatureObject.class.getPackage().getName());

		protected VirtualModelResource virtualModelResource;
		private String virtualModelURI;

		@Override
		public VirtualModelResource getAccessedVirtualModelResource() {

			VirtualModelLibrary fmlLibrary = null;
			if (getNature() != null && getNature().getProject() != null) {
				fmlLibrary = getNature().getProject().getServiceManager().getVirtualModelLibrary();
			}

			if (virtualModelResource == null && StringUtils.isNotEmpty(virtualModelURI) && fmlLibrary != null) {
				virtualModelResource = fmlLibrary.getVirtualModelResource(virtualModelURI);
				if (virtualModelResource != null) {
					logger.info("Looked-up " + virtualModelResource);
				}
			}

			return virtualModelResource;
		}

		@Override
		public void setAccessedVirtualModelResource(VirtualModelResource virtualModelResource) {
			VirtualModelResource oldValue = this.virtualModelResource;
			this.virtualModelResource = virtualModelResource;
			if (virtualModelResource == null) {
				virtualModelURI = null;
			}
			getPropertyChangeSupport().firePropertyChange("accessedVirtualModelResource", oldValue, virtualModelResource);
		}

		@Override
		public String getAccessedVirtualModelURI() {
			if (virtualModelResource != null) {
				return virtualModelResource.getURI();
			}
			return virtualModelURI;
		}

		@Override
		public void setAccessedVirtualModelURI(String metaModelURI) {
			this.virtualModelURI = metaModelURI;
		}

		/**
		 * Return adressed virtual model (the virtual model this model slot specifically adresses, not the one in which it is defined)
		 * 
		 * @return
		 */
		@Override
		public final VirtualModel getAccessedVirtualModel() {
			if (getAccessedVirtualModelResource() != null && !getAccessedVirtualModelResource().isLoading()) {
				// Do not load virtual model when unloaded
				// return getAccessedVirtualModelResource().getLoadedResourceData();
				try {
					return getAccessedVirtualModelResource().getResourceData(null);
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
			}
			return null;
		}

		@Override
		public void setAccessedVirtualModel(VirtualModel aVirtualModel) {
			this.virtualModelURI = aVirtualModel.getURI();
			this.virtualModelResource = (VirtualModelResource) aVirtualModel.getResource();
		}

		@Override
		public FlexoProject<?> getResourceData() {
			return getProject();
		}

		@Override
		public FlexoProject<?> getProject() {
			if (getNature() != null) {
				return getNature().getOwner();
			}
			return super.getProject();
		}

	}
}
