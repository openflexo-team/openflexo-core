/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.prefs.PreferencesContainer;

/**
 * Encodes rc preferences for the whole application
 * 
 * @author sguerin
 * 
 */
@ModelEntity
@ImplementationClass(ResourceCenterPreferences.ResourceCenterPreferencesImpl.class)
@XMLElement(xmlTag = "ResourceCenterPreferences")
public interface ResourceCenterPreferences extends PreferencesContainer {

	@PropertyIdentifier(type = ResourceCenterEntry.class, cardinality = Cardinality.LIST)
	public static final String RESOURCE_CENTER_ENTRIES_KEY = "resourceCenterEntries";

	@Getter(value = RESOURCE_CENTER_ENTRIES_KEY, cardinality = Cardinality.LIST)
	@XMLElement
	public List<ResourceCenterEntry<?>> getResourceCenterEntries();

	@Setter(RESOURCE_CENTER_ENTRIES_KEY)
	public void setResourceCenterEntries(List<ResourceCenterEntry<?>> resourceCenterEntries);

	@Adder(RESOURCE_CENTER_ENTRIES_KEY)
	public void addToResourceCenterEntries(ResourceCenterEntry<?> aResourceCenterEntry);

	@Remover(RESOURCE_CENTER_ENTRIES_KEY)
	public void removeFromResourceCenterEntries(ResourceCenterEntry<?> aResourceCenterEntry);

	public void ensureResourceEntryIsPresent(ResourceCenterEntry<?> entry);

	public void ensureResourceEntryIsNoMorePresent(ResourceCenterEntry<?> entry);

	/**
	 * Return the list all all {@link FlexoResourceCenter} registered for the session
	 * 
	 * @return
	 */
	/*public List<File> getDirectoryResourceCenterList();

	public void assertDirectoryResourceCenterRegistered(File dirRC);

	public void setDirectoryResourceCenterList(List<File> rcList);*/

	public abstract class ResourceCenterPreferencesImpl extends PreferencesContainerImpl implements ResourceCenterPreferences {

		private static final Logger logger = Logger.getLogger(ResourceCenterPreferences.class.getPackage().getName());

		@Override
		public void ensureResourceEntryIsPresent(ResourceCenterEntry<?> entry) {
			System.out.println("Tiens, je me rajoute ca: " + entry);
			Thread.dumpStack();
			System.out.println("J'ai deja: " + getResourceCenterEntries());
			System.out.println("getResourceCenterEntries().contains(entry)=" + getResourceCenterEntries().contains(entry));

			if (!getResourceCenterEntries().contains(entry)) {
				addToResourceCenterEntries(entry);
			}
		}

		@Override
		public void ensureResourceEntryIsNoMorePresent(ResourceCenterEntry<?> entry) {
			while (getResourceCenterEntries().contains(entry)) {
				removeFromResourceCenterEntries(entry);
			}

		}

	}

}
