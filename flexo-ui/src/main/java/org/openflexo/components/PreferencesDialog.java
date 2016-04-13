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

package org.openflexo.components;

import java.awt.Window;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.openflexo.ApplicationContext;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.prefs.FlexoPreferences;
import org.openflexo.prefs.PreferencesContainer;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Dialog allowing to edit all {@link Preferences}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class PreferencesDialog extends JFIBDialog<FlexoPreferences> {

	static final Logger logger = Logger.getLogger(PreferencesDialog.class.getPackage().getName());

	public static final Resource PREFERENCES_FIB = ResourceLocator.locateResource("Fib/Preferences.fib");

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

		super(applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(PREFERENCES_FIB, true),
				applicationContext.getPreferencesService().getFlexoPreferences(), parent, true,
				new PreferencesFIBController(
						applicationContext.getApplicationFIBLibraryService().retrieveFIBComponent(PREFERENCES_FIB, true),
						SwingViewFactory.INSTANCE));

		setTitle("Preferences");

	}

	public static class PreferencesFIBController extends FlexoFIBController {

		public PreferencesFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public Resource fibForPreference(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			if (getFIBPanelForObject(prefs) != null) {
				return getFIBPanelForObject(prefs);
			}

			return null;

			/*ModelEntity<?> prefsEntity = prefs.getFlexoPreferencesFactory().getModelEntityForInstance(prefs);
			
			if (prefsEntity != null) {
			
				// System.out.println("Entity=" + prefsEntity);
				// System.out.println("Class=" + prefsEntity.getImplementedInterface());
			
				FIBPanel fibDeclaration = prefsEntity.getImplementedInterface().getAnnotation(FIBPanel.class);
			
				if (fibDeclaration != null) {
					Resource returned = ResourceLocator.locateResource(fibDeclaration.value());
					// System.out.println("Returning " + returned);
					return returned;
				}
			}
			
			return null;*/
		}
	}

}
