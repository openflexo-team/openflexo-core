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
import java.util.ArrayList;
import java.util.List;

import org.openflexo.AdvancedPrefs;
import org.openflexo.ApplicationContext;
import org.openflexo.GeneralPreferences;
import org.openflexo.ResourceCenterPreferences;
import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.resource.FlexoFileNotFoundException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.resource.PamelaResourceImpl;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.module.Module;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.IProgress;

/**
 * This is the {@link FlexoResource} encoding the preferences of the application (see {@link FlexoPreferences})
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FlexoPreferencesResource.FlexoPreferencesResourceImpl.class)
@XMLElement
public interface FlexoPreferencesResource extends PamelaResource<FlexoPreferences, FlexoPreferencesFactory> {

	public FlexoPreferences getFlexoPreferences();

	/**
	 * Default implementation for {@link FlexoPreferencesResource}
	 * 
	 * 
	 * @author Sylvain
	 * 
	 */
	public static abstract class FlexoPreferencesResourceImpl extends PamelaResourceImpl<FlexoPreferences, FlexoPreferencesFactory>
			implements FlexoPreferencesResource {

		private static final String FLEXO_PREFS_FILE_NAME = "Flexo.prefs";

		public static FlexoPreferencesResource makePreferencesResource(ApplicationContext applicationContext) {
			try {
				ModelFactory resourceFactory = new ModelFactory(ModelContextLibrary.getCompoundModelContext( 
						FileFlexoIODelegate.class,FlexoPreferencesResource.class));
				FlexoPreferencesResourceImpl returned = (FlexoPreferencesResourceImpl) resourceFactory
						.newInstance(FlexoPreferencesResource.class);

				File preferencesFile = new File(FileUtils.getApplicationDataDirectory(), FLEXO_PREFS_FILE_NAME);
				returned.setName("OpenflexoPreferences");
				returned.setURI("http://www.openflexo.org/OpenflexoPreferences");
				
				returned.setFlexoIODelegate(FileFlexoIODelegateImpl.makeFileFlexoIODelegate(preferencesFile, resourceFactory));
				//returned.setFile(preferencesFile);
				
				returned.setFactory(makePreferencesFactory(returned, applicationContext));
				returned.setServiceManager(applicationContext);

				if (preferencesFile.exists()) {
					returned.loadResourceData(null);
				} else {
					FlexoPreferences newFlexoPreferences = returned.getFactory().newInstance(FlexoPreferences.class);
					returned.setResourceData(newFlexoPreferences);
					returned.save(null);
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

		private static FlexoPreferencesFactory makePreferencesFactory(FlexoPreferencesResource resource,
				ApplicationContext applicationContext) throws ModelDefinitionException {
			List<Class<?>> classes = new ArrayList<Class<?>>();
			classes.add(FlexoPreferences.class);
			classes.add(GeneralPreferences.class);
			classes.add(AdvancedPrefs.class);
			classes.add(ResourceCenterPreferences.class);
			for (Module<?> m : applicationContext.getModuleLoader().getKnownModules()) {
				classes.add(m.getPreferencesClass());
			}
			return new FlexoPreferencesFactory(resource, ModelContextLibrary.getCompoundModelContext(classes.toArray(new Class<?>[classes
					.size()])));
		}

		@Override
		public FlexoPreferences getFlexoPreferences() {
			return (FlexoPreferences) getLoadedResourceData();
		}

		@Override
		public FlexoPreferences loadResourceData(IProgress progress) throws FlexoFileNotFoundException, IOFlexoException,
				InvalidXMLException, InconsistentDataException, InvalidModelDefinitionException {
			try {
				return super.loadResourceData(progress);
			} catch (InvalidXMLException e) {

				// Preferences file is not readable, perhaps this is because it is an old version of Openflexo
				// Creates it from scratch

				FlexoPreferences prefs = getFactory().newInstance(FlexoPreferences.class);
				GeneralPreferences generalPrefs = getFactory().newInstance(GeneralPreferences.class);
				prefs.addToContents(generalPrefs);
				AdvancedPrefs advancedPrefs = getFactory().newInstance(AdvancedPrefs.class);
				prefs.addToContents(advancedPrefs);
				setResourceData(prefs);

				try {
					save(null);
				} catch (SaveResourceException e2) {
					// TODO Auto-generated catch block
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
