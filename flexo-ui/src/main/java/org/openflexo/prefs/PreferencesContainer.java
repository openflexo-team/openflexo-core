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

package org.openflexo.prefs;

import java.util.List;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;

/**
 * This class represents a logical container of some preferences regarding a particular functional aspect (eg a module)
 * 
 * @author sguerin
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(PreferencesContainer.PreferencesContainerImpl.class)
public interface PreferencesContainer extends FlexoObject {

	@PropertyIdentifier(type = PreferencesContainer.class, cardinality = Cardinality.LIST)
	public static final String CONTENTS = "contents";
	@PropertyIdentifier(type = PreferencesContainer.class)
	public static final String CONTAINER = "container";
	@PropertyIdentifier(type = PreferencesService.class)
	public static final String PREFERENCES_SERVICE = "preferencesService";
	@PropertyIdentifier(type = PreferenceProperty.class, cardinality = Cardinality.LIST)
	String CUSTOM_PROPERTIES_KEY = "customProperties";

	@Getter(value = CONTAINER, inverse = CONTENTS)
	public PreferencesContainer getContainer();

	@Setter(CONTAINER)
	public void setContainer(PreferencesContainer aContainer);

	@Getter(value = CONTENTS, cardinality = Cardinality.LIST, inverse = CONTAINER)
	@XMLElement
	public List<PreferencesContainer> getContents();

	@Setter(CONTENTS)
	public void setContents(List<PreferencesContainer> someContents);

	@Adder(CONTENTS)
	public void addToContents(PreferencesContainer aContent);

	@Remover(CONTENTS)
	public void removeFromContents(PreferencesContainer aContent);

	@Getter(value = CUSTOM_PROPERTIES_KEY, cardinality = Cardinality.LIST, inverse = PreferenceProperty.OWNER_KEY)
	@XMLElement
	public List<PreferenceProperty> getCustomProperties();

	@Setter(CUSTOM_PROPERTIES_KEY)
	public void setCustomProperties(List<PreferenceProperty> customProperties);

	@Adder(CUSTOM_PROPERTIES_KEY)
	public void addToCustomProperties(PreferenceProperty aCustomPropertie);

	@Remover(CUSTOM_PROPERTIES_KEY)
	public void removeFromCustomProperties(PreferenceProperty aCustomPropertie);

	@Finder(attribute = FlexoProperty.NAME_KEY, collection = CUSTOM_PROPERTIES_KEY)
	public PreferenceProperty getPropertyNamed(String name);

	public <P extends PreferencesContainer> P getPreferences(Class<P> containerType);

	public FlexoPreferencesFactory getFlexoPreferencesFactory();

	public void setFlexoPreferencesFactory(FlexoPreferencesFactory factory);

	public PreferenceProperty assertProperty(String propertyName);

	public String getName();

	@Getter(value = PREFERENCES_SERVICE, ignoreType = true)
	public PreferencesService getPreferencesService();

	@Setter(PREFERENCES_SERVICE)
	public void setPreferencesService(PreferencesService preferencesService);

	public static abstract class PreferencesContainerImpl extends FlexoObjectImpl implements PreferencesContainer {

		private FlexoPreferencesFactory factory;

		public boolean hasPropertyNamed(String name) {
			return getPropertyNamed(name) != null;
		}

		@Override
		public PreferenceProperty getPropertyNamed(String name) {
			if (name == null) {
				for (PreferenceProperty p : getCustomProperties()) {
					if (p.getName() == null) {
						return p;
					}
				}
			}
			else {
				for (PreferenceProperty p : getCustomProperties()) {
					if (name.equals(p.getName())) {
						return p;
					}
				}
			}
			return null;
		}

		@Override
		public PreferenceProperty assertProperty(String propertyName) {
			PreferenceProperty p = getPropertyNamed(propertyName);
			if (p == null) {
				p = getFlexoPreferencesFactory().newInstance(PreferenceProperty.class);
				p.setName(propertyName);
				addToCustomProperties(p);
				return p;
			}
			return p;
		}

		@Override
		public FlexoPreferencesFactory getFlexoPreferencesFactory() {
			return factory;
		}

		@Override
		public void setFlexoPreferencesFactory(FlexoPreferencesFactory factory) {
			this.factory = factory;
		}

		@Override
		public <P extends PreferencesContainer> P getPreferences(Class<P> containerType) {
			for (PreferencesContainer c : getContents()) {
				if (containerType.isAssignableFrom(c.getClass())) {
					return (P) c;
				}
				P returned = c.getPreferences(containerType);
				if (returned != null) {
					return returned;
				}
			}
			return null;
		}

		@Override
		public String getName() {
			org.openflexo.pamela.model.ModelEntity e = getFlexoPreferencesFactory().getModelEntityForInstance(this);
			return e.getImplementedInterface().getSimpleName();
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getPreferencesService() != null) {
				return getPreferencesService().getServiceManager();
			}
			return super.getServiceManager();
		}

	}
}
