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

import java.awt.Window;
import java.beans.PropertyChangeSupport;
import java.io.File;

import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.MissingFlexoResource;
import org.openflexo.toolbox.HasPropertyChangeSupport;


public class ResourceMissingEditor implements HasPropertyChangeSupport{
	
	private final PropertyChangeSupport _pcSupport;
	
	private Window owner;
	
	private final ResourceConsistencyService service;
	
	private String errorMessage;
	
	private MissingFlexoResource missingResource;
	
	private File missingFile;
	
	public File getMissingFile() {
		return missingFile;
	}

	public void setMissingFile(File missingFile) {
		this.missingFile = missingFile;
	}
	
	public Window getOwner() {
		return owner;
	}

	public void setOwner(Window owner) {
		this.owner = owner;
	}
	
	public ResourceMissingEditor(MissingFlexoResource missingResource, ResourceConsistencyService service){
		 _pcSupport = new PropertyChangeSupport(this);
		this.service = service;
		this.missingResource = missingResource;
	}
	

    @Override
    public String getDeletedProperty() {
        return null;
    }

    @Override
    public PropertyChangeSupport getPropertyChangeSupport() {
        return _pcSupport;
    }

	public MissingFlexoResource getMissingResource() {
		return missingResource;
	}

	public void setMissingResource(MissingFlexoResource missingResource) {
		this.missingResource = missingResource;
	}
	
	public void load(){
		DirectoryResourceCenter newRC = new DirectoryResourceCenter(getMissingFile().getParentFile());
		service.getServiceManager().getResourceCenterService().addToResourceCenters(newRC);
		
	}
}
	
