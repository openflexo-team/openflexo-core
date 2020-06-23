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

package org.openflexo.view;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.widget.FIBBrowserAction;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.view.controller.FlexoController;

/**
 * A built-in adapter in openflexo ui layer which allows to automatically map FlexoAction environment to FIBBrowser behaviour
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(FIBBrowserActionAdapter.FIBBrowserActionAdapterImpl.class)
@XMLElement
public interface FIBBrowserActionAdapter<T extends FlexoObject> extends FIBBrowserAction {

	public Object performAction(T selected);

	public boolean isAvailable(T selected);

	@Override
	public String getName();

	/**
	 * Return type of action (might be Add, Delete or Custom)
	 */
	@Override
	public ActionType getActionType();

	/**
	 * Return {@link FlexoActionFactory}
	 * 
	 * @return
	 */
	public FlexoActionFactory<?, T, ?> getFlexoActionType();

	public abstract class FIBBrowserActionAdapterImpl<T extends FlexoObject> extends FIBBrowserActionImpl
			implements FIBBrowserActionAdapter<T> {

		public static <T extends FlexoObject> FIBBrowserActionAdapter<T> makeFIBBrowserActionAdapter(FlexoActionFactory<?, T, ?> actionType,
				FIBBrowserView<?> browserView, FlexoController controller) throws ModelDefinitionException {

			FIBModelFactory fibModelFactory = new FIBModelFactory(
					controller != null ? controller.getApplicationContext().getTechnologyAdapterService() : null,
					FIBBrowserActionAdapter.class);
			FIBBrowserActionAdapterImpl<T> returned = (FIBBrowserActionAdapterImpl<T>) fibModelFactory
					.newInstance(FIBBrowserActionAdapter.class);
			returned.initWithActionType(actionType, browserView);
			return returned;
		}

		private FlexoActionFactory<?, T, ?> actionType;
		private FIBBrowserView<?> browserView;

		private void initWithActionType(FlexoActionFactory<?, T, ?> actionType, FIBBrowserView<?> browserView) {
			this.actionType = actionType;
			this.browserView = browserView;
			setMethod(new DataBinding<>("action.performAction(selected)"));
			setIsAvailable(new DataBinding<>("action.isAvailable(selected)"));
		}

		@Override
		public Object performAction(T selected) {
			FlexoAction<?, ?, ?> action = actionType.makeNewAction(selected, null, browserView.getFIBController().getEditor());
			action.doAction();
			return action;
		}

		@Override
		public boolean isAvailable(T selected) {
			/*System.out.println("browserView=" + browserView);
			System.out.println("browserView.getFIBController()=" + browserView.getFIBController());
			System.out
					.println("browserView.getFIBController().getFlexoController()=" + browserView.getFIBController().getFlexoController());
			System.out.println("browserView.getFIBController().getEditor()=" + browserView.getFIBController().getEditor());*/

			if (browserView.getFIBController().getEditor() != null) {
				return browserView.getFIBController().getEditor().isActionVisible(actionType, selected, null)
						&& browserView.getFIBController().getEditor().isActionEnabled(actionType, selected, null);
			}

			return false;
		}

		@Override
		public String getName() {
			return actionType.getUnlocalizedName();
		}

		@Override
		public ActionType getActionType() {
			if (actionType.getActionCategory() == FlexoActionFactory.ADD_ACTION_TYPE) {
				return ActionType.Add;
			}
			else if (actionType.getActionCategory() == FlexoActionFactory.DELETE_ACTION_TYPE) {
				return ActionType.Delete;
			}
			else {
				return ActionType.Custom;
			}
		}

		@Override
		public FlexoActionFactory<?, T, ?> getFlexoActionType() {
			return actionType;
		}

		@Override
		public boolean isSerializable() {
			return false;
		}

	}
}
