/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.view.controller;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.gina.swing.view.JFIBView;
import org.openflexo.localization.FlexoLocalization;

/**
 * Widget allowing to edit parameters of an FlexoBehaviour
 * 
 * @author sguerin
 * 
 */
// Not sure it is still used
@Deprecated
public class FIBParametersRetrieverWidget extends FIBJPanel<FlexoBehaviourAction> {

	static final Logger logger = Logger.getLogger(FIBParametersRetrieverWidget.class.getPackage().getName());

	private final FlexoController flexoController;

	public FIBParametersRetrieverWidget(FlexoBehaviourAction action, FlexoController flexoController) {
		super((new ParametersRetriever(action, flexoController)).makeFIB(false, false), action, FlexoLocalization.getMainLocalizer());
		this.flexoController = flexoController;
	}

	@Override
	public Class<FlexoBehaviourAction> getRepresentedType() {
		return FlexoBehaviourAction.class;
	}

	@Override
	public void fireEditedObjectChanged() {
		FlexoBehaviourAction action = getEditedObject();
		if (action != null) {
			fibComponent = (new ParametersRetriever(action, flexoController)).makeFIB(false, false);
			controller = makeFIBController(fibComponent, localizer);
			fibView = (JFIBView<?, ?>) controller.buildView(fibComponent);
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
