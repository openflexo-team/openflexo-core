/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.resource;

import java.util.logging.Logger;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Implementation for a {@link FlexoResourceCenter} accessed via an URL
 * 
 * @author sylvain
 * 
 */
// TODO
@ModelEntity
@ImplementationClass(RemoteResourceCenter.RemoteResourceCenterImpl.class)
public interface RemoteResourceCenter extends FlexoResourceCenter<Object> {

	public static abstract class RemoteResourceCenterImpl extends ResourceRepositoryImpl<FlexoResource<?>, Object>
			implements RemoteResourceCenter {

		protected static final Logger logger = Logger.getLogger(RemoteResourceCenter.class.getPackage().getName());

		/*public RemoteResourceCenterImpl(FlexoResourceCenter<Object> resourceCenter) {
			super(resourceCenter, null);
		}*/

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
					PamelaModelFactory factory = new PamelaModelFactory(RemoteResourceCenterEntry.class);
					entry = factory.newInstance(RemoteResourceCenterEntry.class);
					entry.setURL(getURL());
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				}
			}
			return entry;
		}

		/**
		 * Stops the Resource Center (When needed)
		 */
		@Override
		public void stop() {
			logger.warning("STOP method needs to be implemented for RemoteResourceCenters");
		}

		@Override
		public boolean containsArtefact(Object serializationArtefact) {
			// TODO
			return false;
		}

		@Override
		public String relativePath(Object serializationArtefact) {
			// TODO
			return null;
		}

		@Override
		public String getDisplayableStatus() {
			return "[uri=\"" + getDefaultBaseURI() + "\"] with " + getAllResources().size() + " resources";
		}

	}

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

			private boolean isSystem = false;

			@Override
			public RemoteResourceCenter makeResourceCenter(FlexoResourceCenterService rcService) {
				return null;
			}

			@Override
			public boolean isSystemEntry() {
				return false;
			}

			@Override
			public void setIsSystemEntry(boolean isSystemEntry) {
				// Does Nothing
			}
		}

	}

}
