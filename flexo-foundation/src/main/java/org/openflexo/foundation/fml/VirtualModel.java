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

	public static abstract class VirtualModelImpl extends AbstractVirtualModelImpl<VirtualModel>implements VirtualModel {

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
			File viewPointDirectory = ResourceLocator.retrieveResourceAsFile(((ViewPointResource) viewPoint.getResource()).getDirectory());
			// File virtualModelDirectory = new File(ResourceLocator.retrieveResourceAsFile(((ViewPointResource) viewPoint.getResource())
			// .getDirectory()), baseName);
			// if (!virtualModelDirectory.exists()) {
			// virtualModelDirectory.mkdirs();
			// }
			// File diagramSpecificationXMLFile = new File(virtualModelDirectory, baseName + ".xml");
			ViewPointLibrary viewPointLibrary = viewPoint.getViewPointLibrary();
			VirtualModelResource vmRes = VirtualModelResourceImpl.makeVirtualModelResource(baseName, viewPointDirectory,
					(ViewPointResource) viewPoint.getResource(), viewPointLibrary.getServiceManager());
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

		@Override
		public AbstractVirtualModel<?> getOwner() {
			return getViewPoint();
		}
	}

}
