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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.module.FlexoModule.WelcomePanel;
import org.openflexo.swing.layout.MultiSplitLayout.Node;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;

public abstract class FlexoPerspective extends ControllerModelObject {

	private static final Logger logger = Logger.getLogger(FlexoPerspective.class.getPackage().getName());

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

	public FlexoPerspective(FlexoController controller) {
		super();
		this.controller = controller;
	}

	/**
	 * Unlocalized name
	 * 
	 * @return
	 */
	public abstract String getName();

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

	public void setupDefaultLayout(Node<?> layout) {
	}

	public abstract ImageIcon getActiveIcon();

	/**
	 * Return a boolean indicating if this {@link FlexoPerspective} handles supplied object by defining a {@link ModuleView} in which
	 * supplied object is representable either as a master object or as an object representable in related view
	 * 
	 * This method should be overriden
	 * 
	 * @param object
	 * @return
	 */
	public boolean isRepresentableInModuleView(FlexoObject object) {
		if (object instanceof WelcomePanel) {
			return true;
		}
		if (object instanceof FlexoProject) {
			return true;
		}
		return false;
	}

	/**
	 * Return {@link FlexoObject} for which this perspective defines a {@link ModuleView} where supplied object is also representable
	 * 
	 * This method should be overriden
	 * 
	 * @param object
	 * @return
	 */
	public FlexoObject getRepresentableMasterObject(FlexoObject object) {
		if (object instanceof WelcomePanel) {
			return object;
		}
		if (object instanceof FlexoProject) {
			return object;
		}
		return null;
	}

	/**
	 * Build a {@link ModuleView} for supplied object, considered as master object (main subject of the returned view)
	 * 
	 * @param object
	 * @param editable
	 * @return
	 */
	public final ModuleView<?> createModuleViewForMasterObject(FlexoObject object, boolean editable) {
		if (!editable) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Perspective " + getName()
						+ " does not override createModuleViewForObject(O object, FlexoController controller, boolean editable)");
			}
		}
		return createModuleViewForMasterObject(object);
	}

	/**
	 * Build a {@link ModuleView} for supplied object, considered as master object (main subject of the returned view)
	 * 
	 * @param object
	 * @return
	 */
	public ModuleView<?> createModuleViewForMasterObject(FlexoObject object) {

		if (object instanceof WelcomePanel) {
			return getController().makeWelcomePanel((WelcomePanel<?>) object, this);
		}
		if (object instanceof FlexoProject) {
			return getController().makeDefaultProjectView((FlexoProject<?>) object, this);
		}
		return new EmptyPanel<>(controller, this, object);
	}

	/**
	 * Return boolean indicating if this perspective handles supplied object (true if perspective may build and display a {@link ModuleView}
	 * representing supplied object)<br>
	 * 
	 * This method should be overriden<br>
	 * 
	 * Default returned value is true for View/FMLRTVirtualModelInstance/ViewPoint/VirtualModel/FlexoConcept objects<br>
	 * Default returned value depends on nature availability for FlexoProject/FlexoConceptInstance objects
	 * 
	 * @param object
	 * @return
	 */
	/*public boolean hasModuleViewForObject(FlexoObject object) {
		if (object instanceof FlexoProject) {
			return true;
		}
		if (object instanceof TechnologyObject) {
			return hasModuleViewForTechnologyObject((TechnologyObject<?>) object);
		}
		return false;
	}*/

	/**
	 * Return boolean indicating if this perspective handles supplied object (true if perspective may build and display a {@link ModuleView}
	 * representing supplied object)<br>
	 * 
	 * @param object
	 * @return
	 */
	/*public final <TA extends TechnologyAdapter<TA>> boolean hasModuleViewForTechnologyObject(TechnologyObject<TA> object) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		TechnologyAdapterController<TA> tac = tacService.getTechnologyAdapterController(object.getTechnologyAdapter());
		return tac.hasModuleViewForObject(object, controller,this);
	}*/

	/**
	 * Return default object to be displayed, given supplied object (which might be null)
	 * 
	 * @param proposedObject
	 * @param controller
	 * @return
	 */
	public FlexoObject getDefaultObject(FlexoObject proposedObject) {
		if (getRepresentableMasterObject(proposedObject) != null) {
			return getRepresentableMasterObject(proposedObject);
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
