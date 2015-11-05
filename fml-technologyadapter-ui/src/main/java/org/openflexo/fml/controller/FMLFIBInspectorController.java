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

package org.openflexo.fml.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openflexo.connie.BindingVariable;
import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.model.FIBModelFactory;
import org.openflexo.fib.utils.FIBInspector;
import org.openflexo.fib.view.GinaViewFactory;
import org.openflexo.foundation.DataModification;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoObservable;
import org.openflexo.foundation.FlexoProperty;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.action.AddFlexoProperty;
import org.openflexo.foundation.resource.ResourceLoaded;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.exceptions.ModelDefinitionException;

/**
 * Represents the controller of a FIBInspector (FIBComponent) in the context of FML
 * 
 * @author sylvain
 * 
 */
public class FMLFIBInspectorController extends FMLFIBController {

	private static final Logger logger = FlexoLogger.getLogger(FMLFIBInspectorController.class.getPackage().getName());

	private static FIBModelFactory INSPECTOR_FACTORY;

	static {
		try {
			INSPECTOR_FACTORY = new FIBModelFactory(FIBInspector.class);
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FMLFIBInspectorController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
	}

	public boolean displayInspectorTabForContext(String context) {
		if (getFlexoController() != null) {
			return getFlexoController().displayInspectorTabForContext(context);
		}
		else {
			return true;
		}
	}

	@Override
	public Object getValue(BindingVariable variable) {
		/*if (variable instanceof FlexoConceptInstanceBindingVariable) {
			if (getDataObject() instanceof FlexoObject) {
				List<FlexoObjectReference<FlexoConceptInstance>> refs = ((FlexoObject) getDataObject()).getFlexoConceptReferences();
				if (refs != null && ((FlexoConceptInstanceBindingVariable) variable).getIndex() < refs.size()) {
					return refs.get(((FlexoConceptInstanceBindingVariable) variable).getIndex()).getObject();
				}
			}
		}*/
		return super.getValue(variable);
	}

	@Override
	protected void openFIBEditor(FIBComponent component, final MouseEvent event) {
		if (component instanceof FIBInspector) {
			JPopupMenu popup = new JPopupMenu();
			for (FIBInspector current : getFlexoController().getModuleInspectorController().inspectorsForClass(component.getDataClass())) {
				JMenuItem menuItem = new JMenuItem(current.getResource().getRelativePath());
				// We dont use existing inspector which is already
				// aggregated !!!
				final FIBInspector inspectorToOpen = (FIBInspector) FIBLibrary.instance().retrieveFIBComponent(current.getResource(), false,
						INSPECTOR_FACTORY);
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						FMLFIBInspectorController.super.openFIBEditor(inspectorToOpen, event);
					}
				});
				popup.add(menuItem);
			}
			popup.show(event.getComponent(), event.getX(), event.getY());

		}
		else {
			super.openFIBEditor(component, event);
		}
	}

	@Override
	public void update(FlexoObservable o, DataModification dataModification) {
		super.update(o, dataModification);
		if (dataModification instanceof ResourceLoaded) {
			// System.out.println("Detected resource being loaded !");
		}
	}

	public void addCustomProperty(FlexoObject object) {
		if (object instanceof InnerResourceData) {
			System.out.println("Creating property for object " + object);
			AddFlexoProperty action = AddFlexoProperty.actionType.makeNewAction(object, null, getEditor());
			action.doAction();
		}
	}

	public void removeCustomProperty(FlexoProperty property) {
		System.out.println("Deleting property " + property + " for object " + property.getOwner());
		property.delete();
	}

}
