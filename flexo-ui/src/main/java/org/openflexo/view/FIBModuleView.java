/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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

package org.openflexo.view;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.GraphicalFlexoObserver;
import org.openflexo.foundation.task.Progress;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.listener.FIBSelectionListener;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.selection.SelectionListener;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * Please comment this class
 * 
 * @author sguerin
 * 
 */
public abstract class FIBModuleView<O extends FlexoObject> extends SelectionSynchronizedFIBView
		implements SelectionSynchronizedModuleView<O>, GraphicalFlexoObserver, FIBSelectionListener, Scrollable {
	static final Logger logger = Logger.getLogger(FIBModuleView.class.getPackage().getName());

	// private Object representedObject;
	// private FlexoController controller;
	// private FIBViewImpl fibView;

	public FIBModuleView(O representedObject, FlexoController controller, Resource fibResource, LocalizedDelegate locales) {
		this(representedObject, controller, fibResource, locales, false);
	}

	public FIBModuleView(O representedObject, FlexoController controller, Resource fibResource, LocalizedDelegate locales,
			boolean addScrollBar) {
		this(representedObject, controller, controller.getApplicationFIBLibraryService().retrieveFIBComponent(fibResource), locales,
				addScrollBar);
		controller.willLoad(fibResource);
	}

	protected FIBModuleView(O representedObject, FlexoController controller, FIBComponent fibComponent, LocalizedDelegate locales,
			boolean addScrollBar) {
		super(representedObject, controller, fibComponent, locales, addScrollBar);
		getRepresentedObject().getPropertyChangeSupport().addPropertyChangeListener(getRepresentedObject().getDeletedProperty(), this);
		Progress.progress("instantiating_fib_component");
	}

	@Override
	public void initializeFIBComponent() {
		Progress.progress("initializing_fib_component");
		super.initializeFIBComponent();
	}

	@Override
	protected FlexoFIBController createFibController(FIBComponent fibComponent, FlexoController controller, LocalizedDelegate locales) {
		Progress.progress("initializing_fib_controller");
		return super.createFibController(fibComponent, controller, locales);
	}

	@Override
	public void setDataObject(Object object) {
		Progress.progress("set_data_object");
		super.setDataObject(object);
	}

	@Override
	public void deleteModuleView() {
		System.out.println("deleteModuleView() in FIBModuleView");
		getFlexoController().removeModuleView(this);
		deleteView();
		getFIBController().delete();
		getRepresentedObject().getPropertyChangeSupport().removePropertyChangeListener(getRepresentedObject().getDeletedProperty(), this);
		if (getFlexoController() != null) {
			getFlexoController().removeModuleView(this);
			if (getFlexoController().getSelectionManager() != null) {
				getFlexoController().getSelectionManager().removeFromSelectionListeners(this);
			}
		}
	}

	@Override
	public List<SelectionListener> getSelectionListeners() {
		return Arrays.asList((SelectionListener) this);
	}

	@Override
	public abstract FlexoPerspective getPerspective();

	@Override
	public void willHide() {
		// System.out.println("************ ModuleView willHide() " + this + " devient INVISIBLE");
		// setVisible(false);
	}

	@Override
	public void willShow() {
		// System.out.println("************ ModuleView willShow()" + this + " devient VISIBLE");
		setVisible(true);
		if (getSelectionManager() != null && getSelectionManager().getSelection().size() == 1
				&& getSelectionManager().getSelection().get(0) == getRepresentedObject()) {
			fireObjectSelected(getSelectionManager().getSelection().get(0));
		}
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
		// Override when required
	}

	@Override
	public O getRepresentedObject() {
		return (O) getDataObject();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		switch (orientation) {
			case SwingConstants.VERTICAL:
				return visibleRect.height / 10;
			case SwingConstants.HORIZONTAL:
				return visibleRect.width / 10;
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		switch (orientation) {
			case SwingConstants.VERTICAL:
				return visibleRect.height;
			case SwingConstants.HORIZONTAL:
				return visibleRect.width;
			default:
				throw new IllegalArgumentException("Invalid orientation: " + orientation);
		}
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getSource() == getRepresentedObject() && evt.getPropertyName().equals(getRepresentedObject().getDeletedProperty())) {
			// This event matches a deletion, delete ModuleView
			deleteModuleView();
		}
		super.propertyChange(evt);
	}
}
