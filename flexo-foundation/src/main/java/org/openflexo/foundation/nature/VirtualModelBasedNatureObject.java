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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
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

	public CompilationUnitResource getAccessedVirtualModelResource();

	public void setAccessedVirtualModelResource(CompilationUnitResource virtualModelResource);

	public VirtualModel getAccessedVirtualModel();

	public void setAccessedVirtualModel(VirtualModel aVirtualModel);

	public abstract class VirtualModelBasedNatureObjectImpl<N extends ProjectNature<N>> extends FlexoProjectObjectImpl
			implements VirtualModelBasedNatureObject<N> {

		private static final Logger logger = FlexoLogger.getLogger(VirtualModelBasedNatureObject.class.getPackage().getName());

		protected CompilationUnitResource virtualModelResource;
		private String virtualModelURI;

		@Override
		public CompilationUnitResource getAccessedVirtualModelResource() {

			VirtualModelLibrary fmlLibrary = null;
			if (getNature() != null && getNature().getProject() != null) {
				fmlLibrary = getNature().getProject().getServiceManager().getVirtualModelLibrary();
			}

			if (virtualModelResource == null && StringUtils.isNotEmpty(virtualModelURI) && fmlLibrary != null) {
				virtualModelResource = fmlLibrary.getCompilationUnitResource(virtualModelURI);
				if (virtualModelResource != null) {
					logger.info("Looked-up " + virtualModelResource);
					listenTo(virtualModelResource);
					// What was computed from it until now (a label, typically) may be outdated
					fireAccessedVirtualModelChanged();
				}
			}

			return virtualModelResource;
		}

		/**
		 * Listens to the resource of the accessed virtual model, to notify when it gets loaded.
		 *
		 * <p>
		 * A view displays what a nature object derives from its virtual model - its name, typically - and caches it: a browser cell only
		 * computes its label again when notified. Asked while the virtual model is not available yet (still loading, or its resource not
		 * found yet), such a value is null, and would stay so.
		 *
		 * <p>
		 * A PropertyChangeListener, not a FlexoObserver: the FlexoObservable API is to be deprecated. The resource fires
		 * <code>loadedCompilationUnit</code> once loading is complete, second analysis pass included.
		 */
		private final PropertyChangeListener resourceListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (LOADED_COMPILATION_UNIT.equals(evt.getPropertyName())) {
					fireAccessedVirtualModelChanged();
				}
			}
		};

		/** What {@link CompilationUnitResource} notifies when it is loaded */
		private static final String LOADED_COMPILATION_UNIT = "loadedCompilationUnit";

		private CompilationUnitResource listenedResource;

		private void listenTo(CompilationUnitResource resource) {
			if (listenedResource == resource) {
				return;
			}
			if (listenedResource != null) {
				listenedResource.getPropertyChangeSupport().removePropertyChangeListener(resourceListener);
			}
			listenedResource = resource;
			if (resource != null) {
				resource.getPropertyChangeSupport().addPropertyChangeListener(resourceListener);
			}
		}

		/**
		 * Notify that the accessed virtual model, and what subclasses derive from it, may have changed. <code>name</code> is notified as
		 * well: it is what the nature objects of the platform derive from their virtual model, and what a browser shows.
		 */
		protected void fireAccessedVirtualModelChanged() {
			getPropertyChangeSupport().firePropertyChange("accessedVirtualModel", null, virtualModelResource);
			getPropertyChangeSupport().firePropertyChange("name", null, getLoadedOrResourceName());
		}

		/**
		 * A name for the accessed virtual model that never loads it: its own name when loaded, the name of its resource otherwise (they are
		 * the same unless the virtual model was renamed since), null when its resource is not found.
		 */
		protected String getLoadedOrResourceName() {
			CompilationUnitResource resource = getAccessedVirtualModelResource();
			if (resource == null) {
				return null;
			}
			if (resource.isLoaded() && resource.getLoadedCompilationUnit() != null
					&& resource.getLoadedCompilationUnit().getVirtualModel() != null) {
				return resource.getLoadedCompilationUnit().getVirtualModel().getName();
			}
			return resource.getName();
		}

		@Override
		public void setAccessedVirtualModelResource(CompilationUnitResource virtualModelResource) {
			CompilationUnitResource oldValue = this.virtualModelResource;
			this.virtualModelResource = virtualModelResource;
			if (virtualModelResource == null) {
				virtualModelURI = null;
			}
			listenTo(virtualModelResource);
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
					if (getAccessedVirtualModelResource().getResourceData() != null) {
						return getAccessedVirtualModelResource().getResourceData().getVirtualModel();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					e.printStackTrace();
				} catch (FlexoException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public void setAccessedVirtualModel(VirtualModel aVirtualModel) {
			this.virtualModelURI = aVirtualModel.getURI();
			this.virtualModelResource = aVirtualModel.getResource();
			listenTo(virtualModelResource);
			fireAccessedVirtualModelChanged();
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
