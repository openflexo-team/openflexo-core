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
import java.awt.Rectangle;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.icon.IconLibrary;
import org.openflexo.inspector.ModuleInspectorController.InspectedObjectChanged;
import org.openflexo.swing.WindowSynchronizer;
import org.openflexo.utils.WindowBoundsSaver;
import org.openflexo.view.controller.FlexoController;

/**
 * Represent a JDialog showing inspector for the selection managed by an instance of ModuleInspectorController
 * 
 * @author sylvain
 * 
 */
public class FIBInspectorDialog extends JDialog implements Observer {

	static final Logger logger = Logger.getLogger(FIBInspectorDialog.class.getPackage().getName());

	private static final String INSPECTOR_TITLE = "Inspector";

	private static final WindowSynchronizer inspectorSync = new WindowSynchronizer();

	private FIBInspectorPanel inspectorPanel;

	private ModuleInspectorController inspectorController;

	public FIBInspectorDialog(ModuleInspectorController inspectorController) {
		super(inspectorController.getFlexoController().getFlexoFrame(), INSPECTOR_TITLE, false);
		this.inspectorController = inspectorController;
		inspectorSync.addToSynchronizedWindows(this);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		inspectorController.addObserver(this);
		inspectorPanel = new FIBInspectorPanel(inspectorController);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(inspectorPanel, BorderLayout.CENTER);
		setResizable(true);
		new WindowBoundsSaver(this, "JFIBInspector", new Rectangle(800, 400, 400, 400), inspectorController.getFlexoController());
	}

	public void delete() {
		setVisible(false);
		dispose();
		inspectorController.deleteObserver(this);
		inspectorSync.removeFromSynchronizedWindows(this);
		inspectorController = null;
		inspectorPanel = null;
	}

	/*public void inspectObject(Object object) {
		if (inspectorPanel.inspectObject(object)) {
			if (object instanceof FlexoObject && (object instanceof DiagramShape || object instanceof DiagramConnector)
					&& ((FlexoObject) object).getFlexoConceptReferences().size() > 0) {
				String newTitle = ((FlexoObject) object).getFlexoConceptReferences().firstElement().getFlexoConcept()
						.getInspector().getInspectorTitle();
				setTitle(newTitle);
			} else {
				JFIBInspector newInspector = inspectorController.inspectorForObject(object);
				setTitle(newInspector.getParameter("title"));
			}
		}
	}*/

	@Override
	public void update(Observable o, Object notification) {
		/*if (notification instanceof EmptySelectionActivated) {
			setTitle(INSPECTOR_TITLE);
		} else if (notification instanceof MultipleSelectionActivated) {
			setTitle(INSPECTOR_TITLE);
		} else*/if (notification instanceof InspectedObjectChanged) {
			Object object = ((InspectedObjectChanged) notification).getInspectedObject();
			if (object instanceof FlexoConceptInstance) {
				if (((FlexoConceptInstance) object).getFlexoConcept() != null) {
					String newTitle = ((FlexoConceptInstance) object).getFlexoConcept().getInspector().getInspectorTitle();
					setTitle(newTitle);
				}
			} /*else if (getInspectorPanel() != null && getInspectorPanel().getCurrentlyDisplayedInspector() != null) {
				setTitle(getInspectorPanel().getCurrentlyDisplayedInspector().getParameter("title"));
				}*/else if (object instanceof FlexoResource) {
				FlexoResource<?> resource = (FlexoResource<?>) object;
				setTitle(resource.getResourceDataClass().getSimpleName() + " (unloaded)");
			} else if (object instanceof FlexoObject /*&& (object instanceof DiagramShape || object instanceof DiagramConnector)*/
			/*&& ((FlexoObject) object).getFlexoConceptReferences().size() > 0*/) {
				// String newTitle = ((FlexoObject) object).getFlexoConceptReferences().get(0).getObject().getFlexoConcept().getInspector()
				// .getInspectorTitle();
				// setTitle(newTitle);
				setTitle(((FlexoObject) object).getImplementedInterface().getSimpleName());
			}
			if (object instanceof FlexoResource) {
				FlexoResource<?> resource = (FlexoResource<?>) object;
				if (resource instanceof TechnologyAdapterResource) {
					ImageIcon imageIcon = FlexoController
							.statelessIconForTechnologyAdapterResource((TechnologyAdapterResource<?, ?>) resource);
					if (imageIcon != null) {
						setIconImage(imageIcon.getImage());
					}
				} else {
					setIconImage(IconLibrary.OPENFLEXO_NOTEXT_16.getImage());
				}
			} else if (object instanceof FlexoObject) {
				ImageIcon icon = FlexoController.statelessIconForObject(object);
				if (icon != null) {
					setIconImage(icon.getImage());
				}

				/*if (getInspectorPanel() != null && getInspectorPanel().getCurrentlyDisplayedInspector() != null
						&& object.getClass() != getInspectorPanel().getCurrentlyDisplayedInspector().getDataClass()
						&& !object.getClass().getSimpleName().contains("javassist")) {
					setTitle(getInspectorPanel().getCurrentlyDisplayedInspector().getParameter("title") + " : "
							+ object.getClass().getSimpleName());
				}*/
			}

		}
	}

	public FIBInspectorPanel getInspectorPanel() {
		return inspectorPanel;
	}
}
