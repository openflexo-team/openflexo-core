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

package org.openflexo.foundation.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProjectObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * A {@link ViewObject} is an abstract run-time concept (instance) for an object "living" in a {@link View} (instanceof a {@link ViewPoint})
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ViewObject.ViewObjectImpl.class)
public interface ViewObject extends FlexoObject  {

	@PropertyIdentifier(type = View.class)
	public static final String VIEW_KEY = "view";

	/**
	 * Return the {@link View} where this object is declared and living
	 * 
	 * @return
	 */
	@Getter(VIEW_KEY)
	public abstract View getView();

	/**
	 * Return the {@link View} where this object is declared and living
	 * 
	 * @return
	 */
	@Setter(VIEW_KEY)
	public void setView(View view);

	/** 
	 * Returns FlexoResourceCenter that contains the ViewResource containing this ViewObject
	 * 
	 * @return
	 */
	public FlexoResourceCenter<?> getResourceCenter();

	public FlexoServiceManager getServiceManager();
	
	
	public abstract class ViewObjectImpl extends FlexoObjectImpl implements ViewObject {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(ViewObject.class.getPackage().getName());

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getResourceCenter() != null) {
				return getResourceCenter().getServiceManager();
			}
			else 
				return null;
		}

		@Override
		public boolean delete(Object... context) {
			return performSuperDelete(context);
		}

	}


}
