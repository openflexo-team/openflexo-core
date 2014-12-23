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
package org.openflexo.view.controller.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptNature;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointNature;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelNature;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstanceNature;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewNature;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.swing.layout.MultiSplitLayout.Node;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

public abstract class FlexoPerspective extends ControllerModelObject {

	static final Logger logger = Logger.getLogger(FlexoPerspective.class.getPackage().getName());

	private final String name;

	public static final String HEADER = "header";
	public static final String FOOTER = "footer";

	public static final String TOP_LEFT_VIEW = "topLeftView";
	public static final String TOP_RIGHT_VIEW = "topRightView";
	public static final String TOP_CENTER_VIEW = "topCenterView";

	public static final String MIDDLE_LEFT_VIEW = "middleLeftView";
	public static final String MIDDLE_RIGHT_VIEW = "middleRightView";

	public static final String BOTTOM_LEFT_VIEW = "bottomLeftView";
	public static final String BOTTOM_RIGHT_VIEW = "bottomRightView";
	public static final String BOTTOM_CENTER_VIEW = "bottomCenterView";

	public static final String[] PROPERTIES = { HEADER, FOOTER, TOP_LEFT_VIEW, TOP_RIGHT_VIEW, TOP_CENTER_VIEW, MIDDLE_LEFT_VIEW,
			MIDDLE_RIGHT_VIEW, BOTTOM_LEFT_VIEW, BOTTOM_RIGHT_VIEW, BOTTOM_CENTER_VIEW };

	private JComponent topLeftView;
	private JComponent topRightView;
	private JComponent middleLeftView;
	private JComponent middleRightView;
	private JComponent bottomLeftView;
	private JComponent bottomRightView;
	private JComponent topCenterView;
	private JComponent bottomCenterView;

	private JComponent header;
	private JComponent footer;

	private final FlexoController controller;

	public FlexoPerspective(String name, FlexoController controller) {
		super();
		this.name = name;
		this.controller = controller;
	}

	public String getName() {
		return name;
	}

	public String getLocalizedName() {
		return FlexoLocalization.localizedForKey(getName());
	}

	@Override
	public String toString() {
		return getName();
	}

	public FlexoController getController() {
		return controller;
	}

	public void setupDefaultLayout(Node layout) {
	}

	public abstract ImageIcon getActiveIcon();

	public ModuleView<?> createModuleViewForObject(FlexoObject object, boolean editable) {
		if (!editable) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Perspective " + getName()
						+ " does not override createModuleViewForObject(O object, FlexoController controller, boolean editable)");
			}
		}
		return createModuleViewForObject(object);
	}

	public ModuleView<?> createModuleViewForObject(FlexoObject object) {
		if (object instanceof FlexoProject) {
			FlexoProject project = (FlexoProject) object;
			List<ProjectNature> availableNatures = getSpecificNaturesForProject(project);
			if (availableNatures.size() > 0) {
				ProjectNature nature = availableNatures.get(0);
				return getModuleViewForProject(project, nature);
			}
			// No default view for a FlexoProject !
			return new EmptyPanel<FlexoObject>(controller, this, object);
		}
		if (object instanceof View) {
			View view = (View) object;
			List<ViewNature> availableNatures = getSpecificNaturesForView(view);
			if (availableNatures.size() > 0) {
				ViewNature nature = availableNatures.get(0);
				return getModuleViewForView(view, nature);
			}
			// TODO: check that FMLTechnologyAdapterController now handle this
			// return new ViewModuleView(view, controller, this);
		}
		if (object instanceof VirtualModelInstance) {
			VirtualModelInstance vmi = (VirtualModelInstance) object;
			List<VirtualModelInstanceNature> availableNatures = getSpecificNaturesForVirtualModelInstance(vmi);
			if (availableNatures.size() > 0) {
				VirtualModelInstanceNature nature = availableNatures.get(0);
				return getModuleViewForVirtualModelInstance(vmi, nature);
			}
			// TODO: check that FMLTechnologyAdapterController now handle this
			// return new VirtualModelInstanceView(vmi, controller, this);
		}
		if (object instanceof FlexoConceptInstance) {
			FlexoConceptInstance vmi = (FlexoConceptInstance) object;
			List<FlexoConceptInstanceNature> availableNatures = getSpecificNaturesForFlexoConceptInstance(vmi);
			if (availableNatures.size() > 0) {
				FlexoConceptInstanceNature nature = availableNatures.get(0);
				return getModuleViewForFlexoConceptInstance(vmi, nature);
			}
			// No default view for a FlexoConceptInstance !
			// return new EmptyPanel<FlexoObject>(controller, this, object);
			// TODO: check that FMLTechnologyAdapterController now handle this
		}
		if (object instanceof ViewPoint) {
			ViewPoint viewPoint = (ViewPoint) object;
			List<ViewPointNature> availableNatures = getSpecificNaturesForViewPoint(viewPoint);
			if (availableNatures.size() > 0) {
				ViewPointNature nature = availableNatures.get(0);
				return getModuleViewPointForViewPoint(viewPoint, nature);
			}
			// return new ViewPointView(viewPoint, controller, this);
			// TODO: check that FMLTechnologyAdapterController now handle this
		}
		if (object instanceof VirtualModel) {
			VirtualModel virtualModel = (VirtualModel) object;
			List<VirtualModelNature> availableNatures = getSpecificNaturesForVirtualModel(virtualModel);
			if (availableNatures.size() > 0) {
				VirtualModelNature nature = availableNatures.get(0);
				return getModuleViewForVirtualModel(virtualModel, nature);
			}
			// return new VirtualModelView(virtualModel, controller, this);
			// TODO: check that FMLTechnologyAdapterController now handle this
		}
		if (object instanceof FlexoConcept) {
			FlexoConcept concept = (FlexoConcept) object;
			List<FlexoConceptNature> availableNatures = getSpecificNaturesForFlexoConcept(concept);
			if (availableNatures.size() > 0) {
				FlexoConceptNature nature = availableNatures.get(0);
				return getModuleViewForFlexoConcept(concept, nature);
			}
			// return new StandardFlexoConceptView(concept, controller, this);
			// TODO: check that FMLTechnologyAdapterController now handle this
		}
		if (object instanceof TechnologyObject) {
			return getModuleViewForTechnologyObject((TechnologyObject<?>) object);
		}
		return new EmptyPanel<FlexoObject>(controller, this, object);
	}

	/**
	 * Return boolean indicating if this perspective handles supplied object (true if perspective may build and display a {@link ModuleView}
	 * representing supplied object)<br>
	 * 
	 * This method should be overriden<br>
	 * 
	 * Default returned value is true for View/VirtualModelInstance/ViewPoint/VirtualModel/FlexoConcept objects<br>
	 * Default returned value depends on nature availability for FlexoProject/FlexoConceptInstance objects
	 * 
	 * @param object
	 * @return
	 */
	public boolean hasModuleViewForObject(FlexoObject object) {
		if (object instanceof FlexoProject) {
			return getSpecificNaturesForProject((FlexoProject) object).size() > 0;
		}
		if (object instanceof View) {
			return true;
		}
		if (object instanceof VirtualModelInstance) {
			return true;
		}
		if (object instanceof FlexoConceptInstance) {
			return getSpecificNaturesForFlexoConceptInstance((FlexoConceptInstance) object).size() > 0;
		}
		if (object instanceof ViewPoint) {
			return true;
		}
		if (object instanceof VirtualModel) {
			return true;
		}
		if (object instanceof FlexoConcept) {
			return true;
		}
		if (object instanceof TechnologyObject) {
			return hasModuleViewForTechnologyObject((TechnologyObject<?>) object);
		}
		return false;
	}

	/**
	 * Return boolean indicating if this perspective handles supplied object (true if perspective may build and display a {@link ModuleView}
	 * representing supplied object)<br>
	 * 
	 * @param object
	 * @return
	 */
	public final <TA extends TechnologyAdapter> boolean hasModuleViewForTechnologyObject(TechnologyObject<TA> object) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterController<TA> tac = tacService.getTechnologyAdapterController(object.getTechnologyAdapter());
		return tac.hasModuleViewForObject(object, controller);
	}

	/**
	 * Return boolean indicating if this perspective handles supplied object (true if perspective may build and display a {@link ModuleView}
	 * representing supplied object)<br>
	 * 
	 * @param object
	 * @return
	 */
	public final <TA extends TechnologyAdapter> ModuleView<?> getModuleViewForTechnologyObject(TechnologyObject<TA> object) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterController<TA> tac = tacService.getTechnologyAdapterController(object.getTechnologyAdapter());
		if (tac != null) {
			return tac.createModuleViewForObject(object, controller, this);
		}
		return null;
	}

	/**
	 * Return default object to be displayed, given supplied object (which might be null)
	 * 
	 * @param proposedObject
	 * @param controller
	 * @return
	 */
	public FlexoObject getDefaultObject(FlexoObject proposedObject) {
		if (hasModuleViewForObject(proposedObject)) {
			return proposedObject;
		}
		return null;
	}

	public JComponent getHeader() {
		return header;
	}

	public void setHeader(JComponent header) {
		JComponent old = this.header;
		this.header = header;
		getPropertyChangeSupport().firePropertyChange(HEADER, old, header);
	}

	public JComponent getFooter() {
		return footer;
	}

	public void setFooter(JComponent footer) {
		JComponent old = this.footer;
		this.footer = footer;
		getPropertyChangeSupport().firePropertyChange(FOOTER, old, footer);
	}

	/*public void notifyModuleViewDisplayed(ModuleView<?> moduleView) {
	}*/

	public JComponent getTopLeftView() {
		return topLeftView;
	}

	public void setTopLeftView(JComponent topLetfView) {
		JComponent old = this.topLeftView;
		this.topLeftView = topLetfView;
		getPropertyChangeSupport().firePropertyChange(TOP_LEFT_VIEW, old, topLetfView);
	}

	public JComponent getTopRightView() {
		return topRightView;
	}

	public void setTopRightView(JComponent topRightView) {
		JComponent old = this.topRightView;
		this.topRightView = topRightView;
		getPropertyChangeSupport().firePropertyChange(TOP_RIGHT_VIEW, old, topRightView);
	}

	public JComponent getBottomLeftView() {
		return bottomLeftView;
	}

	public void setBottomLeftView(JComponent bottomLetfView) {
		JComponent old = this.bottomLeftView;
		this.bottomLeftView = bottomLetfView;
		getPropertyChangeSupport().firePropertyChange(BOTTOM_LEFT_VIEW, old, bottomLetfView);
	}

	public JComponent getBottomRightView() {
		return bottomRightView;
	}

	public void setBottomRightView(JComponent bottomRightView) {
		JComponent old = this.bottomRightView;
		this.bottomRightView = bottomRightView;
		getPropertyChangeSupport().firePropertyChange(BOTTOM_RIGHT_VIEW, old, bottomRightView);
	}

	public JComponent getTopCenterView() {
		return topCenterView;
	}

	public void setTopCenterView(JComponent topCentralView) {
		JComponent old = this.topCenterView;
		this.topCenterView = topCentralView;
		getPropertyChangeSupport().firePropertyChange(TOP_CENTER_VIEW, old, topCentralView);
	}

	public JComponent getBottomCenterView() {
		return bottomCenterView;
	}

	public void setBottomCenterView(JComponent bottomCentralView) {
		JComponent old = this.bottomCenterView;
		this.bottomCenterView = bottomCentralView;
		getPropertyChangeSupport().firePropertyChange(BOTTOM_CENTER_VIEW, old, bottomCentralView);
	}

	public JComponent getMiddleLeftView() {
		return middleLeftView;
	}

	public void setMiddleLeftView(JComponent middleLeftView) {
		JComponent old = this.middleLeftView;
		this.middleLeftView = middleLeftView;
		getPropertyChangeSupport().firePropertyChange(MIDDLE_LEFT_VIEW, old, middleLeftView);
	}

	public JComponent getMiddleRightView() {
		return middleRightView;
	}

	public void setMiddleRightView(JComponent middleRightView) {
		JComponent old = this.middleRightView;
		this.middleRightView = middleRightView;
		getPropertyChangeSupport().firePropertyChange(MIDDLE_RIGHT_VIEW, old, middleRightView);
	}

	public void objectWasClicked(Object object, FlexoController controller) {
		// logger.info("FlexoPerspective: object was clicked: " + object);
	}

	public void objectWasRightClicked(Object object, FlexoController controller) {
		// logger.info("FlexoPerspective: object was right-clicked: " + object);
	}

	public void objectWasDoubleClicked(Object object, FlexoController controller) {
		// logger.info("FlexoPerspective: object was double-clicked: " + object);
	}

	public void focusOnObject(FlexoObject object) {
		logger.info("NOT IMPLEMENTED: focusOnObject " + object);
	}

	// Handle natures for FlexoProject

	public List<ProjectNature> getSpecificNaturesForProject(FlexoProject project) {
		List<ProjectNature> returned = new ArrayList<ProjectNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificProjectNatures(project));
		}
		return returned;
	}

	public ModuleView<FlexoProject> getModuleViewForProject(FlexoProject project, ProjectNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<FlexoProject> returned = tac.createFlexoProjectModuleViewForSpecificNature(project, nature, controller, this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// Handle natures for View

	public List<ViewNature> getSpecificNaturesForView(View view) {
		List<ViewNature> returned = new ArrayList<ViewNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificViewNatures(view));
		}
		return returned;
	}

	public ModuleView<View> getModuleViewForView(View view, ViewNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<View> returned = tac.createViewModuleViewForSpecificNature(view, nature, controller, this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// Handle natures for VirtualModelInstance

	public List<VirtualModelInstanceNature> getSpecificNaturesForVirtualModelInstance(VirtualModelInstance vmi) {
		List<VirtualModelInstanceNature> returned = new ArrayList<VirtualModelInstanceNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificVirtualModelInstanceNatures(vmi));
		}
		return returned;
	}

	public ModuleView<VirtualModelInstance> getModuleViewForVirtualModelInstance(VirtualModelInstance vmi, VirtualModelInstanceNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<VirtualModelInstance> returned = tac.createVirtualModelInstanceModuleViewForSpecificNature(vmi, nature, controller,
					this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// Handle natures for FlexoConceptInstance

	public List<FlexoConceptInstanceNature> getSpecificNaturesForFlexoConceptInstance(FlexoConceptInstance vmi) {
		List<FlexoConceptInstanceNature> returned = new ArrayList<FlexoConceptInstanceNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificFlexoConceptInstanceNatures(vmi));
		}
		return returned;
	}

	public ModuleView<FlexoConceptInstance> getModuleViewForFlexoConceptInstance(FlexoConceptInstance vmi, FlexoConceptInstanceNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<FlexoConceptInstance> returned = tac.createFlexoConceptInstanceModuleViewForSpecificNature(vmi, nature, controller,
					this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// Handle natures for ViewPoint

	public List<ViewPointNature> getSpecificNaturesForViewPoint(ViewPoint view) {
		List<ViewPointNature> returned = new ArrayList<ViewPointNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificViewPointNatures(view));
		}
		return returned;
	}

	public ModuleView<ViewPoint> getModuleViewPointForViewPoint(ViewPoint view, ViewPointNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<ViewPoint> returned = tac.createViewPointModuleViewPointForSpecificNature(view, nature, controller, this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// Handle natures for VirtualModel

	public List<VirtualModelNature> getSpecificNaturesForVirtualModel(VirtualModel vmi) {
		List<VirtualModelNature> returned = new ArrayList<VirtualModelNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificVirtualModelNatures(vmi));
		}
		return returned;
	}

	public ModuleView<VirtualModel> getModuleViewForVirtualModel(VirtualModel vmi, VirtualModelNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<VirtualModel> returned = tac.createVirtualModelModuleViewForSpecificNature(vmi, nature, controller, this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	// Handle natures for FlexoConcept

	public List<FlexoConceptNature> getSpecificNaturesForFlexoConcept(FlexoConcept vmi) {
		List<FlexoConceptNature> returned = new ArrayList<FlexoConceptNature>();
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			returned.addAll(tac.getSpecificFlexoConceptNatures(vmi));
		}
		return returned;
	}

	public ModuleView<FlexoConcept> getModuleViewForFlexoConcept(FlexoConcept vmi, FlexoConceptNature nature) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterService taService = controller.getApplicationContext().getTechnologyAdapterService();
		for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
			TechnologyAdapterController<?> tac = tacService.getTechnologyAdapterController(ta);
			ModuleView<FlexoConcept> returned = tac.createFlexoConceptModuleViewForSpecificNature(vmi, nature, controller, this);
			if (returned != null) {
				return returned;
			}
		}
		return null;
	}

	public abstract String getWindowTitleforObject(FlexoObject object, FlexoController controller);

}
