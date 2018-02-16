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
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.ResourceManager;
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
@ImplementationClass(VirtualModelInstanceBasedNatureObject.VirtualModelInstanceBasedNatureObjectImpl.class)
public interface VirtualModelInstanceBasedNatureObject<N extends ProjectNature<N>> extends NatureObject<N> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_INSTANCE_URI_KEY = "virtualModelInstanceURI";
	@PropertyIdentifier(type = String.class)
	String DESCRIPTION_KEY = "description";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_URI_KEY)
	@XMLAttribute(xmlTag = "virtualModelInstanceURI")
	public String getAccessedVirtualModelInstanceURI();

	@Setter(VIRTUAL_MODEL_INSTANCE_URI_KEY)
	public void setAccessedVirtualModelInstanceURI(String virtualModelURI);

	public FMLRTVirtualModelInstanceResource getAccessedVirtualModelInstanceResource();

	public void setAccessedVirtualModelInstanceResource(FMLRTVirtualModelInstanceResource virtualModelResource);

	public FMLRTVirtualModelInstance getAccessedVirtualModelInstance();

	public void setAccessedVirtualModelInstance(FMLRTVirtualModelInstance aVirtualModelInstance);

	public String getName();

	public String getURI();

	@Getter(value = DESCRIPTION_KEY)
	@XMLAttribute
	public String getDescription();

	@Setter(DESCRIPTION_KEY)
	public void setDescription(String description);

	public abstract class VirtualModelInstanceBasedNatureObjectImpl<N extends ProjectNature<N>> extends FlexoProjectObjectImpl
			implements VirtualModelInstanceBasedNatureObject<N> {

		private static final Logger logger = FlexoLogger.getLogger(VirtualModelInstanceBasedNatureObject.class.getPackage().getName());

		protected FMLRTVirtualModelInstanceResource virtualModelInstanceResource;
		private String virtualModelInstanceURI;

		@Override
		public FMLRTVirtualModelInstanceResource getAccessedVirtualModelInstanceResource() {

			ResourceManager resourceManager = null;
			if (getNature() != null && getNature().getProject() != null) {
				resourceManager = getNature().getProject().getServiceManager().getResourceManager();
			}

			if (virtualModelInstanceResource == null && StringUtils.isNotEmpty(virtualModelInstanceURI) && resourceManager != null) {
				virtualModelInstanceResource = (FMLRTVirtualModelInstanceResource) resourceManager.getResource(virtualModelInstanceURI,
						FMLRTVirtualModelInstance.class);
				if (virtualModelInstanceResource != null) {
					logger.info("Looked-up " + virtualModelInstanceResource);
				}
			}

			return virtualModelInstanceResource;
		}

		@Override
		public void setAccessedVirtualModelInstanceResource(FMLRTVirtualModelInstanceResource virtualModelInstanceResource) {
			FMLRTVirtualModelInstanceResource oldValue = this.virtualModelInstanceResource;
			this.virtualModelInstanceResource = virtualModelInstanceResource;
			if (virtualModelInstanceResource == null) {
				virtualModelInstanceURI = null;
			}
			getPropertyChangeSupport().firePropertyChange("accessedVirtualModelInstanceResource", oldValue, virtualModelInstanceResource);
		}

		@Override
		public String getAccessedVirtualModelInstanceURI() {
			if (virtualModelInstanceResource != null) {
				return virtualModelInstanceResource.getURI();
			}
			return virtualModelInstanceURI;
		}

		@Override
		public void setAccessedVirtualModelInstanceURI(String metaModelURI) {
			this.virtualModelInstanceURI = metaModelURI;
		}

		/**
		 * Return adressed virtual model (the virtual model this model slot specifically adresses, not the one in which it is defined)
		 * 
		 * @return
		 */
		@Override
		public final FMLRTVirtualModelInstance getAccessedVirtualModelInstance() {
			if (getAccessedVirtualModelInstanceResource() != null && !getAccessedVirtualModelInstanceResource().isLoading()) {
				// Do not load virtual model when unloaded
				// return getAccessedVirtualModelResource().getLoadedResourceData();
				try {
					FMLRTVirtualModelInstance returned = getAccessedVirtualModelInstanceResource().getResourceData(null);
					if (returned != null) {
						fireVirtualModelInstanceConnected(returned);
					}
					return returned;
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
		public void setAccessedVirtualModelInstance(FMLRTVirtualModelInstance aVirtualModelInstance) {
			if (getAccessedVirtualModelInstance() != aVirtualModelInstance) {
				if (getAccessedVirtualModelInstance() != null) {
					fireVirtualModelInstanceDisconnected(getAccessedVirtualModelInstance());
				}
				this.virtualModelInstanceURI = aVirtualModelInstance.getURI();
				this.virtualModelInstanceResource = (FMLRTVirtualModelInstanceResource) aVirtualModelInstance.getResource();
				fireVirtualModelInstanceConnected(aVirtualModelInstance);
			}
		}

		public void fireVirtualModelInstanceConnected(FMLRTVirtualModelInstance aVirtualModelInstance) {
			// getPropertyChangeSupport().firePropertyChange("name", null, aVirtualModelInstance.getName());
			// getPropertyChangeSupport().firePropertyChange("URI", null, aVirtualModelInstance.getURI());
		}

		public void fireVirtualModelInstanceDisconnected(FMLRTVirtualModelInstance aVirtualModelInstance) {
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

		@Override
		public String getName() {
			if (getAccessedVirtualModelInstance() != null) {
				return getAccessedVirtualModelInstance().getName();
			}
			return null;
		}

		@Override
		public String getURI() {
			if (getAccessedVirtualModelInstance() != null) {
				return getAccessedVirtualModelInstance().getURI();
			}
			return null;
		}

	}
}
