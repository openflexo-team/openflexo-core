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
package org.openflexo.foundation.resource;

import java.util.logging.Logger;

import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Implementation for a {@link FlexoResourceCenter} accessed via an URL
 * 
 * @author sylvain
 * 
 */
// TODO
public abstract class RemoteResourceCenter extends ResourceRepository<FlexoResource<?>> implements FlexoResourceCenter {

	protected static final Logger logger = Logger.getLogger(RemoteResourceCenter.class.getPackage().getName());

	@ModelEntity
	@XMLElement
	public static interface RemoteResourceCenterEntry extends ResourceCenterEntry<RemoteResourceCenter> {
		@PropertyIdentifier(type = String.class)
		public static final String URL_KEY = "url";

		@Getter(URL_KEY)
		@XMLAttribute
		public String getURL();

		@Setter(URL_KEY)
		public void setURL(String aURL);

		@Implementation
		public static abstract class RemoteResourceCenterEntryImpl implements RemoteResourceCenterEntry {
			@Override
			public RemoteResourceCenter makeResourceCenter() {
				// TODO Auto-generated method stub
				return null;
			}
		}

	}

	public RemoteResourceCenter(Object owner) {
		super(owner);
	}

	public String getURL() {
		return null;
	}

	public void setURL(String aURL) {

	}

	private RemoteResourceCenterEntry entry;

	@Override
	public RemoteResourceCenterEntry getResourceCenterEntry() {
		if (entry == null) {
			try {
				ModelFactory factory = new ModelFactory(RemoteResourceCenterEntry.class);
				entry = factory.newInstance(RemoteResourceCenterEntry.class);
				entry.setURL(getURL());
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			}
		}
		return entry;
	}

}
