/**
 * 
 * Copyright (c) 2013-2015, Openflexo
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

package org.openflexo.view.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptNature;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointNature;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelNature;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstanceNature;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewNature;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This class represents a technology-specific controller provided by a {@link TechnologyAdapter}<br>
 * A {@link TechnologyAdapterController} works above conceptual layer provided by a {@link TechnologyAdapter}, and manages all tooling
 * dedicated to technology-specific management of a {@link TechnologyAdapter}<br>
 * This controller makes the bindings between Openflexo controllers/editors layer and the {@link TechnologyAdapter}
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyAdapterController<TA extends TechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(TechnologyAdapterController.class.getPackage().getName());

	private TechnologyAdapterControllerService technologyAdapterControllerService;

	/**
	 * Returns applicable {@link ProjectNatureService}
	 * 
	 * @return
	 */
	public TechnologyAdapterControllerService getTechnologyAdapterControllerService() {
		return technologyAdapterControllerService;
	}

	/**
	 * Sets applicable {@link ProjectNatureService}
	 * 
	 * @param technologyAdapterService
	 */
	public void setTechnologyAdapterService(TechnologyAdapterControllerService technologyAdapterControllerService) {
		this.technologyAdapterControllerService = technologyAdapterControllerService;
	}

	/**
	 * Return TechnologyAdapter
	 * 
	 * @return
	 */
	public final TA getTechnologyAdapter() {
		return technologyAdapterControllerService.getServiceManager().getService(TechnologyAdapterService.class)
				.getTechnologyAdapter(getTechnologyAdapterClass());
	}

	/**
	 * Return TechnologyAdapter class
	 * 
	 * @return
	 */
	public abstract Class<TA> getTechnologyAdapterClass();

	public abstract void initializeActions(ControllerActionInitializer actionInitializer);

	public void initialize() {

	}

	public FlexoServiceManager getServiceManager() {
		return getTechnologyAdapter().getTechnologyAdapterService().getServiceManager();
	}

	/**
	 * Return icon representing underlying technology, required size is 32x32
	 * 
	 * @return
	 */
	public abstract ImageIcon getTechnologyBigIcon();

	/**
	 * Return icon representing underlying technology, required size is 16x16
	 * 
	 * @return
	 */
	public abstract ImageIcon getTechnologyIcon();

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	public abstract ImageIcon getModelIcon();

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	public abstract ImageIcon getMetaModelIcon();

	/**
	 * Return icon representing supplied ontology object
	 * 
	 * @param object
	 * @return
	 */
	public abstract ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject<?>> objectClass);

	/**
	 * Return icon representing supplied pattern role
	 * 
	 * @param object
	 * @return
	 */
	public abstract ImageIcon getIconForPatternRole(Class<? extends FlexoRole<?>> patternRoleClass);

	/**
	 * Return icon representing supplied edition action
	 * 
	 * @param object
	 * @return
	 */
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction> editionActionClass) {

		if (AddFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
		} else if (SelectFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
		} else if (MatchFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_ICON, IconLibrary.SYNC);
		} else if (AddToListAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.LIST_ICON, IconLibrary.POSITIVE_MARKER);
		} else if (RemoveFromListAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.LIST_ICON, IconLibrary.NEGATIVE_MARKER);
		} else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.DELETE_ICON;
		} else if (ConditionalAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.CONDITIONAL_ACTION_ICON;
		} else if (IterationAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.ITERATION_ACTION_ICON;
		} else if (ExpressionAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.EXPRESSION_ACTION_ICON;
		}
		return null;

	}

	/**
	 * Return icon representing supplied edition scheme
	 * 
	 * @param object
	 * @return
	 */
	public ImageIcon getIconForFlexoBehaviour(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
		return null;
	}

	public abstract String getWindowTitleforObject(TechnologyObject<TA> object, FlexoController controller);

	/**
	 * Return boolean indicating if this TechnologyAdapter controller service support ModuleView rendering for supplied technology object
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean hasModuleViewForObject(TechnologyObject<TA> object, FlexoController controller);

	/**
	 * Return a newly created ModuleView for supplied technology object, if this TechnologyAdapter controller service support ModuleView
	 * rendering
	 * 
	 * @param object
	 * @return
	 */
	public abstract ModuleView<?> createModuleViewForObject(TechnologyObject<TA> object, FlexoController controller,
			FlexoPerspective perspective);

	/**
	 * Called when a {@link ModuleView} is about to be displayed
	 * 
	 * @param moduleView
	 * @param controller
	 * @param perspective
	 */
	/*public void notifyModuleViewDisplayed(ModuleView<?> moduleView, FlexoController controller, FlexoPerspective perspective) {
		System.out.println(">>>>>>> Will display module view for " + moduleView.getRepresentedObject());
	}*/

	/*public File getFIBPanelForObject(Object anObject) {
	>>>>>>> branch '1.7' of ssh://git@github.com/openflexo-team/openflexo-core.git
		if (anObject != null) {
			return getFIBPanelForClass(anObject.getClass());
		}
		return null;
	}

	public String getFIBPanelForClass(Class<?> aClass) {
		if (aClass == null) {
			return null;
		}
		String returned = fibPanelsForClasses.get(aClass);
		if (returned == null) {
			if (aClass.getAnnotation(FIBPanel.class) != null) {
				String fibPanelName = aClass.getAnnotation(FIBPanel.class).value();
				URL fibPanelURL = ResourceLocator.locateResource(fibPanelName);
				if (fibPanelURL != null) {
					logger.info("Found " + fibPanelURL.toString());
					fibPanelsForClasses.put(aClass, fibPanelName);
					return fibPanelName;
				} else {
					logger.warning("Not found " + fibPanelName);
					return null;
				}
			}
			if (aClass.getSuperclass() != null) {
				return getFIBPanelForClass(aClass.getSuperclass());
			}
		}
		return returned;
	}*/

	public Resource getFIBPanelForObject(Object anObject) {
		if (anObject != null) {
			return getFIBPanelForClass(anObject.getClass());
		}
		return null;
	}

	private final Map<Class<?>, Resource> fibPanelsForClasses = new HashMap<Class<?>, Resource>() {
		@Override
		public Resource get(Object key) {
			if (containsKey(key)) {
				return super.get(key);
			}
			if (key instanceof Class) {
				Class<?> aClass = (Class<?>) key;
				// System.out.println("Searching FIBPanel for " + aClass);
				if (aClass.getAnnotation(FIBPanel.class) != null) {
					// System.out.println("Found annotation " + aClass.getAnnotation(FIBPanel.class));
					String fibPanelName = aClass.getAnnotation(FIBPanel.class).value();
					// System.out.println("fibPanelFile=" + fibPanel);
					Resource fibLocation = ResourceLocator.locateResource(fibPanelName);
					if (fibLocation != null) {
						// logger.info("Found " + fibPanel);
						put(aClass, fibLocation);
						return fibLocation;
					}
				}
				put(aClass, null);
				return null;
			}
			return null;
		}
	};

	public Resource getFIBPanelForClass(Class<?> aClass) {
		return TypeUtils.objectForClass(aClass, fibPanelsForClasses);
	}

	/*private List<Class<? extends VirtualModelInstanceNature>> availableVirtualModelInstanceNatures;

	public List<Class<? extends VirtualModelInstanceNature>> getAvailableVirtualModelInstanceNatures() {
		if (availableVirtualModelInstanceNatures == null) {
			availableVirtualModelInstanceNatures = computeAvailableVirtualModelInstanceNatures();
		}
		return availableVirtualModelInstanceNatures;
	}

	private List<Class<? extends VirtualModelInstanceNature>> computeAvailableVirtualModelInstanceNatures() {
		availableVirtualModelInstanceNatures = new ArrayList<Class<? extends VirtualModelInstanceNature>>();
		Class<?> cl = getClass();
		if (cl.isAnnotationPresent(DeclareVirtualModelInstanceNatures.class)) {
			DeclareVirtualModelInstanceNatures allNatureDeclarations = cl.getAnnotation(DeclareVirtualModelInstanceNatures.class);
			for (DeclareVirtualModelInstanceNature natureDeclaration : allNatureDeclarations.value()) {
				availableVirtualModelInstanceNatures.add(natureDeclaration.nature());
			}
		}
		return availableVirtualModelInstanceNatures;
	}*/

	// ***************************************************************
	// Management of FlexoProject natures
	// ***************************************************************

	public final boolean hasSpecificFlexoProjectNature(FlexoProject project) {
		return getSpecificProjectNatures(project).size() > 0;
	}

	// Override when required
	public List<? extends ProjectNature> getSpecificProjectNatures(FlexoProject project) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<FlexoProject> createFlexoProjectModuleViewForSpecificNature(FlexoProject project, ProjectNature nature,
			FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	// ***************************************************************
	// Management of View natures
	// ***************************************************************

	public final boolean hasSpecificViewNatures(View view) {
		return getSpecificViewNatures(view).size() > 0;
	}

	// Override when required
	public List<? extends ViewNature> getSpecificViewNatures(View view) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<View> createViewModuleViewForSpecificNature(View view, ViewNature nature, FlexoController controller,
			FlexoPerspective perspective) {
		return null;
	}

	// ***************************************************************
	// Management of VirtualModelInstance natures
	// ***************************************************************

	public final boolean hasSpecificVirtualModelInstanceNatures(VirtualModelInstance vmi) {
		return getSpecificVirtualModelInstanceNatures(vmi).size() > 0;
	}

	// Override when required
	public List<? extends VirtualModelInstanceNature> getSpecificVirtualModelInstanceNatures(VirtualModelInstance vmInstance) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<VirtualModelInstance> createVirtualModelInstanceModuleViewForSpecificNature(VirtualModelInstance vmInstance,
			VirtualModelInstanceNature nature, FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	// ***************************************************************
	// Management of FlexoConceptInstance natures
	// ***************************************************************

	public final boolean hasSpecificFlexoConceptInstanceNatures(FlexoConceptInstance conceptInstance) {
		return getSpecificFlexoConceptInstanceNatures(conceptInstance).size() > 0;
	}

	// Override when required
	public List<? extends FlexoConceptInstanceNature> getSpecificFlexoConceptInstanceNatures(FlexoConceptInstance conceptInstance) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<FlexoConceptInstance> createFlexoConceptInstanceModuleViewForSpecificNature(FlexoConceptInstance conceptInstance,
			FlexoConceptInstanceNature nature, FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	// ***************************************************************
	// Management of ViewPoint natures
	// ***************************************************************

	public final boolean hasSpecificViewPointNatures(ViewPoint viewPoint) {
		return getSpecificViewPointNatures(viewPoint).size() > 0;
	}

	// Override when required
	public List<? extends ViewPointNature> getSpecificViewPointNatures(ViewPoint viewPoint) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<ViewPoint> createViewPointModuleViewPointForSpecificNature(ViewPoint viewPoint, ViewPointNature nature,
			FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	// ***************************************************************
	// Management of VirtualModel natures
	// ***************************************************************

	public final boolean hasSpecificVirtualModelNatures(VirtualModel vmi) {
		return getSpecificVirtualModelNatures(vmi).size() > 0;
	}

	// Override when required
	public List<? extends VirtualModelNature> getSpecificVirtualModelNatures(VirtualModel virtualModel) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<VirtualModel> createVirtualModelModuleViewForSpecificNature(VirtualModel virtualModel, VirtualModelNature nature,
			FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	// ***************************************************************
	// Management of FlexoConcept natures
	// ***************************************************************

	public final boolean hasSpecificFlexoConceptNatures(FlexoConcept concept) {
		return getSpecificFlexoConceptNatures(concept).size() > 0;
	}

	// Override when required
	public List<? extends FlexoConceptNature> getSpecificFlexoConceptNatures(FlexoConcept concept) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<FlexoConcept> createFlexoConceptModuleViewForSpecificNature(FlexoConcept concept, FlexoConceptNature nature,
			FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	private final Map<FlexoController, TechnologyPerspective<TA>> technologyPerspectives = new HashMap<FlexoController, TechnologyPerspective<TA>>();

	public TechnologyPerspective<TA> getTechnologyPerspective(FlexoController controller) {
		TechnologyPerspective<TA> returned = technologyPerspectives.get(controller);
		if (returned == null) {
			returned = new TechnologyPerspective<TA>(getTechnologyAdapter(), controller);
			technologyPerspectives.put(controller, returned);
		}
		return returned;
	}

	public void installTechnologyPerspective(FlexoController controller) {
		controller.addToPerspectives(getTechnologyPerspective(controller));
	}

	/**
	 * Override when required
	 * 
	 * @param controller
	 * @return
	 */
	protected FIBTechnologyBrowser<TA> makeTechnologyBrowser(FlexoController controller) {
		return new FIBTechnologyBrowser<TA>(getTechnologyAdapter(), controller);
	}

}
