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
package org.openflexo.components;

import java.awt.Window;
import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.openflexo.ApplicationContext;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.model.ModelEntity;
import org.openflexo.prefs.FlexoPreferences;
import org.openflexo.prefs.PreferencesContainer;
import org.openflexo.toolbox.FileResource;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Dialog allowing to edit all {@link Preferences}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class PreferencesDialog extends FIBDialog<FlexoPreferences> {

	static final Logger logger = Logger.getLogger(PreferencesDialog.class.getPackage().getName());

	public static final File PREFERENCES_FIB = new FileResource("Fib/Preferences.fib");

	private static PreferencesDialog dialog;

	public static PreferencesDialog getPreferencesDialog(ApplicationContext applicationContext, Window parent) {
		System.out.println("getPreferencesDialog with " + applicationContext);

		if (dialog == null) {
			dialog = new PreferencesDialog(applicationContext, parent);
		}

		return dialog;
	}

	public static void showPreferencesDialog(ApplicationContext applicationContext, Window parent) {
		System.out.println("showPreferencesDialog with " + applicationContext);

		if (dialog == null) {
			dialog = getPreferencesDialog(applicationContext, parent);
		}
		dialog.showDialog();
	}

	public PreferencesDialog(ApplicationContext applicationContext, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(PREFERENCES_FIB),
				applicationContext.getPreferencesService().getFlexoPreferences(), parent, true, new PreferencesFIBController(FIBLibrary
						.instance().retrieveFIBComponent(PREFERENCES_FIB)));

		setTitle("Preferences");

	}

	public static class PreferencesFIBController extends FlexoFIBController {

		public PreferencesFIBController(FIBComponent component) {
			super(component);
		}

		public File fibForPreference(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			ModelEntity<?> prefsEntity = prefs.getFlexoPreferencesFactory().getModelEntityForInstance(prefs);

			if (prefsEntity != null) {

				// System.out.println("Entity=" + prefsEntity);
				// System.out.println("Class=" + prefsEntity.getImplementedInterface());

				FIBPanel fibDeclaration = prefsEntity.getImplementedInterface().getAnnotation(FIBPanel.class);

				if (fibDeclaration != null) {
					File returned = new FileResource(fibDeclaration.value());
					// System.out.println("Returning " + returned);
					return returned;
				}
			}

			return null;
		}
	}

}
