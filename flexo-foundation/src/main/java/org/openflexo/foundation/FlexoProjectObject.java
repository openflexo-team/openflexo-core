/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;

/**
 * Super class for any object involved in Openflexo and beeing part of a {@link FlexoProject}<br>
 * Provides a direct access to {@link FlexoServiceManager} (and all services) through {@link FlexoProject}<br>
 * Also provides support for storing references to {@link FlexoConceptInstance}
 * 
 * @author sguerin
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoProjectObject.FlexoProjectObjectImpl.class)
public interface FlexoProjectObject extends FlexoObject {

	@PropertyIdentifier(type = FlexoProject.class)
	public static final String PROJECT = "project";

	@Getter(value = PROJECT, ignoreType = true)
	public FlexoProject getProject();

	@Setter(PROJECT)
	public void setProject(FlexoProject project);

	public FlexoServiceManager getServiceManager();

	public abstract class FlexoProjectObjectImpl extends FlexoObjectImpl implements FlexoProjectObject {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(FlexoProjectObject.class.getPackage().getName());

		private FlexoProject project;

		public FlexoProjectObjectImpl() {
			super();
		}

		public FlexoProjectObjectImpl(FlexoProject project) {
			this();
			this.project = project;
		}

		@Override
		public boolean delete(Object... context) {
			project = null;
			return performSuperDelete(context);
		}

		@Override
		public FlexoProject getProject() {
			return project;
		}

		@Override
		public void setProject(FlexoProject project) {
			this.project = project;
		}

		// TODO: Should be refactored with injectors
		@Override
		public FlexoServiceManager getServiceManager() {
			if (getProject() != null) {
				return getProject().getServiceManager();
			}
			return null;
		}

	}
}
