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
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rm.AbstractVirtualModelResource;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.action.CreateSubViewInView;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * This action is used to explicitely instanciate a new {@link View} in a given {@link View}
 * 
 * @author sylvain
 * 
 */

@FIBPanel("Fib/FML/AddSubViewPanel.fib")
@ModelEntity
@ImplementationClass(AddSubView.AddSubViewImpl.class)
@XMLElement
@FML("AddSubView")
public interface AddSubView extends AddAbstractVirtualModelInstance<View> {

	public ViewPointResource getViewPointType();

	public void setViewPointType(ViewPointResource resource);

	public static abstract class AddSubViewImpl extends AddAbstractVirtualModelInstanceImpl<View>implements AddSubView {

		static final Logger logger = Logger.getLogger(AddSubView.class.getPackage().getName());

		@Override
		public View execute(RunTimeEvaluationContext evaluationContext) {
			return super.execute(evaluationContext);
		}

		@Override
		public ViewPointResource getViewPointType() {
			return (ViewPointResource) getVirtualModelType();
		}

		@Override
		public void setViewPointType(ViewPointResource resource) {
			AbstractVirtualModelResource<?> oldVPType = getViewPointType();
			setVirtualModelType(resource);
			getPropertyChangeSupport().firePropertyChange("viewPointType", oldVPType, getViewPointType());
		}

		@Override
		protected View makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext) {

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
				CreateSubViewInView createSubViewAction = CreateSubViewInView.actionType
						.makeNewEmbeddedAction((ViewResource) view.getResource(), null, (FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				createSubViewAction.setSkipChoosePopup(true);
				createSubViewAction.setEscapeModelSlotConfiguration(true);
				createSubViewAction.setNewVirtualModelInstanceName(name);
				createSubViewAction.setNewVirtualModelInstanceTitle(title);
				createSubViewAction.setVirtualModel((ViewPoint) getFlexoConceptType());
				createSubViewAction.doAction();
				return createSubViewAction.getNewVirtualModelInstance();
			}

			logger.warning("Unexpected RunTimeEvaluationContext");
			return null;

		}

	}
}
