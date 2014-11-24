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

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.resource.FlexoResource;

public class ConflictedResourceSet {

	private List<FlexoResource<?>> conflictedResources;
	
	private String commonUri;

	public List<FlexoResource<?>> getConflictedResources() {
		return conflictedResources;
	}

	public void setConflictedResources(List<FlexoResource<?>> conflictedResources) {
		this.conflictedResources = conflictedResources;
	}

	public ConflictedResourceSet(List<FlexoResource<?>> conflictedResources) {
		this.conflictedResources = conflictedResources;
		commonUri = conflictedResources.get(0).getURI();
	}
	
	public ConflictedResourceSet(FlexoResource<?> firstResource) {
		conflictedResources = new ArrayList<FlexoResource<?>>();
		conflictedResources.add(firstResource);
		commonUri = firstResource.getURI();
	}
	
	public ConflictedResourceSet() {
		conflictedResources = new ArrayList<FlexoResource<?>>();
	}

	public String getCommonUri() {
		return commonUri;
	}
	
}
