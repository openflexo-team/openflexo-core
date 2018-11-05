/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.components.widget;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoProjectObject;

/**
 * Widget allowing to select an {@link FlexoProjectObject} (an object located in a {@link FlexoProject})<br>
 * 
 * Defines the scope of a project where to look-up a {@link FlexoProjectObject}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FIBProjectObjectSelector<T extends FlexoObject> extends FIBFlexoObjectSelector<T> {

	static final Logger logger = Logger.getLogger(FIBProjectObjectSelector.class.getPackage().getName());

	private FlexoProject<?> project;

	public FIBProjectObjectSelector(T editedObject) {
		super(editedObject);
	}

	@Override
	public void delete() {
		super.delete();
		project = null;
	}

	public FlexoProject<?> getProject() {
		return project;
	}

	@CustomComponentParameter(name = "project", type = CustomComponentParameter.Type.MANDATORY)
	public void setProject(FlexoProject<?> project) {
		if (this.project != project) {
			FlexoProject<?> oldProject = this.project;
			if (project == null) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Passing null project. If you rely on project this is unlikely to work");
				}
			}
			this.project = project;
			getPropertyChangeSupport().firePropertyChange("project", oldProject, project);
		}
	}

}
