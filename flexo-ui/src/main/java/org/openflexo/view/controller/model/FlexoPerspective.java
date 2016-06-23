/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.view.controller.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
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
		return getController().getModuleLocales().localizedForKey(getName());
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

	public abstract String getWindowTitleforObject(FlexoObject object, FlexoController controller);

	/**
	 * Override when required
	 * 
	 * @param project
	 */
	public void updateEditor(FlexoEditor from, FlexoEditor to) {
		// Do nothing here
	}

	/**
	 * Hook triggered when a perspective is about to be shown
	 */
	public void willShow() {
	}
}
