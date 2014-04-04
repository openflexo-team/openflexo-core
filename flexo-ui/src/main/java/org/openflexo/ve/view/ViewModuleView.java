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
package org.openflexo.ve.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionSource;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.view.action.CreateBasicVirtualModelInstance;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;
import org.openflexo.view.listener.FlexoActionButton;

/**
 * @author vincent
 */
public class ViewModuleView extends JPanel implements ModuleView<View>, PropertyChangeListener, FlexoActionSource {

	private final View view;
	private JPanel panel;
	private final FlexoController controller;

	private final FlexoPerspective declaredPerspective;

	public ViewModuleView(View view, FlexoController controller, FlexoPerspective perspective) {
		super(new BorderLayout());
		declaredPerspective = perspective;
		this.view = view;
		this.controller = controller;
		view.getPropertyChangeSupport().addPropertyChangeListener(this);
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		// buttonPanel.add(new FlexoActionButton(CreateDiagram.actionType, this, controller));
		buttonPanel.add(new FlexoActionButton(CreateBasicVirtualModelInstance.actionType, this, controller));
		panel.add(buttonPanel);
		add(panel);
		revalidate();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					propertyChange(evt);
				}
			});
			return;
		}
		if (evt.getPropertyName().equals(FlexoObject.DELETED)) {
			deleteModuleView();
		}
	}

	@Override
	public void deleteModuleView() {
		view.getPropertyChangeSupport().removePropertyChangeListener(this);
		controller.removeModuleView(this);
		panel = null;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return declaredPerspective;
	}

	@Override
	public View getRepresentedObject() {
		return view;
	}

	@Override
	public void willHide() {
	}

	@Override
	public void willShow() {
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
	}

	/**
	 * Overrides getEditor
	 * 
	 * @see org.openflexo.foundation.action.FlexoActionSource#getEditor()
	 */
	@Override
	public FlexoEditor getEditor() {
		return controller.getEditor();
	}

	/**
	 * Overrides getFocusedObject
	 * 
	 * @see org.openflexo.foundation.action.FlexoActionSource#getFocusedObject()
	 */
	@Override
	public View getFocusedObject() {
		return view;
	}

	/**
	 * Overrides getGlobalSelection
	 * 
	 * @see org.openflexo.foundation.action.FlexoActionSource#getGlobalSelection()
	 */
	@Override
	public List<FlexoObject> getGlobalSelection() {
		return null;
	}

	@Override
	public boolean isAutoscrolled() {
		return false;
	}

}
