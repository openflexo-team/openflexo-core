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

import java.io.File;
import java.io.IOException;

import org.openflexo.ApplicationContext;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.resource.FileIODelegate;
import org.openflexo.foundation.resource.FileIODelegate.FileIODelegateImpl;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaXMLSerializableResource;
import org.openflexo.foundation.resource.PamelaXMLSerializableResourceImpl;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FileUtils;

/**
 * This is the {@link FlexoResource} encoding the preferences of the application (see {@link FlexoPreferences})
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoPreferencesResource.FlexoPreferencesResourceImpl.class)
@XMLElement
public interface FlexoPreferencesResource extends PamelaXMLSerializableResource<FlexoPreferences, FlexoPreferencesFactory> {

	public FlexoPreferences getFlexoPreferences();

	/**
	 * Default implementation for {@link FlexoPreferencesResource}
	 * 
	 * 
	 * @author Sylvain
	 * 
	 */
	public static abstract class FlexoPreferencesResourceImpl
			extends PamelaXMLSerializableResourceImpl<FlexoPreferences, FlexoPreferencesFactory> implements FlexoPreferencesResource {

		private static final String FLEXO_PREFS_FILE_NAME = "Flexo.prefs";

		public static FlexoPreferencesResource makePreferencesResource(ApplicationContext applicationContext) {
			try {
				PamelaModelFactory resourceFactory = new PamelaModelFactory(
						PamelaMetaModelLibrary.retrieveMetaModel(FileIODelegate.class, FlexoPreferencesResource.class));
				FlexoPreferencesResourceImpl returned = (FlexoPreferencesResourceImpl) resourceFactory
						.newInstance(FlexoPreferencesResource.class);

				File preferencesFile = new File(FileUtils.getApplicationDataDirectory(), FLEXO_PREFS_FILE_NAME);
				returned.initName("OpenflexoPreferences");
				returned.setURI("http://www.openflexo.org/OpenflexoPreferences");

				returned.setIODelegate(FileIODelegateImpl.makeFileFlexoIODelegate(preferencesFile, resourceFactory));
				// returned.setFile(preferencesFile);

				returned.setFactory(applicationContext.getPreferencesService().makePreferencesFactory(returned, applicationContext));
				// TODO: setResourceCenter()
				returned.setServiceManager(applicationContext);

				if (preferencesFile.exists()) {
					returned.loadResourceData();
				}
				else {
					FlexoPreferences newFlexoPreferences = returned.getFactory().newInstance(FlexoPreferences.class);
					returned.setResourceData(newFlexoPreferences);
					returned.save();
				}
				return returned;
			} catch (ModelDefinitionException e) {
				e.printStackTrace();
			} catch (FlexoFileNotFoundException e) {
				e.printStackTrace();
			} catch (IOFlexoException e) {
				e.printStackTrace();
			} catch (InvalidXMLException e) {
				e.printStackTrace();
			} catch (InconsistentDataException e) {
				e.printStackTrace();
			} catch (InvalidModelDefinitionException e) {
				e.printStackTrace();
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public FlexoPreferences getFlexoPreferences() {
			return getLoadedResourceData();
		}

		@Override
		protected FlexoPreferences performLoad() throws IOException, Exception {
			try {
				return super.performLoad();
			} catch (InvalidXMLException e) {

				// Preferences file is not readable, perhaps this is because it is an old version of Openflexo
				// Creates it from scratch

				FlexoPreferences prefs = getFactory().newInstance(FlexoPreferences.class);
				GeneralPreferences generalPrefs = getFactory().newInstance(GeneralPreferences.class);
				prefs.addToContents(generalPrefs);
				PresentationPreferences presentationPrefs = getFactory().newInstance(PresentationPreferences.class);
				prefs.addToContents(presentationPrefs);
				AdvancedPrefs advancedPrefs = getFactory().newInstance(AdvancedPrefs.class);
				prefs.addToContents(advancedPrefs);
				setResourceData(prefs);

				try {
					save();
				} catch (SaveResourceException e2) {
					e2.printStackTrace();
				}

				return prefs;
			}
		}

		@Override
		public Class<FlexoPreferences> getResourceDataClass() {
			return FlexoPreferences.class;
		}
	}
}
