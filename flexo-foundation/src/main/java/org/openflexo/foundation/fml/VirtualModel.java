/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2014 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml;

import java.io.File;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceImpl;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.rm.ResourceLocator;

/**
 * A {@link VirtualModel} is the specification of a model which will be instantied in a {@link View} as a set of federated models.
 * 
 * The base modelling element of a {@link VirtualModel} is provided by {@link FlexoConcept} concept.
 * 
 * A {@link VirtualModel} instance contains a set of {@link FlexoConceptInstance}.
 * 
 * A {@link VirtualModel} is itself an {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(VirtualModel.VirtualModelImpl.class)
@XMLElement
public interface VirtualModel extends AbstractVirtualModel<VirtualModel> {

	@PropertyIdentifier(type = ViewPoint.class)
	public static final String VIEW_POINT_KEY = "viewPoint";

	@Override
	@Getter(value = VIEW_POINT_KEY /*, inverse = ViewPoint.VIRTUAL_MODELS_KEY*/)
	public ViewPoint getViewPoint();

	@Setter(VIEW_POINT_KEY)
	public void setViewPoint(ViewPoint aViewPoint);

	public static abstract class VirtualModelImpl extends AbstractVirtualModelImpl<VirtualModel> implements VirtualModel {

		private static final Logger logger = Logger.getLogger(VirtualModel.class.getPackage().getName());

		private ViewPoint viewPoint;
		private VirtualModelBindingModel bindingModel;

		/**
		 * Creates a new VirtualModel on user request<br>
		 * Creates both the resource and the object
		 * 
		 * 
		 * @param baseName
		 * @param viewPoint
		 * @return
		 * @throws SaveResourceException
		 */
		public static VirtualModel newVirtualModel(String baseName, ViewPoint viewPoint) throws SaveResourceException {

			Progress.progress(FlexoLocalization.localizedForKey("create_virtual_model_resource"));
			File virtualModelDirectory = new File(ResourceLocator.retrieveResourceAsFile(((ViewPointResource) viewPoint.getResource())
					.getDirectory()), baseName);
			if (!virtualModelDirectory.exists()) {
				virtualModelDirectory.mkdirs();
			}
			File diagramSpecificationXMLFile = new File(virtualModelDirectory, baseName + ".xml");
			ViewPointLibrary viewPointLibrary = viewPoint.getViewPointLibrary();
			VirtualModelResource vmRes = VirtualModelResourceImpl.makeVirtualModelResource(virtualModelDirectory,
					diagramSpecificationXMLFile, (ViewPointResource) viewPoint.getResource(), viewPointLibrary.getServiceManager());
			Progress.progress(FlexoLocalization.localizedForKey("create_virtual_model_resource_data"));
			VirtualModel virtualModel = vmRes.getFactory().newVirtualModel();
			virtualModel.setViewPoint(viewPoint);
			vmRes.setResourceData(virtualModel);
			virtualModel.setResource(vmRes);
			viewPoint.addToVirtualModels(virtualModel);
			Progress.progress(FlexoLocalization.localizedForKey("save_virtual_model_resource"));
			virtualModel.getResource().save(null);

			return virtualModel;
		}

		// Used during deserialization, do not use it
		public VirtualModelImpl() {
			super();
		}

		/**
		 * Creates a new VirtualModel in supplied viewpoint
		 * 
		 * @param viewPoint
		 */
		public VirtualModelImpl(ViewPoint viewPoint) {
			this();
			setViewPoint(viewPoint);
		}

		@Override
		public ViewPoint getViewPoint() {
			return viewPoint;
		}

		@Override
		public void setViewPoint(ViewPoint viewPoint) {
			if (this.viewPoint != viewPoint) {
				ViewPoint oldViewPoint = this.viewPoint;
				this.viewPoint = viewPoint;
				// updateBindingModel();
				getPropertyChangeSupport().firePropertyChange(VIEW_POINT_KEY, oldViewPoint, viewPoint);
			}
		}

		@Override
		public VirtualModelBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new VirtualModelBindingModel(this);
				// createBindingModel();
			}
			return bindingModel;
		}

		@Override
		public boolean delete(Object... context) {
			if (bindingModel != null) {
				bindingModel.delete();
			}
			return super.delete();
		}

	}

}
