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
