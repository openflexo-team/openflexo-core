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

package org.openflexo.rm;

import java.awt.Image;
import java.awt.Window;
import java.beans.PropertyChangeSupport;

import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.icon.ImageIconResource;
import org.openflexo.toolbox.HasPropertyChangeSupport;

public class ConflictedResourceEditor implements HasPropertyChangeSupport {

	private final PropertyChangeSupport _pcSupport;

	private Window owner;

	private final ResourceConsistencyService service;

	private ConflictedResourceSet resources;

	private String errorMessage;

	private Image image;

	private final Image conflictImage = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/resource_conflict.png"))
			.getImage();

	private final Image validImage = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/resource_conflict_ok.png"))
			.getImage();

	public Window getOwner() {
		return owner;
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}

	public ConflictedResourceEditor(ConflictedResourceSet resources, ResourceConsistencyService service) {
		_pcSupport = new PropertyChangeSupport(this);
		this.service = service;
		this.resources = resources;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return _pcSupport;
	}

	public Image getImage() {
		if (image == null || !isValid()) {
			image = conflictImage;
		}
		else {
			image = validImage;
		}
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public String getLocation(FlexoResource<?> resource) {
		if (resource != null && resource.getIODelegate() instanceof FileIODelegate) {
			FileIODelegate delegate = (FileIODelegate) (resource.getIODelegate());
			return delegate.getFile().getAbsolutePath();
		}
		return "";
	}

	public void update() {
		getPropertyChangeSupport().firePropertyChange("image", null, null);
		getPropertyChangeSupport().firePropertyChange("isValid()", null, isValid());
	}

	public ConflictedResourceSet getResources() {
		return resources;
	}

	public void setResources(ConflictedResourceSet resources) {
		this.resources = resources;
	}

	public String getErrorMessage() {
		isValid();
		return errorMessage;
	}

	public boolean isValid() {
		for (FlexoResource<?> resource : resources.getConflictedResources()) {
			if (!service.multipleResourcesWithSameURI(resource)) {
				return true;
			}
			else {
				return false;
			}
		}
		return true;
	}

	public String getNumberOfConflicts() {
		return Integer.toString(service.getNumberOfConflicts());
	}

	public String getIndexOfConflicts() {
		return Integer.toString(service.getConflictedResourceSets().indexOf(resources) + 1);
	}

}
