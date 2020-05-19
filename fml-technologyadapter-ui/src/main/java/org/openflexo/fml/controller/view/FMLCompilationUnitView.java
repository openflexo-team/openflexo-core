/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Freemodellingeditor, a component of the software infrastructure 
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

package org.openflexo.fml.controller.view;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.fml.controller.FMLTechnologyPerspective;
import org.openflexo.fml.controller.widget.fmleditor.FMLEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;
import org.openflexo.view.controller.model.FlexoPerspective;

@SuppressWarnings("serial")
public class FMLCompilationUnitView extends JPanel implements ModuleView<FMLCompilationUnit>, PropertyChangeListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLCompilationUnitView.class.getPackage().getName());

	private final CompilationUnitResource fmlResource;
	private final FMLTechnologyPerspective perspective;
	private final FMLEditor editor;
	private final FlexoController flexoController;

	// private final JPanel bottomPanel;

	public FMLCompilationUnitView(CompilationUnitResource fmlResource, FlexoController flexoController,
			FMLTechnologyPerspective perspective) {
		super();
		setLayout(new BorderLayout());
		this.fmlResource = fmlResource;
		this.perspective = perspective;
		this.flexoController = flexoController;

		editor = new FMLEditor(fmlResource, flexoController);

		// add(editor.getToolsPanel(), BorderLayout.NORTH);
		add(editor, BorderLayout.CENTER);

		// bottomPanel = new JPanel(new BorderLayout());
		// bottomPanel.add(editor.getFlexoController().makeInfoLabel(), BorderLayout.CENTER);
		// add(bottomPanel, BorderLayout.SOUTH);

		validate();

		getRepresentedObject().getPropertyChangeSupport().addPropertyChangeListener(getRepresentedObject().getDeletedProperty(), this);
	}

	public FMLEditor getEditor() {
		return editor;
	}

	public FlexoController getFlexoController() {
		return flexoController;
	}

	@Override
	public FMLTechnologyPerspective getPerspective() {
		return perspective;
	}

	@Override
	public void deleteModuleView() {
		getRepresentedObject().getPropertyChangeSupport().removePropertyChangeListener(getRepresentedObject().getDeletedProperty(), this);
		getFlexoController().removeModuleView(this);
		getEditor().delete();
	}

	@Override
	public FMLCompilationUnit getRepresentedObject() {
		return fmlResource.getCompilationUnit();
	}

	@Override
	public boolean isAutoscrolled() {
		return true;
	}

	@Override
	public void willHide() {
		System.out.println("FMLCompilationUnitView WILL HIDE !!!!!!");
	}

	@Override
	public void willShow() {

		System.out.println("FMLCompilationUnitView WILL SHOW !!!!!!");
		getPerspective().focusOnObject(getRepresentedObject());
	}

	public FMLTechnologyAdapterController getDiagramTechnologyAdapterController(FlexoController controller) {
		TechnologyAdapterControllerService tacService = controller.getApplicationContext().getTechnologyAdapterControllerService();
		return tacService.getTechnologyAdapterController(FMLTechnologyAdapterController.class);
	}

	@Override
	public void show(final FlexoController controller, FlexoPerspective perspective) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getRepresentedObject() && evt.getPropertyName().equals(getRepresentedObject().getDeletedProperty())) {
			deleteModuleView();
		}
	}
}
