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

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.swing.view.JFIBView;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.gina.view.FIBView;
import org.openflexo.inspector.ModuleInspectorController.EmptySelectionActivated;
import org.openflexo.inspector.ModuleInspectorController.InspectedObjectChanged;
import org.openflexo.inspector.ModuleInspectorController.InspectorSwitching;
import org.openflexo.inspector.ModuleInspectorController.MultipleSelectionActivated;
import org.openflexo.inspector.ModuleInspectorController.NewInspectorsLoaded;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Represent a {@link JPanel} showing default inspector tab for the selection managed by an instance of {@link ModuleInspectorController}
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public class FIBFlexoConceptInstanceInspectorPanel extends JPanel implements Observer {

	static final Logger logger = Logger.getLogger(FIBFlexoConceptInstanceInspectorPanel.class.getPackage().getName());

	private final JPanel EMPTY_CONTENT;
	private final JPanel MULTIPLE_SELECTION_CONTENT;
	private final Map<FIBPanel, FIBView<?, ?>> inspectorViews;
	private final ModuleInspectorController inspectorController;
	private FIBPanel currentlyDisplayedInspector;
	private JFIBView<?, ?> currentInspectorView = null;

	public FIBFlexoConceptInstanceInspectorPanel(ModuleInspectorController inspectorController) {

		super(new BorderLayout());
		this.inspectorController = inspectorController;

		inspectorController.addObserver(this);

		inspectorViews = new Hashtable<>();

		resetViews();

		EMPTY_CONTENT = new JPanel(new BorderLayout());
		EMPTY_CONTENT.add(new JLabel("No selection", SwingConstants.CENTER), BorderLayout.CENTER);
		MULTIPLE_SELECTION_CONTENT = new JPanel(new BorderLayout());
		MULTIPLE_SELECTION_CONTENT.add(new JLabel("Multiple selection", SwingConstants.CENTER), BorderLayout.CENTER);

		switchToEmptyContent();

	}

	private void resetViews() {

		if (inspectorViews != null) {
			for (FIBView<?, ?> v : inspectorViews.values()) {
				FlexoLocalization.removeFromLocalizationListeners(v);
			}
			inspectorViews.clear();
		}

		/*for (Class<?> c : inspectorController.getInspectors().keySet()) {
			JFIBInspector inspector = inspectorController.getInspectors().get(c);
			FIBViewImpl<?, ?, ?> inspectorView = FIBController.makeView(inspector, FlexoLocalization.getMainLocalizer());
			FlexoLocalization.addToLocalizationListeners(inspectorView);
			inspectorViews.put(inspector, inspectorView);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Initialized view for inspector for " + inspector.getDataClass());
			}
		}*/

	}

	private FIBView<?, ?> buildViewFor(FIBInspector inspector) {

		// System.out.println("------ On construit une FIBView<?, ?> pour l'inspecteur " + inspector);
		// System.out.println("locales=" + inspector.getLocales());

		FIBView<?, ?> inspectorView = FIBController.makeView(inspector/*.getTabPanel().getSubComponents().get(0)*/,
				SwingViewFactory.INSTANCE, inspector.getLocales(), null, false);
		FIBController controller = inspectorView.getController();
		if (controller instanceof FlexoFIBController) {
			((FlexoFIBController) controller).setFlexoController(inspectorController.getFlexoController());
		}
		FlexoLocalization.addToLocalizationListeners(inspectorView);
		inspectorViews.put(inspector, inspectorView);
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Initialized view for inspector for " + inspector.getDataClass());
		}
		return inspectorView;
	}

	public void delete() {
		inspectorController.deleteObserver(this);
		for (FIBView<?, ?> v : inspectorViews.values()) {
			v.getController().delete();
			FlexoLocalization.removeFromLocalizationListeners(v);
		}
		inspectorViews.clear();
	}

	private void switchToEmptyContent() {
		currentInspectorView = null;
		currentlyDisplayedInspector = null;
		removeAll();
		add(EMPTY_CONTENT, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private void switchToMultipleSelection() {
		currentInspectorView = null;
		currentlyDisplayedInspector = null;
		removeAll();
		add(MULTIPLE_SELECTION_CONTENT, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private void switchToInspector(FIBInspector newInspector) {

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("switchToInspector " + newInspector + " for " + this);
		}

		currentlyDisplayedInspector = newInspector;

		JFIBView<?, ?> view = (JFIBView<?, ?>) viewForInspector(newInspector);

		FlexoFIBController controller = (FlexoFIBController) view.getController();
		controller.setFlexoController(inspectorController.getFlexoController());

		JFIBView<?, ?> defaultTabView = (JFIBView<?, ?>) view.getController()
				.viewForComponent(newInspector.getTabPanel().getSubComponents().get(0));

		if (view != null) {
			currentInspectorView = defaultTabView;
			removeAll();
			add(currentInspectorView.getResultingJComponent(), BorderLayout.CENTER);
			revalidate();
			repaint();
		}
		else {
			logger.warning("No inspector view for " + newInspector);
			switchToEmptyContent();
		}
	}

	private void switchToObject(Object inspectedObject) {

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("switchToObject " + inspectedObject + " for " + this);
		}
		if (currentInspectorView != null) {
			currentInspectorView.getController().setDataObject(inspectedObject);
		}
	}

	private FIBView<?, ?> viewForInspector(FIBInspector inspector) {
		FIBView<?, ?> returned = inspectorViews.get(inspector);
		if (returned == null) {
			returned = buildViewFor(inspector);
		}
		return returned;
	}

	@Override
	public void update(Observable o, Object notification) {
		// logger.info("JFIBInspectorController received: "+selection);
		if (notification instanceof NewInspectorsLoaded) {
			resetViews();
		}
		if (notification instanceof EmptySelectionActivated) {
			switchToEmptyContent();
		}
		else if (notification instanceof MultipleSelectionActivated) {
			switchToMultipleSelection();
		}
		else if (notification instanceof InspectorSwitching) {
			switchToInspector(((InspectorSwitching) notification).getNewInspector());
		}
		else if (notification instanceof InspectedObjectChanged) {
			switchToObject(((InspectedObjectChanged) notification).getInspectedObject());
		}
	}

	public void refreshComponentVisibility() {
		if (currentInspectorView != null) {
			currentInspectorView.update();
		}
	}

	public FIBPanel getCurrentlyDisplayedInspector() {
		return currentlyDisplayedInspector;
	}
}
