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
package org.openflexo.view.controller;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import org.openflexo.fib.swing.FIBJPanel;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.localization.FlexoLocalization;

/**
 * Widget allowing to edit parameters of an FlexoBehaviour
 * 
 * @author sguerin
 * 
 */
public class FIBParametersRetrieverWidget extends FIBJPanel<FlexoBehaviourAction> {

	static final Logger logger = Logger.getLogger(FIBParametersRetrieverWidget.class.getPackage().getName());

	public FIBParametersRetrieverWidget(FlexoBehaviourAction action) {
		super((new ParametersRetriever(action)).makeFIB(false, false), action, FlexoLocalization.getMainLocalizer());
	}

	@Override
	public Class<FlexoBehaviourAction> getRepresentedType() {
		return FlexoBehaviourAction.class;
	}

	@Override
	public void fireEditedObjectChanged() {
		FlexoBehaviourAction action = getEditedObject();
		if (action != null) {
			fibComponent = (new ParametersRetriever(action)).makeFIB(false, false);
			controller = makeFIBController(fibComponent, localizer);
			fibView = controller.buildView(fibComponent);
			removeAll();
			add(fibView.getResultingJComponent(), BorderLayout.CENTER);
			revalidate();
		}
		super.fireEditedObjectChanged();
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub

	}

}
