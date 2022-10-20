/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.view.controller.model;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProjectObject;

/**
 * Represents an edited location in edition navigable history
 * 
 * A {@link Location} is mainly composed of an object (which is not necessary the master object) and perspective
 * 
 * @author sylvain
 *
 */
public class Location extends ControllerModelObject {
	private final FlexoObject object;
	private final FlexoPerspective perspective;
	private final FlexoEditor editor;

	public Location(FlexoEditor context, FlexoObject object, FlexoPerspective perspective) {
		super();
		this.editor = context;
		this.object = object;
		this.perspective = perspective;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (editor == null ? 0 : editor.hashCode());
		result = prime * result + (object == null ? 0 : object.hashCode());
		result = prime * result + (perspective == null ? 0 : perspective.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return (object != null ? object : "No object")
				+ " - "
				+ (perspective != null ? perspective.getName() : " No perspective" + " - "
						+ (editor != null ? editor.getProject() : "No project"));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Location other = (Location) obj;
		if (editor == null) {
			if (other.editor != null) {
				return false;
			}
		} else if (!editor.equals(other.editor)) {
			return false;
		}
		if (object == null) {
			if (other.object != null) {
				return false;
			}
		} else if (!object.equals(other.object)) {
			return false;
		}
		if (perspective == null) {
			if (other.perspective != null) {
				return false;
			}
		} else if (!perspective.equals(other.perspective)) {
			return false;
		}
		return true;
	}

	public FlexoObject getObject() {
		return object;
	}

	public FlexoObject getMasterObject() {
		return getPerspective().getRepresentableMasterObject(getObject());
	}
	
	public boolean isMasterLocation() {
		return getObject() == getMasterObject();
	}
	
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	public FlexoEditor getEditor() {
		return editor;
	}

	public boolean isEditable() {
		if (getObject() instanceof FlexoProjectObject) {
			return ((FlexoProjectObject) getObject()).getProject() == getEditor().getProject()
					|| ((FlexoProjectObject) getObject()).getProject() == null;
		}
		return true;
	}

}
