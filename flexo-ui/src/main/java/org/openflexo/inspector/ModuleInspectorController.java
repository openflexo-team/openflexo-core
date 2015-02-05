/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.inspector;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBContainer;
import org.openflexo.fib.model.FIBModelFactory;
import org.openflexo.fib.model.FIBWidget;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.task.Progress;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.FlexoController;

/**
 * Represents the controller for all inspectors managed in the context of a module<br>
 * It is connected with one or many FIBInspectorPanels sharing the same selection. In particular, manage the inspector dialog of the module.
 * 
 * @author sylvain
 * 
 */
public class ModuleInspectorController extends Observable implements Observer {

	private static final String CONTROLLER_EDITABLE_BINDING = "controller.flexoController.isEditable(data)";

	static final Logger logger = Logger.getLogger(ModuleInspectorController.class.getPackage().getName());

	private final FIBInspectorDialog inspectorDialog;

	private final FlexoController flexoController;

	private final Map<FlexoConcept, FIBInspector> flexoConceptInspectors;
	private final Map<Class<?>, FIBInspector> inspectors;

	private FIBInspector currentInspector = null;
	private Object currentInspectedObject = null;

	public static FIBModelFactory INSPECTOR_FACTORY;

	static {
		try {
			INSPECTOR_FACTORY = new FIBModelFactory(FIBInspector.class);
		} catch (ModelDefinitionException e1) {
			e1.printStackTrace();
		}
	}

	public ModuleInspectorController(final FlexoController flexoController) {
		this.flexoController = flexoController;
		inspectors = new Hashtable<Class<?>, FIBInspector>();
		flexoConceptInspectors = new Hashtable<FlexoConcept, FIBInspector>();
		inspectorDialog = new FIBInspectorDialog(this);
		Boolean visible = null;
		if (flexoController.getApplicationContext().getGeneralPreferences() != null) {
			visible = flexoController.getApplicationContext().getGeneralPreferences().getInspectorVisible();
		}
		inspectorDialog.setVisible(visible == null || visible);
		inspectorDialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				flexoController.getApplicationContext().getGeneralPreferences().setInspectorVisible(true);
				flexoController.getApplicationContext().getPreferencesService().savePreferences();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				flexoController.getApplicationContext().getGeneralPreferences().setInspectorVisible(false);
				flexoController.getApplicationContext().getPreferencesService().savePreferences();
			};
		});
		Resource inspectorsDir = ResourceLocator.locateResource("Inspectors/COMMON");
		loadDirectory(inspectorsDir);
	}

	public FlexoController getFlexoController() {
		return flexoController;
	}

	public void loadDirectory(Resource dir) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Loading directory: " + dir);
		}
		if (dir != null) {
			for (Resource f : dir.getContents(Pattern.compile(".*[.]inspector"))) {

				logger.fine("Loading: " + f.getURI());
				FIBInspector inspector = (FIBInspector) FIBLibrary.instance().retrieveFIBComponent(f, false, INSPECTOR_FACTORY);
				if (inspector != null) {
					appendVisibleFor(inspector);
					appendEditableCondition(inspector);
					if (inspector.getDataClass() != null) {
						// try {
						inspectors.put(inspector.getDataClass(), inspector);
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Loaded inspector: " + f.getRelativePath() + " for " + inspector.getDataClass());
						}
						Progress.progress(FlexoLocalization.localizedForKey("loaded_inspector") + " "
								+ inspector.getDataClass().getSimpleName());
					}
				} else {
					logger.warning("Not found: " + f.getURI());
				}
			}

			for (FIBInspector inspector : inspectors.values()) {
				// logger.info("Merging inspector: " + inspector);
				inspector.appendSuperInspectors(this);
			}

			for (FIBInspector inspector : inspectors.values()) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Initialized inspector for " + inspector.getDataClass());
				}
			}

			setChanged();
			notifyObservers(new NewInspectorsLoaded());
		}
	}

	private void appendEditableCondition(FIBComponent component) {
		if (component instanceof FIBWidget) {
			FIBWidget widget = (FIBWidget) component;
			DataBinding<Boolean> enable = widget.getEnable();
			if (enable != null && enable.isValid()) {
				widget.setEnable(new DataBinding<Boolean>(enable.toString() + " & " + CONTROLLER_EDITABLE_BINDING));
			} else {
				widget.setEnable(new DataBinding<Boolean>(CONTROLLER_EDITABLE_BINDING));
			}
		} else if (component instanceof FIBContainer) {
			for (FIBComponent child : ((FIBContainer) component).getSubComponents()) {
				appendEditableCondition(child);
			}
		}
	}

	private void appendVisibleFor(FIBComponent component) {
		/*String visibleForParam = component.getParameter("visibleFor");
		if (visibleForParam != null) {
			String[] s = visibleForParam.split("[;,\"]");
			if (s.length > 0) {
				UserType userType = UserType.getCurrentUserType();
				boolean ok = false;
				for (String string : s) {
					ok |= userType.getName().equalsIgnoreCase(string);
					ok |= userType.getIdentifier().equalsIgnoreCase(string);
					if (ok) {
						break;
					}
				}
				if (!ok) {
					component.setVisible(new DataBinding<Boolean>("false"));
				}
			}
		}*/
		if (component instanceof FIBContainer) {
			for (FIBComponent child : ((FIBContainer) component).getSubComponents()) {
				appendVisibleFor(child);
			}
		}
	}

	protected FIBInspector inspectorForObject(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof FlexoConceptInstance) {
			return inspectorForFlexoConceptInstance(((FlexoConceptInstance) object).getFlexoConcept());

		}
		return inspectorForClass(object.getClass());
	}

	protected FIBInspector inspectorForFlexoConceptInstance(FlexoConcept concept) {
		if (concept == null) {
			return null;
		}
		FIBInspector returned = flexoConceptInspectors.get(concept);
		if (returned != null) {
			return returned;
		} else {
			returned = inspectorForClass(FlexoConceptInstance.class);
			returned = (FIBInspector) returned.cloneObject();
			// FIBTab basicTab = (FIBTab) returned.getTabPanel().getChildAt(0);
			// System.out.println("basicTab=" + basicTab);
			// returned.removeFromSubComponents(basicTab);
			returned.appendFlexoConceptInspectors(concept);
			flexoConceptInspectors.put(concept, returned);
			return returned;
		}
	}

	protected FIBInspector inspectorForClass(Class<?> aClass) {
		if (aClass == null) {
			return null;
		}
		FIBInspector returned = inspectors.get(aClass);
		if (returned != null) {
			return returned;
		} else {
			Class<?> superclass = aClass.getSuperclass();
			if (superclass != null) {
				returned = inspectors.get(aClass);
				if (returned != null) {
					return returned;
				} else {
					for (Class<?> superInterface : aClass.getInterfaces()) {
						returned = inspectors.get(superInterface);
						if (returned != null) {
							return returned;
						}
					}
					return inspectorForClass(superclass);
				}
			}
		}
		List<Class<?>> matchingClasses = new ArrayList<Class<?>>();
		for (Class<?> cl : inspectors.keySet()) {
			if (cl.isAssignableFrom(aClass)) {
				matchingClasses.add(cl);
			}
		}
		if (matchingClasses.size() > 0) {
			return inspectors.get(TypeUtils.getMostSpecializedClass(matchingClasses));
		}
		return null;
	}

	protected Map<Class<?>, FIBInspector> getInspectors() {
		return inspectors;
	}

	public FIBInspectorDialog getInspectorDialog() {
		return inspectorDialog;
	}

	public void refreshComponentVisibility() {
		inspectorDialog.getInspectorPanel().refreshComponentVisibility();
	}

	protected void switchToEmptyContent() {
		// logger.info("switchToEmptyContent()");
		currentInspectedObject = null;
		currentInspector = null;
		setChanged();
		notifyObservers(new EmptySelectionActivated());
	}

	private void switchToMultipleSelection() {
		// logger.info("switchToMultipleSelection()");
		currentInspectedObject = null;
		currentInspector = null;
		setChanged();
		notifyObservers(new MultipleSelectionActivated());
	}

	private void switchToInspector(FIBInspector newInspector/*, boolean updateEPTabs*/) {
		currentInspector = newInspector;
		setChanged();
		notifyObservers(new InspectorSwitching(newInspector/*, updateEPTabs*/));
	}

	private void displayObject(Object object) {
		setChanged();
		notifyObservers(new InspectedObjectChanged(object));
	}

	/**
	 * Returns boolean indicating if inspection change
	 * 
	 * @param object
	 * @return
	 */
	public boolean inspectObject(Object object) {
		if (object == currentInspectedObject) {
			return false;
		}

		// logger.info("ModuleInspectorController: inspectObject with " + object);
		// logger.info("currentInspectedObject=" + currentInspectedObject);

		currentInspectedObject = object;

		FIBInspector newInspector = inspectorForObject(object);

		if (newInspector == null) {
			logger.warning("No inspector for " + object);
			switchToEmptyContent();
		} else {
			/*boolean updateEPTabs = false;
			if (object instanceof FlexoConceptInstance) {
				updateEPTabs = newInspector.updateFlexoConceptInstanceInspector((FlexoConceptInstance) object);
			} else if (object instanceof FlexoObject) {
				updateEPTabs = newInspector.updateFlexoObjectInspector((FlexoObject) object);
			}*/
			if (newInspector != currentInspector /*|| updateEPTabs*/) {
				switchToInspector(newInspector/*, updateEPTabs*/);
			}
			displayObject(object);
		}

		return true;
	}

	public void resetInspector() {
		switchToEmptyContent();
	}

	@Override
	public void update(Observable o, Object notification) {
		if (notification instanceof InspectorSelection) {
			InspectorSelection inspectorSelection = (InspectorSelection) notification;
			if (inspectorSelection instanceof EmptySelection) {
				switchToEmptyContent();
			} else if (inspectorSelection instanceof MultipleSelection) {
				switchToMultipleSelection();
			} else if (inspectorSelection instanceof UniqueSelection) {
				inspectObject(((UniqueSelection) inspectorSelection).getInspectedObject());
			}
		}

		// Reforward notification to all in inspector panels
		setChanged();
		notifyObservers(notification);
	}

	public static class NewInspectorsLoaded {

	}

	public static class EmptySelectionActivated {

	}

	public static class MultipleSelectionActivated {

	}

	public static class InspectorSwitching {
		// private final boolean updateEPTabs;
		private final FIBInspector newInspector;

		public InspectorSwitching(FIBInspector newInspector/*, boolean updateEPTabs*/) {
			this.newInspector = newInspector;
			// this.updateEPTabs = updateEPTabs;
		}

		/*public boolean updateEPTabs() {
			return updateEPTabs;
		}*/

		public FIBInspector getNewInspector() {
			return newInspector;
		}
	}

	public static class InspectedObjectChanged {
		private final Object inspectedObject;

		public InspectedObjectChanged(Object inspectedObject) {
			this.inspectedObject = inspectedObject;
		}

		public Object getInspectedObject() {
			return inspectedObject;
		}
	}

	public void delete() {
		inspectorDialog.delete();
		currentInspectedObject = null;
		currentInspector = null;
	}

}
