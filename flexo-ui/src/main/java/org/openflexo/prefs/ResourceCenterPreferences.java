/**
 * 
 * Copyright (c) 2014-2016 , Openflexo
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

package org.openflexo.prefs;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FlexoResourceCenter.ResourceCenterEntry;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.foundation.resource.FlexoResourceCenterService;

/**
 * Preferences encoding all user declared resource centers the ones that are declared at the "system-level" (i.e. globally or from the
 * classpath) are not stored in user preferences
 * 
 * @author sguerin, xtof
 * 
 */
@ModelEntity
@ImplementationClass(ResourceCenterPreferences.ResourceCenterPreferencesImpl.class)
@XMLElement
@Preferences(
		shortName = "Resource centers",
		longName = "Resource Centers Preferences",
		FIBPanel = "Fib/Prefs/ResourceCenterPreferences.fib",
		smallIcon = "Icons/Common/ResourceCenter.png",
		bigIcon = "Icons/Common/ResourceCenter_64x64.png")
public interface ResourceCenterPreferences extends ServicePreferences<FlexoResourceCenterService> {

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
	 * Implementation Class
	 * 
	 * @author sylvain
	 *
	 */
	public abstract class ResourceCenterPreferencesImpl extends PreferencesContainerImpl implements ResourceCenterPreferences {

		private static final Logger logger = Logger.getLogger(ResourceCenterPreferences.class.getPackage().getName());

		@Override
		public void addToResourceCenterEntries(ResourceCenterEntry<?> aResourceCenterEntry) {
			if (!aResourceCenterEntry.isSystemEntry()) {
				this.performSuperAdder(RESOURCE_CENTER_ENTRIES_KEY, aResourceCenterEntry);
			}
		}

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
