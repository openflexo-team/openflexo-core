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
package org.openflexo.view;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBBrowser;
import org.openflexo.fib.model.FIBBrowserElement;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBContainer;
import org.openflexo.fib.model.listener.FIBSelectionListener;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.rm.Resource;
import org.openflexo.view.FIBBrowserActionAdapter.FIBBrowserActionAdapterImpl;
import org.openflexo.view.controller.FlexoController;

/**
 * Implements a view showing a simple browser using FIB technology
 * 
 * @author sguerin
 * 
 */
public abstract class FIBBrowserView<O> extends SelectionSynchronizedFIBView implements FIBSelectionListener {
	static final Logger logger = Logger.getLogger(FIBBrowserView.class.getPackage().getName());

	// private O representedObject;
	// private FlexoController controller;
	// private FIBView fibView;

	public FIBBrowserView(O representedObject, FlexoController controller, Resource fibResource) {
		this(representedObject, controller, fibResource, false);
	}

	public FIBBrowserView(O representedObject, FlexoController controller, Resource fibResource, boolean addScrollBar) {
		this(representedObject, controller, FIBLibrary.instance().retrieveFIBComponent(fibResource), addScrollBar);
		controller.willLoad(fibResource);
	}

	// Removed as we should only used Resource everywhere
	/*
	public FIBBrowserView(O representedObject, FlexoController controller, String fibResourcePath) {
		this(representedObject, controller, fibResourcePath, false, controller.willLoad(fibResourcePath));
	}

	public FIBBrowserView(O representedObject, FlexoController controller, String fibResourcePath, FlexoProgress progress) {
		this(representedObject, controller, fibResourcePath, false, progress);
	}

	public FIBBrowserView(O representedObject, FlexoController controller, String fibResourcePath, boolean addScrollBar,
			FlexoProgress progress) {
		this(representedObject, controller, FIBLibrary.instance().retrieveFIBComponent(fibResourcePath), addScrollBar, progress);
	}
	*/

	protected FIBBrowserView(O representedObject, FlexoController controller, FIBComponent fibComponent, boolean addScrollBar) {
		super(representedObject, controller, fibComponent, addScrollBar);
	}

	@Override
	public O getDataObject() {
		return (O) super.getDataObject();
	}

	public O getRootObject() {
		return getDataObject();
	}

	public void setRootObject(O obj) {
		setDataObject(obj);
	}

	/**
	 * Internally called to automatically add actions to a FIBBrowser.<br>
	 * Bind "click" actions to controller.<br>
	 * Also add all relevant {@link FlexoAction} to each FIBBrowserElement regarding its type
	 * 
	 * @param browser
	 */
	protected void bindFlexoActionsToBrowser(FIBBrowser browser) {
		if (browser == null) {
			logger.warning("Could not retrieve FIBBrowser for component " + getFIBComponent());
			return;
		}
		if (!browser.getClickAction().isSet() || !browser.getClickAction().isValid()) {
			browser.setClickAction(new DataBinding<Object>("controller.singleClick(" + browser.getName() + ".selected)"));
		}
		if (!browser.getDoubleClickAction().isSet() || !browser.getDoubleClickAction().isValid()) {
			browser.setDoubleClickAction(new DataBinding<Object>("controller.doubleClick(" + browser.getName() + ".selected)"));
		}
		if (!browser.getRightClickAction().isSet() || !browser.getRightClickAction().isValid()) {
			browser.setRightClickAction(new DataBinding<Object>("controller.rightClick(" + browser.getName() + ".selected,event)"));
		}

		for (FIBBrowserElement el : browser.getElements()) {
			if (el.getDataClass() != null) {
				if (FlexoObject.class.isAssignableFrom(el.getDataClass())) {
					List<FlexoActionType<?, ?, ?>> actionList = FlexoObjectImpl.getActionList((Class<? extends FlexoObject>) el
							.getDataClass());
					for (FlexoActionType<?, ?, ?> actionType : actionList) {
						el.addToActions(FIBBrowserActionAdapterImpl.makeFIBBrowserActionAdapter(actionType, this));
					}
				}
			}
		}
	}

	@Override
	protected void initializeFIBComponent() {
		super.initializeFIBComponent();

		FIBBrowser browser = retrieveFIBBrowser((FIBContainer) getFIBComponent());

		if (browser != null) {
			bindFlexoActionsToBrowser(browser);
		}

	}

	protected static FIBBrowser retrieveFIBBrowser(FIBContainer component) {
		if (component == null) {
			return null;
		}
		List<FIBComponent> listComponent = component.getAllSubComponents();
		for (FIBComponent c : listComponent) {
			if (c instanceof FIBBrowser) {
				return (FIBBrowser) c;
			}
		}
		return null;
	}

	protected static FIBBrowser retrieveFIBBrowserNamed(FIBContainer component, String name) {
		if (component == null) {
			return null;
		}
		List<FIBComponent> listComponent = component.getAllSubComponents();
		for (FIBComponent c : listComponent) {
			if ((c instanceof FIBBrowser) && (c.getName() != null) && (c.getName().equals(name))) {
				return (FIBBrowser) c;
			}
		}
		return null;
	}

}
