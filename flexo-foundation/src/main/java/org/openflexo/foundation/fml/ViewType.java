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

package org.openflexo.foundation.fml;

import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * Represent the type of a View conform to a given ViewPoint
 * 
 * @author sylvain
 * 
 */
public class ViewType extends VirtualModelInstanceType {

	public static ViewType UNDEFINED_VIEW_TYPE = new ViewType((ViewPoint) null);

	public ViewType(ViewPoint aViewPoint) {
		super(aViewPoint);
	}

	protected ViewType(String viewpointURI) {
		super(viewpointURI);
	}

	public ViewPoint getViewPoint() {
		return (ViewPoint) getVirtualModel();
	}

	@Override
	public Class<?> getBaseClass() {
		return View.class;
	}

	public static ViewType getViewType(ViewPoint viewPoint) {
		if (viewPoint != null) {
			return viewPoint.getViewType();
		}
		else {
			return null;
		}
	}

	/**
	 * Factory for FlexoConceptInstanceType instances
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/CustomType/ViewTypeFactory.fib")
	public static class ViewTypeFactory extends TechnologyAdapterTypeFactory<ViewType> {

		public ViewTypeFactory(FMLRTTechnologyAdapter technologyAdapter) {
			super(technologyAdapter);
		}

		@Override
		public Class<ViewType> getCustomType() {
			return ViewType.class;
		}

		@Override
		public ViewType makeCustomType(String configuration) {

			ViewPoint viewPoint = null;

			if (configuration != null) {
				viewPoint = getTechnologyAdapter().getTechnologyAdapterService().getServiceManager().getViewPointLibrary()
						.getViewPoint(configuration);
			}
			else {
				viewPoint = getViewPointType();
			}

			if (viewPoint != null) {
				return getViewType(viewPoint);
			}
			else {
				// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
				// if FlexoConcept might be resolved later
				return new ViewType(configuration);
			}
		}

		private ViewPoint viewPointType;

		public ViewPoint getViewPointType() {
			return viewPointType;
		}

		public void setViewPointType(ViewPoint viewPointType) {
			if (viewPointType != this.viewPointType) {
				AbstractVirtualModel<?> oldVirtualModelType = this.viewPointType;
				this.viewPointType = viewPointType;
				getPropertyChangeSupport().firePropertyChange("viewPointType", oldVirtualModelType, viewPointType);
			}
		}

		@Override
		public String toString() {
			return "Instance of VirtualModel";
		}

		@Override
		public void configureFactory(ViewType type) {
			if (type != null) {
				setViewPointType(type.getViewPoint());
			}
		}
	}

}
