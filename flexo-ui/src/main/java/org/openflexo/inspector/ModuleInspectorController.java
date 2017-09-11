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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.TransformException;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.expr.BindingValue.AbstractBindingPathElement;
import org.openflexo.connie.expr.BindingValue.NormalBindingPathElement;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionTransformer;
import org.openflexo.connie.expr.parser.ExpressionParser;
import org.openflexo.connie.expr.parser.ParseException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLLocalizedDictionary;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBVariable;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.TechnologyAdapterController;

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

	private FIBInspector currentInspector = null;

	private final InspectorGroup coreInspectorGroup;
	private final List<InspectorGroup> inspectorGroups;

	private Object currentInspectedObject = null;

	public ModuleInspectorController(final FlexoController flexoController) {
		this.flexoController = flexoController;

		inspectorGroups = new ArrayList<>();
		coreInspectorGroup = new InspectorGroup(ResourceLocator.locateResource("Inspectors/COMMON"), getInspectorsFIBLibrary(),
				flexoController.getFlexoLocales());
		inspectorGroups.add(coreInspectorGroup);

		flexoConceptInspectors = new Hashtable<>();
		inspectorDialog = new FIBInspectorDialog(this);
		Boolean visible = null;
		if (flexoController.getApplicationContext().getGeneralPreferences() != null) {
			visible = flexoController.getApplicationContext().getPresentationPreferences().getInspectorVisible();
		}
		inspectorDialog.setVisible(visible == null || visible);
		inspectorDialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				flexoController.getApplicationContext().getPresentationPreferences().setInspectorVisible(true);
				flexoController.getApplicationContext().getPreferencesService().savePreferences();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				flexoController.getApplicationContext().getPresentationPreferences().setInspectorVisible(false);
				flexoController.getApplicationContext().getPreferencesService().savePreferences();
			};
		});

		// Resource inspectorsDir = ResourceLocator.locateResource("Inspectors/COMMON");
		// loadDirectory(inspectorsDir);
	}

	public InspectorGroup getCoreInspectorGroup() {
		return coreInspectorGroup;
	}

	/*private InspectorGroup loadInspectorGroup(Resource inspectorGroupFolder, InspectorGroup... parentInspectorGroups) {
		InspectorGroup returned = new InspectorGroup(inspectorGroupFolder) {
			@Override
			public void progress(Resource f, FIBInspector inspector) {
				super.progress(f, inspector);
				appendVisibleFor(inspector);
				appendEditableCondition(inspector);
				Progress.progress(FlexoLocalization.localizedForKey("loaded_inspector") + " " + inspector.getDataClass().getSimpleName());
			}
		};
		return returned;
	}*/

	private FIBLibrary getInspectorsFIBLibrary() {
		if (getFlexoController() != null) {
			return getFlexoController().getApplicationContext().getApplicationFIBLibraryService().getApplicationFIBLibrary();
		}
		return ApplicationFIBLibraryImpl.instance();
	}

	public InspectorGroup loadDirectory(Resource inspectorsDirectory, LocalizedDelegate locales, InspectorGroup... parentInspectorGroups) {
		InspectorGroup newInspectorGroup = new InspectorGroup(inspectorsDirectory, getInspectorsFIBLibrary(), locales,
				parentInspectorGroups) {
			@Override
			public void progress(Resource f, FIBInspector inspector) {
				super.progress(f, inspector);
				appendVisibleFor(inspector);

				// Dont do it anymore: perfs issues
				// appendEditableCondition(inspector);
				Progress.progress(FlexoLocalization.getMainLocalizer().localizedForKey("loaded_inspector") + " "
						+ inspector.getDataClass().getSimpleName());
			}
		};
		inspectorGroups.add(newInspectorGroup);
		return newInspectorGroup;
	}

	public FlexoController getFlexoController() {
		return flexoController;
	}

	/*public void loadDirectory(Resource dir) {
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
	}*/

	private void appendEditableCondition(FIBComponent component) {
		if (component instanceof FIBWidget) {
			FIBWidget widget = (FIBWidget) component;
			DataBinding<Boolean> enable = widget.getEnable();
			if (enable != null && enable.isValid()) {
				widget.setEnable(new DataBinding<Boolean>(enable.toString() + " & " + CONTROLLER_EDITABLE_BINDING));
			}
			else {
				widget.setEnable(new DataBinding<Boolean>(CONTROLLER_EDITABLE_BINDING));
			}
		}
		else if (component instanceof FIBContainer) {
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

	/**
	 * Return the inspector matching supplied objectClass<br>
	 * The research is performed on all inspector groups declared in this module<br>
	 * In case of multiple possibilities, the most specialized inspector is returned.
	 * 
	 * @param objectClass
	 * @return
	 */
	public FIBInspector inspectorForClass(Class<?> objectClass) {
		if (objectClass == null) {
			return null;
		}

		Map<Class<?>, FIBInspector> potentialInspectors = new HashMap<>();
		for (InspectorGroup inspectorGroup : new ArrayList<>(inspectorGroups)) {
			FIBInspector inspector = inspectorGroup.inspectorForClass(objectClass);
			if (inspector != null) {
				potentialInspectors.put(inspector.getDataClass(), inspector);
			}
		}

		if (potentialInspectors.size() == 0) {
			logger.warning("Could not find inspector for " + objectClass);
			return null;
		}

		Class<?> mostSpecializedClass = TypeUtils.getMostSpecializedClass(potentialInspectors.keySet());

		FIBInspector returned = potentialInspectors.get(mostSpecializedClass);

		// System.out.println("Pour la classe " + objectClass + " je retourne:");
		// System.out.println(getFactory().stringRepresentation(returned));

		return returned;
	}

	/**
	 * Return all inspectors matching supplied objectClass<br>
	 * The research is performed on all inspector groups declared in this module<br>
	 * 
	 * @param objectClass
	 * @return
	 */
	public List<FIBInspector> inspectorsForClass(Class<?> objectClass) {
		if (objectClass == null) {
			return null;
		}

		List<FIBInspector> returned = new ArrayList<>();

		for (InspectorGroup inspectorGroup : inspectorGroups) {
			for (FIBInspector inspector : inspectorGroup.inspectorsForClass(objectClass)) {
				if (!returned.contains(inspector)) {
					returned.add(inspector);
				}
			}
		}

		return returned;

	}

	public FIBInspector inspectorForObject(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof FlexoConceptInstance) {
			return inspectorForFlexoConceptInstance((FlexoConceptInstance) object);
		}

		return inspectorForClass(object.getClass());
	}

	/**
	 * Internally called to build an inspector for a given {@link FlexoConceptInstance}<br>
	 * Inspector is build from {@link FlexoConceptInstance} classical inspector augmented with tab as defined in
	 * {@link FlexoConceptInspector}
	 * 
	 * @param conceptInstance
	 * @return
	 */
	private FIBInspector inspectorForFlexoConceptInstance(FlexoConceptInstance conceptInstance) {
		FlexoConcept concept = conceptInstance.getFlexoConcept();
		if (concept == null) {
			return null;
		}
		FIBInspector returned = flexoConceptInspectors.get(concept);
		if (returned != null) {
			return returned;
		}
		else {
			// First retrieve basic inspector (as defined in FlexoConceptInstance.inspector)
			returned = inspectorForClass(conceptInstance.getImplementedInterface());
			// Clone it
			returned = (FIBInspector) returned.cloneObject();
			// And append tab matching FlexoConceptInspector
			appendFlexoConceptInspector(concept, returned);
			flexoConceptInspectors.put(concept, returned);
			return returned;
		}
	}

	/*protected FIBInspector inspectorForClass(Class<?> aClass) {
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
	}*/

	/*protected Map<Class<?>, FIBInspector> getInspectors() {
		return inspectors;
	}*/

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
		// System.out.println("switchToInspector " + newInspector);
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
		}
		else {
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
			}
			else if (inspectorSelection instanceof MultipleSelection) {
				switchToMultipleSelection();
			}
			else if (inspectorSelection instanceof UniqueSelection) {
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

	private void appendFlexoConceptInspector(FlexoConcept concept, FIBInspector inspector) {
		FIBTab newTab = makeFIBTab(concept);
		inspector.getTabPanel().addToSubComponents(newTab, null, 0);

		/*try {
			logger.info("Getting this "
					+ XMLCoder.encodeObjectWithMapping(this, FIBLibrary.getFIBMapping(), StringEncoder.getDefaultInstance()));
		} catch (InvalidObjectSpecificationException e) {
			e.printStackTrace();
		} catch (InvalidModelException e) {
			e.printStackTrace();
		} catch (AccessorInvocationException e) {
			e.printStackTrace();
		} catch (DuplicateSerializationIdentifierException e) {
			e.printStackTrace();
		}*/
	}

	private Map<FlexoConcept, FIBPanel> flexoConceptInspectorPanels = new HashMap<>();

	public FIBPanel getFIBInspectorPanel(FlexoConcept flexoConcept) {
		FIBPanel returned = flexoConceptInspectorPanels.get(flexoConcept);
		if (returned == null) {
			returned = makeFIBInspectorPanel(flexoConcept);
			flexoConceptInspectorPanels.put(flexoConcept, returned);
		}
		return returned;

	}

	private FIBPanel makeFIBInspectorPanel(FlexoConcept flexoConcept) {
		FIBPanel inspector = getFactory().newFIBPanel();
		inspector.setLayout(Layout.twocols);
		inspector.setUseScrollBar(true);

		inspector.setControllerClass(FlexoFIBController.class);

		// We create a variable for inspector data
		// This variable is called fci, with type FlexoConceptInstanceType<FlexoConcept>, and value 'data' (which is the
		// FlexoConceptInstance)
		// The goal of that variable definition is to provide type for inspected FlexoConceptInstance
		FIBVariable<?> dataVariable = getFactory().newFIBVariable(inspector, "fci", flexoConcept.getInstanceType());
		dataVariable.setValue(new DataBinding<>("data"));
		inspector.addToVariables(dataVariable);
		inspector.setName(flexoConcept.getName() + "Panel");

		appendInspectorEntries(flexoConcept, inspector);
		inspector.finalizeDeserialization();

		return inspector;
	}

	/**
	 * Internally called to create {@link FIBTab} matching inspector of supplied {@link FlexoConcept}
	 * 
	 * @param flexoConcept
	 * @param inspector
	 * @return
	 */
	private FIBTab makeFIBTab(FlexoConcept flexoConcept) {
		FIBTab newTab = getFactory().newFIBTab();
		newTab.setTitle(flexoConcept.getInspector().getInspectorTitle());
		newTab.setLayout(Layout.twocols);
		newTab.setUseScrollBar(true);

		// We create a variable for inspector data
		// This variable is called fci, with type FlexoConceptInstanceType<FlexoConcept>, and value 'data' (which is the
		// FlexoConceptInstance)
		// The goal of that variable definition is to provide type for inspected FlexoConceptInstance
		FIBVariable<?> dataVariable = getFactory().newFIBVariable(newTab, "fci", flexoConcept.getInstanceType());
		dataVariable.setValue(new DataBinding<>("data"));
		newTab.addToVariables(dataVariable);
		newTab.setName(flexoConcept.getName() + "Panel");

		appendInspectorEntries(flexoConcept, newTab);
		newTab.finalizeDeserialization();

		/*for (FIBComponent c : newTab.getSubComponents()) {
			System.out.println("> component " + c);
			if (c instanceof FIBWidget) {
				System.out.println("value=" + ((FIBWidget) c).getData());
				System.out.println(
						"valid=" + ((FIBWidget) c).getData().isValid() + " reason=" + ((FIBWidget) c).getData().invalidBindingReason());
			}
		}*/

		return newTab;
	}

	/**
	 * Internally called to append all entries of supplied flexo concept
	 * 
	 * @param flexoConcept
	 * @param newTab
	 */
	private void appendInspectorEntries(FlexoConcept flexoConcept, FIBPanel newTab) {
		if (flexoConcept == null) {
			logger.warning("Unexpected null concept ");
			return;
		}
		for (FlexoConcept parentEP : flexoConcept.getParentFlexoConcepts()) {
			appendInspectorEntries(parentEP, newTab);
		}
		if (flexoConcept.getVirtualModel() == null) {
			logger.warning("Unexpected null virtual model for concept " + flexoConcept);
			return;
		}
		FMLLocalizedDictionary localizedDictionary = flexoConcept.getVirtualModel().getLocalizedDictionary();
		for (final InspectorEntry entry : flexoConcept.getInspector().getEntries()) {
			FIBLabel label = getFactory().newFIBLabel();
			String entryLabel = localizedDictionary.localizedForKeyAndLanguage(entry.getLabel(), FlexoLocalization.getCurrentLanguage());
			if (entryLabel == null) {
				entryLabel = entry.getLabel();
			}
			label.setLabel(entryLabel);
			newTab.addToSubComponents(label, new TwoColsLayoutConstraints(TwoColsLayoutLocation.left, false, false));
			FIBWidget widget = makeWidget(entry, newTab);
			if (widget != null) {
				widget.setBindingFactory(entry.getBindingFactory());
				String bindingPath = entry.getData().toString();
				String normalizedBindingPath = normalizeBindingPath(bindingPath);
				widget.setData(new DataBinding<>(normalizedBindingPath));
				widget.setReadOnly(entry.getIsReadOnly());
			}
			/*System.out.println("Widget " + widget + " data=" + entry.getData());
			System.out.println("valid:" + entry.getData().isValid());
			System.out.println("reason=" + entry.getData().invalidBindingReason());
			System.out.println("Widget data " + widget.getData());
			System.out.println("valid:" + widget.getData().isValid());
			System.out.println("reason=" + widget.getData().invalidBindingReason());*/
		}

		//System.out.println("Je retourne " + getFactory().stringRepresentation(newTab));
	}

	/**
	 * Normalized BindingPath so that all {@link BindingValue} starts with 'fci.' (name of FCI beeing represented)
	 * 
	 * @param bindingPath
	 * @return
	 */
	private static String normalizeBindingPath(String bindingPath) {
		Expression expression = null;
		try {
			expression = ExpressionParser.parse(bindingPath);

			expression = expression.transform(new ExpressionTransformer() {
				@Override
				public Expression performTransformation(Expression e) throws TransformException {
					if (e instanceof BindingValue) {
						BindingValue bv = (BindingValue) e;
						if (bv.getParsedBindingPath().size() > 0) {
							AbstractBindingPathElement firstPathElement = bv.getParsedBindingPath().get(0);
							if (!(firstPathElement instanceof NormalBindingPathElement)
									|| !((NormalBindingPathElement) firstPathElement).property.equals("fci")) {
								bv.getParsedBindingPath().add(0, new NormalBindingPathElement("fci"));
								bv.clearSerializationRepresentation();
							}
						}
						return bv;
					}
					return e;
				}
			});

			return expression.toString();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		}
		return expression.toString();
	}

	/**
	 * Factory method used to instanciate a technology-specific FIBWidget for a given {@link InspectorEntry}<br>
	 * We iterate on all known technologies to use the delegated {@link TechnologyAdapterController}
	 * 
	 * @param entry
	 * @param newTab
	 * @param factory
	 * @return
	 */
	private FIBWidget makeWidget(final InspectorEntry entry, FIBPanel newTab) {
		for (TechnologyAdapter ta : flexoController.getApplicationContext().getTechnologyAdapterService().getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = flexoController.getTechnologyAdapterController(ta);
			boolean[] expand = { true, false };
			FIBWidget returned = tac.makeWidget(entry, null, getFactory(), expand);
			newTab.addToSubComponents(returned, new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, expand[0], expand[1]));
			if (returned != null) {
				return returned;
			}
		}

		return null;
	}

	public FIBModelFactory getFactory() {
		return coreInspectorGroup.getFIBModelFactory();
	}
}
