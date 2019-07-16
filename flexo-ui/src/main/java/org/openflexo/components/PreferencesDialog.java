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

import java.awt.Rectangle;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.ApplicationContext;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.utils.JFIBPreferences;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.ImageIconResource;
import org.openflexo.module.Module;
import org.openflexo.prefs.FlexoPreferences;
import org.openflexo.prefs.ModulePreferences;
import org.openflexo.prefs.Preferences;
import org.openflexo.prefs.PreferencesContainer;
import org.openflexo.prefs.PreferencesService;
import org.openflexo.prefs.TechnologyAdapterPreferences;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.swing.ComponentBoundSaver;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.TechnologyAdapterController;

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
		if (dialog == null) {
			dialog = new PreferencesDialog(applicationContext, parent);
		}

		return dialog;
	}

	public static void showPreferencesDialog(ApplicationContext applicationContext, Window parent) {
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

		getController().applicationContext = applicationContext;

		setTitle("Preferences");

		setBounds(JFIBPreferences.getPreferencesBounds());
		new ComponentBoundSaver(this) {

			@Override
			public void saveBounds(Rectangle bounds) {
				JFIBPreferences.setPreferencesBounds(bounds);
			}
		};

		getController().objectAddedToSelection(applicationContext.getPreferencesService().getFlexoPreferences());

	}

	@Override
	public PreferencesFIBController getController() {
		return (PreferencesFIBController) super.getController();
	}

	public static class PreferencesFIBController extends FlexoFIBController {

		private ApplicationContext applicationContext;

		public PreferencesFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		private final Map<PreferencesContainer, Resource> fibPanels = new HashMap<>();
		private final Map<PreferencesContainer, ImageIcon> smallIcons = new HashMap<>();
		private final Map<PreferencesContainer, ImageIcon> bigIcons = new HashMap<>();
		private final Map<PreferencesContainer, String> shortNames = new HashMap<>();
		private final Map<PreferencesContainer, String> longNames = new HashMap<>();

		public ApplicationContext getApplicationContext() {
			return applicationContext;
		}

		public void setApplicationContext(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@NotificationUnsafe
		public Resource fibForPreferences(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			Resource returned = fibPanels.get(prefs);

			if (returned == null) {
				Preferences prefsMD = getPreferencesMetaData(prefs);
				if (prefsMD == null) {
					return null;
				}
				returned = ResourceLocator.locateResource(prefsMD.FIBPanel());
				if (returned != null) {
					fibPanels.put(prefs, returned);
				}
			}

			return returned;
		}

		@NotificationUnsafe
		public String shortNameForPreferences(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			String returned = shortNames.get(prefs);

			if (returned == null) {
				Preferences prefsMD = getPreferencesMetaData(prefs);
				if (prefsMD != null && StringUtils.isNotEmpty(prefsMD.shortName())) {
					returned = prefsMD.shortName();
					if (returned != null) {
						shortNames.put(prefs, returned);
					}
				}
			}

			if (returned == null) {
				returned = prefs.getName();
			}

			return returned;

		}

		@NotificationUnsafe
		public String longNameForPreferences(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			String returned = longNames.get(prefs);

			if (returned == null) {
				Preferences prefsMD = getPreferencesMetaData(prefs);
				if (prefsMD != null && StringUtils.isNotEmpty(prefsMD.longName())) {
					returned = prefsMD.longName();
					if (returned != null) {
						longNames.put(prefs, returned);
					}
				}
			}

			if (returned == null) {
				returned = prefs.getName();
			}

			return returned;

		}

		@NotificationUnsafe
		public ImageIcon iconForPreferences(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			if (prefs instanceof ModulePreferences) {
				Module<?> module = ((ModulePreferences<?>) prefs).getModule();
				if (module != null) {
					return module.getSmallIcon();
				}
			}

			if (prefs instanceof TechnologyAdapterPreferences) {
				TechnologyAdapter ta = ((TechnologyAdapterPreferences<?>) prefs).getTechnologyAdapter();
				TechnologyAdapterController<?> tac = getApplicationContext().getTechnologyAdapterControllerService()
						.getTechnologyAdapterController(ta);
				return tac.getTechnologyIcon();
			}

			ImageIcon returned = smallIcons.get(prefs);

			if (returned == null) {
				Preferences prefsMD = getPreferencesMetaData(prefs);
				if (prefsMD != null && StringUtils.isNotEmpty(prefsMD.smallIcon())) {
					returned = new ImageIconResource(ResourceLocator.locateResource(prefsMD.smallIcon()));
					if (returned != null) {
						smallIcons.put(prefs, returned);
					}
				}
			}

			if (returned == null) {
				returned = IconLibrary.OPENFLEXO_NOTEXT_16;
			}

			return returned;

		}

		@NotificationUnsafe
		public ImageIcon bigIconForPreferences(PreferencesContainer prefs) {

			if (prefs == null) {
				return null;
			}

			if (prefs instanceof ModulePreferences) {
				Module<?> module = ((ModulePreferences<?>) prefs).getModule();
				if (module != null) {
					return module.getBigIcon();
				}
			}

			if (prefs instanceof TechnologyAdapterPreferences) {
				TechnologyAdapter ta = ((TechnologyAdapterPreferences<?>) prefs).getTechnologyAdapter();
				TechnologyAdapterController<?> tac = getApplicationContext().getTechnologyAdapterControllerService()
						.getTechnologyAdapterController(ta);
				return tac.getTechnologyBigIcon();
			}

			ImageIcon returned = bigIcons.get(prefs);

			if (returned == null) {
				Preferences prefsMD = getPreferencesMetaData(prefs);
				if (prefsMD != null && StringUtils.isNotEmpty(prefsMD.smallIcon())) {
					returned = new ImageIconResource(ResourceLocator.locateResource(prefsMD.bigIcon()));
					if (returned != null) {
						bigIcons.put(prefs, returned);
					}
				}
			}

			if (returned == null) {
				returned = IconLibrary.OPENFLEXO_NOTEXT_64;
			}

			return returned;

		}

		private PreferencesService getPreferencesService() {
			return ((FlexoPreferences) getDataObject()).getPreferencesService();
		}

		protected Preferences getPreferencesMetaData(PreferencesContainer prefs) {
			Preferences returned = prefs.getClass().getAnnotation(Preferences.class);
			if (returned == null) {
				for (java.lang.reflect.Type t : prefs.getClass().getGenericInterfaces()) {
					if (t instanceof Class) {
						returned = ((Class<?>) t).getAnnotation(Preferences.class);
						if (returned != null) {
							return returned;
						}
					}
				}

			}
			return returned;
		}

		public void apply() {
			getPreferencesService().applyPreferences();
		}

		public void revert() {
			getPreferencesService().revertToSaved();
		}

		public void save() {
			getPreferencesService().savePreferences();
		}
	}

}
