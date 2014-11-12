/*
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

package org.openflexo.rm;

import java.awt.Image;
import java.awt.Window;
import java.beans.PropertyChangeSupport;

import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.toolbox.HasPropertyChangeSupport;
import org.openflexo.toolbox.ImageIconResource;


public class ConflictedResourceEditor implements HasPropertyChangeSupport{
	
	private final PropertyChangeSupport _pcSupport;
	
	private Window owner;
	
	private final ResourceConsistencyService service;
	
	private ConflictedResourceSet resources;
	
	private String errorMessage;
	
	private Image image;
	
	private final Image conflictImage =  new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/resource_conflict.png")).getImage();
	
	private final Image validImage =  new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/resource_conflict_ok.png")).getImage();
	
	
	public Window getOwner() {
		return owner;
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}
	
	public ConflictedResourceEditor(ConflictedResourceSet resources, ResourceConsistencyService service){
		 _pcSupport = new PropertyChangeSupport(this);
		this.service = service;
		this.resources =resources;
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
		if(image==null || !isValid()){
			image = conflictImage;
		}else {
			image = validImage;
		}
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
	
	public String getLocation(FlexoResource<?> resource){
		if(resource!=null && resource.getFlexoIODelegate() instanceof FileFlexoIODelegate){
			FileFlexoIODelegate delegate = (FileFlexoIODelegate)(resource.getFlexoIODelegate());
			return delegate.getFile().getAbsolutePath();
		}
		return "";
	}
	
	public void update(){ 
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
		for(FlexoResource<?> resource : resources.getConflictedResources()){
			if(!service.multipleResourcesWithSameURI(resource)){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}

	public String getNumberOfConflicts() {
		return Integer.toString(service.getNumberOfConflicts());
	}
	
	public String getIndexOfConflicts() {
		return Integer.toString(service.getConflictedResourceSets().indexOf(resources)+1);
	}
	
}
	
