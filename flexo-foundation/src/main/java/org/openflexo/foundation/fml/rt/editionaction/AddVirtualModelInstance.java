/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateBasicVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * This action is used to explicitely instanciate a new {@link VirtualModelInstance} in a given {@link View}
 * 
 * @author sylvain
 * 
 */

@FIBPanel("Fib/FML/AddVirtualModelInstancePanel.fib")
@ModelEntity
@ImplementationClass(AddVirtualModelInstance.AddVirtualModelInstanceImpl.class)
@XMLElement
@FML("AddVirtualModelInstance")
public interface AddVirtualModelInstance extends AddAbstractVirtualModelInstance<VirtualModelInstance> {

	public static abstract class AddVirtualModelInstanceImpl extends AddAbstractVirtualModelInstanceImpl<VirtualModelInstance>
			implements AddVirtualModelInstance {

		static final Logger logger = Logger.getLogger(AddVirtualModelInstance.class.getPackage().getName());

		@Override
		public VirtualModelInstance execute(RunTimeEvaluationContext evaluationContext) {
			return super.execute(evaluationContext);
		}

		@Override
		protected VirtualModelInstance makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext) {

			View view = getVirtualModelInstance(evaluationContext);
			logger.info("view: " + view);
			if (view == null) {
				logger.warning("null View");
				return null;
			}
			if (evaluationContext instanceof FlexoBehaviourAction) {
				String name = null;
				String title = null;
				try {
					name = getVirtualModelInstanceName().getBindingValue(evaluationContext);
					title = getVirtualModelInstanceTitle().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				CreateBasicVirtualModelInstance createVMIAction = CreateBasicVirtualModelInstance.actionType.makeNewEmbeddedAction(view,
						null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				createVMIAction.setSkipChoosePopup(true);
				createVMIAction.setEscapeModelSlotConfiguration(true);
				createVMIAction.setNewVirtualModelInstanceName(name);
				createVMIAction.setNewVirtualModelInstanceTitle(title);
				createVMIAction.setVirtualModel((VirtualModel) getFlexoConceptType());
				createVMIAction.doAction();
				return createVMIAction.getNewVirtualModelInstance();
			}

			logger.warning("Unexpected RunTimeEvaluationContext");
			return null;

		}

	}
}
