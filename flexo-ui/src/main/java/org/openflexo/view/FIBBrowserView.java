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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.model.listener.FIBSelectionListener;
import org.openflexo.gina.model.widget.FIBBrowser;
import org.openflexo.gina.model.widget.FIBBrowserAction;
import org.openflexo.gina.model.widget.FIBBrowserElement;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.prefs.ApplicationFIBLibraryService;
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
	// private FIBViewImpl fibView;

	public FIBBrowserView(O representedObject, FlexoController controller, Resource fibResource, LocalizedDelegate locales) {
		this(representedObject, controller, fibResource, locales, false);
	}

	public FIBBrowserView(O representedObject, FlexoController controller, Resource fibResource, LocalizedDelegate locales,
			boolean addScrollBar) {
		this(representedObject, controller,
				controller != null ? controller.getApplicationFIBLibraryService().retrieveFIBComponent(fibResource)
						: ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(fibResource),
				locales, addScrollBar);
		if (controller != null) {
			controller.willLoad(fibResource);
		}
	}

	public FIBBrowserView(O representedObject, ApplicationFIBLibraryService appFIBLibraryService, Resource fibResource,
			LocalizedDelegate locales, boolean addScrollBar) {
		this(representedObject, null, appFIBLibraryService.retrieveFIBComponent(fibResource), locales, addScrollBar);
		if (controller != null) {
			controller.willLoad(fibResource);
		}
	}

	protected FIBBrowserView(O representedObject, FlexoController controller, FIBComponent fibComponent, LocalizedDelegate locales,
			boolean addScrollBar) {
		super(representedObject, controller, fibComponent, locales, addScrollBar);
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
			browser.setClickAction(new DataBinding<>("controller.singleClick(" + browser.getName() + ".selected)"));
		}
		if (!browser.getDoubleClickAction().isSet() || !browser.getDoubleClickAction().isValid()) {
			browser.setDoubleClickAction(new DataBinding<>("controller.doubleClick(" + browser.getName() + ".selected)"));
		}
		if (!browser.getRightClickAction().isSet() || !browser.getRightClickAction().isValid()) {
			browser.setRightClickAction(new DataBinding<>("controller.rightClick(" + browser.getName() + ".selected,event)"));
		}

		for (FIBBrowserElement el : browser.getElements()) {
			if (el.getDataType() != null) {
				if (TypeUtils.isTypeAssignableFrom(FlexoObject.class, el.getDataType())) {
					List<FlexoActionFactory<?, ?, ?>> actionList = FlexoObjectImpl
							.getActionList((Class<? extends FlexoObject>) TypeUtils.getBaseClass(el.getDataType()));
					for (FlexoActionFactory<?, ?, ?> actionType : actionList) {
						boolean foundAction = false;
						for (FIBBrowserAction action : el.getActions()) {
							if (action instanceof FIBBrowserActionAdapter) {
								if (((FIBBrowserActionAdapter) action).getFlexoActionType() == actionType) {
									// This action is already present
									foundAction = true;
									break;
								}
							}
						}
						if (!foundAction) {
							try {
								el.addToActions(
										FIBBrowserActionAdapterImpl.makeFIBBrowserActionAdapter(actionType, this, getFlexoController()));
							} catch (ModelDefinitionException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void initializeFIBComponent() {
		super.initializeFIBComponent();

		FIBBrowser browser = retrieveFIBBrowser((FIBContainer) getFIBComponent());

		if (browser != null) {
			bindFlexoActionsToBrowser(browser);
		}

	}

}
