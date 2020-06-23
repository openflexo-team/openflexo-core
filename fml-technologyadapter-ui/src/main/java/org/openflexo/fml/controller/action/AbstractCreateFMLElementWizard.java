/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.util.logging.Logger;

import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateTopLevelVirtualModel;
import org.openflexo.view.controller.FlexoController;

/**
 * Common stuff for wizards of {@link AbstractCreateFlexoConcept} action
 * 
 * @author sylvain
 *
 * @param <A>
 * @see CreateFlexoConcept
 * @see CreateContainedVirtualModel
 * @see CreateTopLevelVirtualModel
 */
public abstract class AbstractCreateFMLElementWizard<A extends FlexoAction<A, T1, T2>, T1 extends FlexoConceptObject, T2 extends FMLObject>
		extends FlexoActionWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFMLElementWizard.class.getPackage().getName());

	public AbstractCreateFMLElementWizard(A action, FlexoController controller) {
		super(action, controller);
	}

	public T1 getFocusedObject() {
		return getAction().getFocusedObject();
	}

	public VirtualModel getVirtualModel() {
		return getFocusedObject().getOwningVirtualModel();
	}

	public FlexoConcept getFlexoConcept() {
		return getFocusedObject().getFlexoConcept();
	}

}
